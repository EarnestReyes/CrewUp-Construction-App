package workers.works.invoice;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    // Company Info
    private TextInputEditText WorkerName, WorkerAddress, WorkerPhone, WorkerEmail;
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
    private TextView tvTotalMaterials, tvTotalLabor, tvTotalMisc, tvGrandTotal;

    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    // Firebase
    private ProposalFirebaseManager proposalManager;
    private String workerId;
    private String clientId; // You'll need to pass this when opening the activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_cost_quote);

        // Get current worker ID from Firebase Auth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            workerId = currentUser.getUid();
        }

        // Get client ID from intent
        clientId = getIntent().getStringExtra("clientId");

        proposalManager = new ProposalFirebaseManager();

        initializeViews();
        setupRecyclerViews();
        setupListeners();
    }

    private void initializeViews() {
        // Company Info
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
        btnSendProposal = findViewById(R.id.btn_send_proposal); // Add this button to your layout
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

        tvTotalMaterials.setText("₱" + currencyFormat.format(totalMaterials));
        tvTotalLabor.setText("₱" + currencyFormat.format(totalLabor));
        tvTotalMisc.setText("₱" + currencyFormat.format(totalMisc));
        tvGrandTotal.setText("₱" + currencyFormat.format(grandTotal));
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

    /**
     * Generate invoice HTML for preview/download
     */
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

    /**
     * Show dialog to confirm sending proposal to client
     */
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
     * Send proposal to Firebase (client will receive notification)
     */
    private void sendProposalToClient() {
        Toast.makeText(this, "Sending proposal...", Toast.LENGTH_SHORT).show();

        Invoice invoice = createInvoiceObject();

        // IMPORTANT: Get the projectId from intent
        String projectId = getIntent().getStringExtra("projectId");

        InvoiceProposalModel proposal = new InvoiceProposalModel(invoice, workerId, clientId);

        // Link proposal to the project
        proposal.setProjectId(projectId);  // ← ADD THIS LINE

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

    private void sendNotificationToClient(String proposalId) {
        // TODO: Implement FCM notification to client
        // You'll need to have the client's FCM token stored in their user document
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

        invoice.setCompanyName(WorkerName.getText().toString());
        invoice.setCompanyAddress(WorkerAddress.getText().toString());
        invoice.setCompanyPhone(WorkerPhone.getText().toString());
        invoice.setCompanyEmail(WorkerEmail.getText().toString());

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
