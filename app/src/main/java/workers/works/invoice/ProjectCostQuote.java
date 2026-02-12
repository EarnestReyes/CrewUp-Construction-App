package workers.works.invoice;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ConstructionApp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import adapters.LaborAdapter;
import adapters.MaterialAdapter;
import adapters.MiscAdapter;
import dialogs.AddLaborDialog;
import dialogs.AddMaterialDialog;
import dialogs.AddMiscDialog;
import workers.works.WorkerProjectModel;

public class ProjectCostQuote extends AppCompatActivity {

    private static final String TAG = "ProjectCostQuote";

    // Worker Info
    private TextInputEditText WorkerName, WorkerAddress, WorkerPhone, WorkerEmail;

    // Client Info
    private TextInputEditText etClientName, etClientAddress, etClientPhone, etClientEmail;
    private TextInputEditText etWorkDescription;

    private WorkerProjectModel project;

    // Buttons
    private Button btnAddMaterials, btnAddLabor, btnAddMisc, btnGenerateInvoice, btnSendProposal;
    private FloatingActionButton fabBack;

    // RecyclerViews
    private RecyclerView rvMaterials, rvLabor, rvMisc;

    // Adapters
    private MaterialAdapter materialAdapter;
    private LaborAdapter laborAdapter;
    private MiscAdapter miscAdapter;

    // Data Lists
    private List<MaterialItem> materialsList = new ArrayList<>();
    private List<LaborItem> laborList = new ArrayList<>();
    private List<MiscItem> miscList = new ArrayList<>();

    // Summary TextViews
    private TextView tvTotalMaterials, tvTotalLabor, tvTotalMisc, tvGrandTotal, tvVAT, tvGrandTotal2;

    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    // Firebase
    private FirebaseFirestore db;
    private ProposalFirebaseManager proposalManager;
    private String workerId;
    private String userId;
    private String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_cost_quote);

        db = FirebaseFirestore.getInstance();

        // Get current worker ID from Firebase Auth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            workerId = currentUser.getUid();
        }


        projectId = getIntent().getStringExtra("projectId");
        userId = getIntent().getStringExtra("userId");



        proposalManager = new ProposalFirebaseManager();

        initializeViews();
        setupRecyclerViews();
        setupListeners();

        loadProjectDetails();

        // 🔥 Load worker data from users collection
        loadWorkerDetails();
    }

    private void initializeViews() {
        // Worker Info
        WorkerName = findViewById(R.id.worker_name);
        WorkerAddress = findViewById(R.id.worker_address);
        WorkerPhone = findViewById(R.id.worker_mobile);
        WorkerEmail = findViewById(R.id.worker_email);

        // Client Info
        etClientName = findViewById(R.id.et_client_name);
        etClientAddress = findViewById(R.id.et_client_address);
        etClientPhone = findViewById(R.id.et_client_phone);
        etClientEmail = findViewById(R.id.et_client_email);

        // Work Description
        etWorkDescription = findViewById(R.id.et_work_description);

        // Buttons
        btnAddMaterials = findViewById(R.id.btn_add_materials);
        btnAddLabor = findViewById(R.id.btn_add_labor);
        btnAddMisc = findViewById(R.id.btn_add_misc);
        btnGenerateInvoice = findViewById(R.id.btn_generate_invoice);
        btnSendProposal = findViewById(R.id.btn_send_proposal);
        fabBack = findViewById(R.id.fab_back);

        // RecyclerViews
        rvMaterials = findViewById(R.id.rv_materials);
        rvLabor = findViewById(R.id.rv_labor);
        rvMisc = findViewById(R.id.rv_misc);

        // Summary TextViews
        tvTotalMaterials = findViewById(R.id.tv_total_materials);
        tvTotalLabor = findViewById(R.id.tv_total_labor);
        tvTotalMisc = findViewById(R.id.tv_total_misc);
        tvGrandTotal = findViewById(R.id.tv_grand_total);
        tvVAT = findViewById(R.id.tv_VAT);
        tvGrandTotal2 = findViewById(R.id.tv_grand_total2);
    }


    private void loadProjectDetails() {
        if (projectId == null) {
            Log.e(TAG, "ProjectId is null");
            Toast.makeText(this, "Error: No project ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Loading project details from BookingOrder");

        db.collection("BookingOrder")
                .document(projectId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Project document found");

                        // 🔥 CLIENT INFO FROM BOOKINGORDER (exact field names from screenshot)
                        String clientName = documentSnapshot.getString("Name");
                        String clientEmail = documentSnapshot.getString("Email");
                        String clientPhone = documentSnapshot.getString("Mobile Number");

                        String siteAddress = documentSnapshot.getString("Site_Address");
                        String workDescription = documentSnapshot.getString("Description");

                        // Set client info
                        etClientName.setText(clientName != null ? clientName : "");
                        etClientEmail.setText(clientEmail != null ? clientEmail : "");
                        etClientPhone.setText(clientPhone != null ? clientPhone : "");
                        etClientAddress.setText(siteAddress != null ? siteAddress : "");
                        etWorkDescription.setText(workDescription != null ? workDescription : "");

                        Log.d(TAG, "Client info loaded: " + clientName);
                        Log.d(TAG, "Client Email: " + clientEmail);
                        Log.d(TAG, "Client Phone: " + clientPhone);
                        Log.d(TAG, "Site Address: " + siteAddress);
                        Log.d(TAG, "Description: " + workDescription);

                    } else {
                        Log.e(TAG, "Project document not found");
                        Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading project", e);
                    Toast.makeText(this, "Error loading project: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }


    private void loadWorkerDetails() {
        if (workerId == null) {
            Log.e(TAG, "WorkerId is null");
            Toast.makeText(this, "Error: Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Loading worker details from users collection");

        db.collection("users")
                .document(workerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Worker document found");

                        // 🔥 BUILD FULL NAME FROM SEPARATE FIELDS (exact field names from screenshot)
                        String firstName = documentSnapshot.getString("FirstName");
                        String middleInitial = documentSnapshot.getString("MiddleInitial");
                        String lastName = documentSnapshot.getString("LastName");

                        String fullName = firstName + " " + middleInitial + " " + lastName;


                        String workerFullName = fullName.toString();
                        String workerEmail = documentSnapshot.getString("email");
                        String workerPhone = documentSnapshot.getString("Mobile Number");
                        String workerAddress = documentSnapshot.getString("Address");
                        WorkerName.setText(workerFullName);
                        WorkerEmail.setText(workerEmail != null ? workerEmail : "");
                        WorkerPhone.setText(workerPhone != null ? workerPhone : "");
                        WorkerAddress.setText(workerAddress != null ? workerAddress : "");

                        Log.d(TAG, "Worker info loaded");
                        Log.d(TAG, "Full Name: " + workerFullName);
                        Log.d(TAG, "Email: " + workerEmail);
                        Log.d(TAG, "Phone: " + workerPhone);
                        Log.d(TAG, "Address: " + workerAddress);

                    } else {
                        Log.e(TAG, "Worker document not found");
                        Toast.makeText(this, "Worker profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading worker details", e);
                    Toast.makeText(this, "Error loading worker details: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setupRecyclerViews() {
        // Materials RecyclerView
        materialAdapter = new MaterialAdapter(materialsList, new MaterialAdapter.OnItemActionListener() {
            @Override
            public void onEdit(int position) {
                editMaterial(position);
            }

            @Override
            public void onDelete(int position) {
                deleteMaterial(position);
            }
        });
        rvMaterials.setLayoutManager(new LinearLayoutManager(this));
        rvMaterials.setAdapter(materialAdapter);

        // Labor RecyclerView
        laborAdapter = new LaborAdapter(laborList, new LaborAdapter.OnItemActionListener() {
            @Override
            public void onEdit(int position) {
                editLabor(position);
            }

            @Override
            public void onDelete(int position) {
                deleteLabor(position);
            }
        });
        rvLabor.setLayoutManager(new LinearLayoutManager(this));
        rvLabor.setAdapter(laborAdapter);

        // Misc RecyclerView
        miscAdapter = new MiscAdapter(miscList, new MiscAdapter.OnItemActionListener() {
            @Override
            public void onEdit(int position) {
                editMisc(position);
            }

            @Override
            public void onDelete(int position) {
                deleteMisc(position);
            }
        });
        rvMisc.setLayoutManager(new LinearLayoutManager(this));
        rvMisc.setAdapter(miscAdapter);
    }

    private void setupListeners() {
        // Add items
        btnAddMaterials.setOnClickListener(v -> showAddMaterialDialog());
        btnAddLabor.setOnClickListener(v -> showAddLaborDialog());
        btnAddMisc.setOnClickListener(v -> showAddMiscDialog());

        // Generate invoice (preview only)
        btnGenerateInvoice.setOnClickListener(v -> generateInvoice());

        // Send proposal to client (saves to Firebase)
        btnSendProposal.setOnClickListener(v -> showSendProposalDialog());

        // Back button
        fabBack.setOnClickListener(v -> finish());
    }

    private void showAddMaterialDialog() {
        AddMaterialDialog dialog = new AddMaterialDialog(this, null, new AddMaterialDialog.OnMaterialAddedListener() {
            @Override
            public void onMaterialAdded(MaterialItem material) {
                materialsList.add(material);
                materialAdapter.notifyDataSetChanged();
                updateTotals();
            }
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void editMaterial(int position) {
        MaterialItem material = materialsList.get(position);
        AddMaterialDialog dialog = new AddMaterialDialog(this, material, new AddMaterialDialog.OnMaterialAddedListener() {
            @Override
            public void onMaterialAdded(MaterialItem updatedMaterial) {
                materialsList.set(position, updatedMaterial);
                materialAdapter.notifyDataSetChanged();
                updateTotals();
            }
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void deleteMaterial(int position) {
        materialsList.remove(position);
        materialAdapter.notifyDataSetChanged();
        updateTotals();
        Toast.makeText(this, "Material deleted", Toast.LENGTH_SHORT).show();
    }

    private void showAddLaborDialog() {
        AddLaborDialog dialog = new AddLaborDialog(this, null, new AddLaborDialog.OnLaborAddedListener() {
            @Override
            public void onLaborAdded(LaborItem labor) {
                laborList.add(labor);
                laborAdapter.notifyDataSetChanged();
                updateTotals();
            }
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void editLabor(int position) {
        LaborItem labor = laborList.get(position);
        AddLaborDialog dialog = new AddLaborDialog(this, labor, new AddLaborDialog.OnLaborAddedListener() {
            @Override
            public void onLaborAdded(LaborItem updatedLabor) {
                laborList.set(position, updatedLabor);
                laborAdapter.notifyDataSetChanged();
                updateTotals();
            }
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void deleteLabor(int position) {
        laborList.remove(position);
        laborAdapter.notifyDataSetChanged();
        updateTotals();
        Toast.makeText(this, "Labor item deleted", Toast.LENGTH_SHORT).show();
    }

    private void showAddMiscDialog() {
        AddMiscDialog dialog = new AddMiscDialog(this, null, new AddMiscDialog.OnMiscAddedListener() {
            @Override
            public void onMiscAdded(MiscItem misc) {
                miscList.add(misc);
                miscAdapter.notifyDataSetChanged();
                updateTotals();
            }
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void editMisc(int position) {
        MiscItem misc = miscList.get(position);
        AddMiscDialog dialog = new AddMiscDialog(this, misc, new AddMiscDialog.OnMiscAddedListener() {
            @Override
            public void onMiscAdded(MiscItem updatedMisc) {
                miscList.set(position, updatedMisc);
                miscAdapter.notifyDataSetChanged();
                updateTotals();
            }
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void deleteMisc(int position) {
        miscList.remove(position);
        miscAdapter.notifyDataSetChanged();
        updateTotals();
        Toast.makeText(this, "Miscellaneous charge deleted", Toast.LENGTH_SHORT).show();
    }

    private void updateTotals() {
        double totalMaterials = calculateTotalMaterials();
        double totalLabor = calculateTotalLabor();
        double totalMisc = calculateTotalMisc();
        double grandTotal = totalMaterials + totalLabor + totalMisc;
        double VAT = grandTotal * 0.12;
        double grandTotal2 = grandTotal + VAT;

        tvTotalMaterials.setText("₱" + currencyFormat.format(totalMaterials));
        tvTotalLabor.setText("₱" + currencyFormat.format(totalLabor));
        tvTotalMisc.setText("₱" + currencyFormat.format(totalMisc));
        tvGrandTotal.setText("₱" + currencyFormat.format(grandTotal));
        tvVAT.setText("₱" + currencyFormat.format(VAT));
        tvGrandTotal2.setText("₱" + currencyFormat.format(grandTotal2));
    }

    private double calculateTotalMaterials() {
        double total = 0;
        for (MaterialItem item : materialsList) {
            total += item.getTotal();
        }
        return total;
    }

    private double calculateTotalLabor() {
        double total = 0;
        for (LaborItem item : laborList) {
            total += item.getAmount();
        }
        return total;
    }

    private double calculateTotalMisc() {
        double total = 0;
        for (MiscItem item : miscList) {
            total += item.getAmount();
        }
        return total;
    }

    private void generateInvoice() {
        if (!validateInputs()) {
            return;
        }

        Toast.makeText(this, "Generating invoice preview...", Toast.LENGTH_SHORT).show();

        Invoice invoice = createInvoiceObject();
        InvoiceGenerator generator = new InvoiceGenerator(this);
        File invoiceFile = generator.generate(invoice);

        if (invoiceFile != null && invoiceFile.exists()) {
            Toast.makeText(this, "Invoice generated successfully!", Toast.LENGTH_LONG).show();
            shareInvoice(invoiceFile);
        } else {
            Toast.makeText(this, "Failed to generate invoice", Toast.LENGTH_LONG).show();
        }
    }

    private void showSendProposalDialog() {
        if (!validateInputs()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Send Proposal")
                .setMessage("Send this cost quotation to " + etClientName.getText().toString() + "?")
                .setPositiveButton("Send", (dialog, which) -> sendProposalToClient())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendProposalToClient() {
        Toast.makeText(this, "Sending proposal...", Toast.LENGTH_SHORT).show();

        Invoice invoice = createInvoiceObject();
        InvoiceProposalModel proposal = new InvoiceProposalModel(invoice, workerId, userId);
        proposal.setProjectId(projectId);
        proposal.setUserId(userId);



        proposalManager.submitProposal(proposal, new ProposalFirebaseManager.OnProposalSubmitListener() {
            @Override
            public void onSuccess(String proposalId) {
                Toast.makeText(ProjectCostQuote.this,
                        "Proposal sent successfully! Waiting for client approval.",
                        Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ProjectCostQuote.this,
                        "Failed to send proposal: " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs() {
        if (WorkerName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter worker name", Toast.LENGTH_SHORT).show();
            WorkerName.requestFocus();
            return false;
        }

        if (etClientName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter client name", Toast.LENGTH_SHORT).show();
            etClientName.requestFocus();
            return false;
        }

        if (materialsList.isEmpty() && laborList.isEmpty() && miscList.isEmpty()) {
            Toast.makeText(this, "Please add at least one item", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private Invoice createInvoiceObject() {
        Invoice invoice = new Invoice();

        invoice.setWorkerName(WorkerName.getText().toString());
        invoice.setWorkerAddress(WorkerAddress.getText().toString());
        invoice.setWorkerPhone(WorkerPhone.getText().toString());
        invoice.setWorkerEmail(WorkerEmail.getText().toString());

        invoice.setClientName(etClientName.getText().toString());
        invoice.setClientAddress(etClientAddress.getText().toString());
        invoice.setClientPhone(etClientPhone.getText().toString());
        invoice.setClientEmail(etClientEmail.getText().toString());

        invoice.setWorkDescription(etWorkDescription.getText().toString());

        invoice.setMaterials(new ArrayList<>(materialsList));
        invoice.setLabor(new ArrayList<>(laborList));
        invoice.setMiscellaneous(new ArrayList<>(miscList));
        invoice.setVat(Double.parseDouble(tvVAT.getText().toString()));
        invoice.setGrandTotalWithVat(Double.parseDouble(tvGrandTotal2.getText().toString()));

        return invoice;
    }

    private void shareInvoice(File file) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/html");

        Uri fileUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                file
        );

        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Cost Quotation Preview");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Preview Invoice"));
    }
}