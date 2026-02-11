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

public class ClientProjectDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ClientProjectDetails";

    // Worker info
    private TextView tvWorkerName, tvWorkerPhone, tvWorkerEmail;

    // Project info
    private TextView tvWorkDescription, tvNotes, tvTotalMaterials, tvTotalLabor, tvTotalMisc, tvGrandTotal;

    // Dates
    private TextView tvStartDate, tvCompletionDate;

    // Actions
    private MaterialCardView cardActions;
    private Button btnAcceptProposal, btnDeclineProposal;

    private FirebaseFirestore db;
    private ClientProjectModel project;
    private String projectId;

    private final DecimalFormat currencyFormat =
            new DecimalFormat("#,##0.00");

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_proposal_details);

        db = FirebaseFirestore.getInstance();
        projectId = getIntent().getStringExtra("projectId");

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

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

        // Worker
        tvWorkerName = findViewById(R.id.tvWorkerName);
        tvWorkerPhone = findViewById(R.id.tvWorkerPhone);
        tvWorkerEmail = findViewById(R.id.tvWorkerEmail);

        // Project
        tvWorkDescription = findViewById(R.id.tvWorkDescription);
        tvNotes = findViewById(R.id.tvNotes);
        tvTotalMaterials = findViewById(R.id.tvTotalMaterials);
        tvTotalLabor = findViewById(R.id.tvTotalLabor);
        tvTotalMisc = findViewById(R.id.tvTotalMisc);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);

        // Dates (only if present in layout)
        tvStartDate = findViewById(R.id.tvStartDate);
        tvCompletionDate = findViewById(R.id.tvCompletionDate);

        // Actions
        cardActions = findViewById(R.id.cardActions);
        btnAcceptProposal = findViewById(R.id.btnAcceptProposal);
        btnDeclineProposal = findViewById(R.id.btnDeclineProposal);

        if (btnAcceptProposal != null) {
            btnAcceptProposal.setOnClickListener(v -> showCompleteDialog());
        }

        if (btnDeclineProposal != null) {
            btnDeclineProposal.setOnClickListener(v -> showCancelDialog());
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

    private void loadProjectDetails() {
        db.collection("WorkerInput")
                .document(projectId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    project = doc.toObject(ClientProjectModel.class);
                    if (project == null) {
                        Toast.makeText(this, "Invalid project data", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    displayProjectDetails();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Load failed", e);
                    Toast.makeText(this, "Failed to load project", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayProjectDetails() {

        // Worker info
        tvWorkerName.setText(nonNull(project.getWorkerName(), "N/A"));
        tvWorkerPhone.setText(nonNull(project.getWorkerPhone(), "N/A"));
        tvWorkerEmail.setText(nonNull(project.getWorkerEmail(), "N/A"));

        // Work description
        tvWorkDescription.setText(
                nonNull(project.getWorkDescription(), "No description")
        );

        tvTotalMaterials.setText(nonNull(String.valueOf(project.getMaterialsCost()), "0.00"));
        tvTotalLabor.setText(nonNull(String.valueOf(project.getLaborCost()), "0.00"));
        tvTotalMisc.setText(nonNull(String.valueOf(project.getMiscCost()), "0.00"));
        tvGrandTotal.setText(nonNull(String.valueOf(project.getMiscCost()), "0.00"));


        // Notes (NULL-SAFE FIX 🔥)
        if (tvNotes != null) {
            if (project.getNotes() != null && !project.getNotes().isEmpty()) {
                tvNotes.setVisibility(View.VISIBLE);
                tvNotes.setText(project.getNotes());
            } else {
                tvNotes.setVisibility(View.GONE);
            }
        }

        // Dates (NULL-SAFE)
        if (tvStartDate != null) {
            tvStartDate.setText(project.getStartDate() != null
                    ? dateFormat.format(project.getStartDate().toDate())
                    : "Not set");
        }

        if (tvCompletionDate != null) {
            tvCompletionDate.setText(project.getCompletionDate() != null
                    ? dateFormat.format(project.getCompletionDate().toDate())
                    : "Not set");
        }

        updateActions(project.getStatus());
    }


    private void updateActions(String status) {
        if (cardActions == null) return;

        if ("pending".equals(status) || "active".equals(status)) {
            cardActions.setVisibility(View.VISIBLE);
        } else {
            cardActions.setVisibility(View.GONE);
        }
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Decline Proposal")
                .setMessage("Are you sure you want to decline this proposal?")
                .setPositiveButton("Yes", (d, w) -> updateStatus("cancelled"))
                .setNegativeButton("No", null)
                .show();
    }

    private void showCompleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Accept Proposal")
                .setMessage("Do you want to accept this proposal?")
                .setPositiveButton("Yes", (d, w) -> updateStatus("active"))
                .setNegativeButton("No", null)
                .show();
    }

    private void updateStatus(String newStatus) {
        db.collection("WorkerInput")
                .document(projectId)
                .update("status", newStatus)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show();
                    btnAcceptProposal.setVisibility(View.GONE);
                    finish();
                    loadProjectDetails();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                );
    }

    private String nonNull(String v, String fallback) {
        return v != null ? v : fallback;
    }
}
