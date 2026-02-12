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

public class ProjectCostQuote extends AppCompatActivity {

    private static final String TAG = "ProjectCostQuote";

    // Worker Info
    private TextInputEditText WorkerName, WorkerAddress, WorkerPhone, WorkerEmail;

    // Client Info
    private TextInputEditText etClientName, etClientAddress, etClientPhone, etClientEmail;
    private TextInputEditText etWorkDescription;

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
    private TextView tvTotalMaterials, tvTotalLabor, tvTotalMisc;
    private TextView tvSubtotal;      // tv_grand_total = Subtotal before VAT
    private TextView tvVAT;           // tv_VAT = 12% VAT amount
    private TextView tvGrandTotal2;   // tv_grand_total2 = Final total with VAT

    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    // 🔥 STORE RAW VALUES - Add these fields
    private double rawSubtotal = 0.0;
    private double rawVAT = 0.0;
    private double rawGrandTotal = 0.0;

    // Firebase
    private FirebaseFirestore db;
    private ProposalFirebaseManager proposalManager;
    private String workerId;
    public static String userId;
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

        // Get data from intent
        projectId = getIntent().getStringExtra("projectId");



        proposalManager = new ProposalFirebaseManager();

        initializeViews();
        setupRecyclerViews();
        setupListeners();
        loadProjectData();
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

        // Summary TextViews - Match XML IDs exactly
        tvTotalMaterials = findViewById(R.id.tv_total_materials);
        tvTotalLabor = findViewById(R.id.tv_total_labor);
        tvTotalMisc = findViewById(R.id.tv_total_misc);
        tvSubtotal = findViewById(R.id.tv_grand_total);    // Subtotal (before VAT)
        tvVAT = findViewById(R.id.tv_VAT);                  // VAT (12%)
        tvGrandTotal2 = findViewById(R.id.tv_grand_total2); // Grand Total (with VAT)
    }

    private void loadProjectData() {
        if (projectId == null) {
            Log.w(TAG, "No projectId provided");
            return;
        }

        Log.d(TAG, "Loading project data for: " + projectId);

        // Load from BookingOrder
        db.collection("BookingOrder")
                .document(projectId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String clientId = doc.getString("userId");

                        userId = clientId;
                        etClientName.setText(doc.getString("Name"));
                        etClientEmail.setText(doc.getString("Email"));
                        etClientPhone.setText(doc.getString("Mobile Number"));
                        etClientAddress.setText(doc.getString("Site_Address"));
                        etWorkDescription.setText(doc.getString("Description"));

                        Log.d(TAG, "ProjectId: " + projectId + ", ClientId: " + clientId);
                        Log.d(TAG, "Client data loaded");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading project data", e));

        // Load worker info
        if (workerId != null) {
            db.collection("users")
                    .document(workerId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String firstName = doc.getString("FirstName");
                            String middleInitial = doc.getString("MiddleInitial");
                            String lastName = doc.getString("LastName");

                            StringBuilder fullName = new StringBuilder();
                            if (firstName != null) fullName.append(firstName);
                            if (middleInitial != null) {
                                if (fullName.length() > 0) fullName.append(" ");
                                fullName.append(middleInitial);
                                if (!middleInitial.endsWith(".")) fullName.append(".");
                            }
                            if (lastName != null) {
                                if (fullName.length() > 0) fullName.append(" ");
                                fullName.append(lastName);
                            }

                            WorkerName.setText(fullName.toString());
                            WorkerEmail.setText(doc.getString("email"));
                            WorkerPhone.setText(doc.getString("Mobile Number"));

                            String address = doc.getString("address");
                            if (address == null) address = doc.getString("Address");
                            if (address == null) address = doc.getString("Home_Address");
                            WorkerAddress.setText(address != null ? address : "");

                            Log.d(TAG, "Worker data loaded");
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error loading worker data", e));
        }
    }

    private void setupRecyclerViews() {
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
        btnAddMaterials.setOnClickListener(v -> showAddMaterialDialog());
        btnAddLabor.setOnClickListener(v -> showAddLaborDialog());
        btnAddMisc.setOnClickListener(v -> showAddMiscDialog());
        btnGenerateInvoice.setOnClickListener(v -> generateInvoice());
        btnSendProposal.setOnClickListener(v -> showSendProposalDialog());
        fabBack.setOnClickListener(v -> finish());
    }

    // Material dialogs
    private void showAddMaterialDialog() {
        AddMaterialDialog dialog = new AddMaterialDialog(this, null, material -> {
            materialsList.add(material);
            materialAdapter.notifyDataSetChanged();
            updateTotals();
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void editMaterial(int position) {
        MaterialItem material = materialsList.get(position);
        AddMaterialDialog dialog = new AddMaterialDialog(this, material, updatedMaterial -> {
            materialsList.set(position, updatedMaterial);
            materialAdapter.notifyDataSetChanged();
            updateTotals();
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void deleteMaterial(int position) {
        materialsList.remove(position);
        materialAdapter.notifyDataSetChanged();
        updateTotals();
        Toast.makeText(this, "Material deleted", Toast.LENGTH_SHORT).show();
    }

    // Labor dialogs
    private void showAddLaborDialog() {
        AddLaborDialog dialog = new AddLaborDialog(this, null, labor -> {
            laborList.add(labor);
            laborAdapter.notifyDataSetChanged();
            updateTotals();
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void editLabor(int position) {
        LaborItem labor = laborList.get(position);
        AddLaborDialog dialog = new AddLaborDialog(this, labor, updatedLabor -> {
            laborList.set(position, updatedLabor);
            laborAdapter.notifyDataSetChanged();
            updateTotals();
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void deleteLabor(int position) {
        laborList.remove(position);
        laborAdapter.notifyDataSetChanged();
        updateTotals();
        Toast.makeText(this, "Labor item deleted", Toast.LENGTH_SHORT).show();
    }

    // Misc dialogs
    private void showAddMiscDialog() {
        AddMiscDialog dialog = new AddMiscDialog(this, null, misc -> {
            miscList.add(misc);
            miscAdapter.notifyDataSetChanged();
            updateTotals();
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void editMisc(int position) {
        MiscItem misc = miscList.get(position);
        AddMiscDialog dialog = new AddMiscDialog(this, misc, updatedMisc -> {
            miscList.set(position, updatedMisc);
            miscAdapter.notifyDataSetChanged();
            updateTotals();
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void deleteMisc(int position) {
        miscList.remove(position);
        miscAdapter.notifyDataSetChanged();
        updateTotals();
        Toast.makeText(this, "Miscellaneous charge deleted", Toast.LENGTH_SHORT).show();
    }

    /**
     * 🔥 UPDATE TOTALS WITH VAT AND STORE RAW VALUES
     */
    private void updateTotals() {
        double totalMaterials = calculateTotalMaterials();
        double totalLabor = calculateTotalLabor();
        double totalMisc = calculateTotalMisc();

        // Subtotal (before VAT)
        rawSubtotal = totalMaterials + totalLabor + totalMisc;

        // VAT (12%)
        rawVAT = rawSubtotal * 0.12;

        // Grand Total (Subtotal + VAT)
        rawGrandTotal = rawSubtotal + rawVAT;

        // Update UI
        tvTotalMaterials.setText("₱" + currencyFormat.format(totalMaterials));
        tvTotalLabor.setText("₱" + currencyFormat.format(totalLabor));
        tvTotalMisc.setText("₱" + currencyFormat.format(totalMisc));
        tvSubtotal.setText("₱" + currencyFormat.format(rawSubtotal));
        tvVAT.setText("₱" + currencyFormat.format(rawVAT));
        tvGrandTotal2.setText("₱" + currencyFormat.format(rawGrandTotal));

        Log.d(TAG, String.format("Totals - Subtotal: %.2f, VAT: %.2f, Grand Total: %.2f",
                rawSubtotal, rawVAT, rawGrandTotal));
    }

    // 🔥 HELPER METHODS TO GET RAW NUMERIC VALUES
    public double getRawSubtotal() {
        return rawSubtotal;
    }

    public double getRawVAT() {
        return rawVAT;
    }

    public double getRawGrandTotal() {
        return rawGrandTotal;
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

    /**
     * 🔥 SEND PROPOSAL WITH VAT AND GRAND TOTAL TO WORKERINPUT
     */
    private void sendProposalToClient() {
        Toast.makeText(this, "Sending proposal...", Toast.LENGTH_SHORT).show();

        Invoice invoice = createInvoiceObject();

        // 🔥 USE RAW VALUES FOR CALCULATIONS
        invoice.setVat(rawVAT);
        invoice.setGrandTotalWithVat(rawGrandTotal);
        invoice.setUserId(userId);

        Log.d(TAG, String.format("Creating proposal - Subtotal: %.2f, VAT: %.2f, Grand Total: %.2f",
                rawSubtotal, rawVAT, rawGrandTotal));
        Log.d(TAG, String.format("Creating UserId - " + userId));

        InvoiceProposalModel proposal = new InvoiceProposalModel(invoice, workerId);
        proposal.setProjectId(projectId);

        // 🔥 EXPLICITLY SET VAT AND GRAND TOTAL
        proposal.setVat(rawVAT);
        proposal.setGrandTotalWithVat(rawGrandTotal);
        proposal.setTotalCost(rawSubtotal);  // Set subtotal as totalCost

        //Log.d(TAG, "Proposal VAT: " + proposal.getVat());
        //Log.d(TAG, "Proposal Grand Total: " + proposal.setGrandTotalWithVat());

        proposalManager.submitProposal(proposal, new ProposalFirebaseManager.OnProposalSubmitListener() {
            @Override
            public void onSuccess(String proposalId) {
                Log.d(TAG, "Proposal submitted successfully: " + proposalId);
                Toast.makeText(ProjectCostQuote.this,
                        "Proposal sent successfully!",
                        Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Proposal submission failed: " + error);
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