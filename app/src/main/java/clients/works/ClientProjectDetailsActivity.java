package clients.works;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ConstructionApp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Shows details from WorkerInput collection
 */
public class ClientProjectDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ClientProjectDetails";

    // Worker info
    private TextView tvWorkerName, tvWorkerPhone, tvWorkerEmail, tvWorkerAddress;

    // Project info
    private TextView tvWorkDescription, tvNotes;

    // Costs
    private TextView tvTotalMaterials, tvTotalLabor, tvTotalMisc, tvGrandTotal;

    // Actions
    private MaterialCardView cardActions;
    private Button btnAcceptProposal, btnDeclineProposal;

    private FirebaseFirestore db;
    private ClientProjectModel project;
    private String projectId;

    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_proposal_details);

        db = FirebaseFirestore.getInstance();
        projectId = getIntent().getStringExtra("projectId");

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (projectId == null) {
            Toast.makeText(this, "Invalid project", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadProjectDetails();
    }

    private void initViews() {
        tvWorkerName = findViewById(R.id.tvWorkerName);
        tvWorkerPhone = findViewById(R.id.tvWorkerPhone);
        tvWorkerEmail = findViewById(R.id.tvWorkerEmail);
        tvWorkerAddress = findViewById(R.id.tvWorkerAddress);

        tvWorkDescription = findViewById(R.id.tvWorkDescription);
        tvNotes = findViewById(R.id.tvNotes);

        tvTotalMaterials = findViewById(R.id.tvTotalMaterials);
        tvTotalLabor = findViewById(R.id.tvTotalLabor);
        tvTotalMisc = findViewById(R.id.tvTotalMisc);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);

        cardActions = findViewById(R.id.cardActions);
        btnAcceptProposal = findViewById(R.id.btnAcceptProposal);
        btnDeclineProposal = findViewById(R.id.btnDeclineProposal);

        if (btnAcceptProposal != null) {
            btnAcceptProposal.setOnClickListener(v -> showAcceptDialog());
        }

        if (btnDeclineProposal != null) {
            btnDeclineProposal.setOnClickListener(v -> showDeclineDialog());
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Load from WorkerInput collection
     */
    private void loadProjectDetails() {
        Log.d(TAG, "Loading from WorkerInput: " + projectId);

        db.collection("WorkerInput")
                .document(projectId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Proposal not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    project = doc.toObject(ClientProjectModel.class);
                    if (project == null) {
                        Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    project.setProjectId(doc.getId());
                    displayProjectDetails();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Load failed", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayProjectDetails() {
        // Worker info
        tvWorkerName.setText(nonNull(project.getWorkerName(), "N/A"));
        tvWorkerPhone.setText(nonNull(project.getWorkerPhone(), "N/A"));
        tvWorkerEmail.setText(nonNull(project.getWorkerEmail(), "N/A"));

        if (tvWorkerAddress != null) {
            tvWorkerAddress.setText(nonNull(project.getWorkerAddress(), "N/A"));
        }

        // Work description
        tvWorkDescription.setText(nonNull(project.getWorkerDescription(), "No description"));

        // Costs
        tvTotalMaterials.setText("₱" + currencyFormat.format(project.getTotalMaterials()));
        tvTotalLabor.setText("₱" + currencyFormat.format(project.getTotalLabor()));
        tvTotalMisc.setText("₱" + currencyFormat.format(project.getTotalMisc()));
        tvGrandTotal.setText("₱" + currencyFormat.format(project.getGrandTotal()));

        updateActionButtons(project.getStatus());
    }

    private void updateActionButtons(String status) {
        if (cardActions == null) return;

        if ("pending".equals(status)) {
            cardActions.setVisibility(View.VISIBLE);
        } else {
            cardActions.setVisibility(View.GONE);
        }
    }

    private void showAcceptDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Accept Proposal")
                .setMessage("Accept this proposal?")
                .setPositiveButton("Accept", (d, w) -> updateStatus("active"))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeclineDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Decline Proposal")
                .setMessage("Decline this proposal?")
                .setPositiveButton("Decline", (d, w) -> updateStatus("cancelled"))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateStatus(String newStatus) {
        db.collection("WorkerInput")
                .document(projectId)
                .update("status", newStatus)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                });
    }

    private String nonNull(String v, String fallback) {
        return (v != null && !v.isEmpty()) ? v : fallback;
    }
}