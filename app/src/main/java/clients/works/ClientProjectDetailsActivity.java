package clients.works;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ConstructionApp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Activity showing full details of a client's project
 */
public class ClientProjectDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ClientProjectDetails";

    private TextView tvWorkerName, tvWorkerPhone, tvWorkerEmail;
    private TextView tvWorkDescription, tvLocation, tvStatus, tvCreatedDate;
    private TextView tvStartDate, tvCompletionDate, tvTotalCost, tvNotes;

    private MaterialCardView cardWorkerInfo, cardProjectInfo, cardDates, cardCost;
    private MaterialCardView cardActions;

    private Button btnCancelProject, btnCompleteProject;

    private FirebaseFirestore db;
    private ClientProjectModel project;
    private String projectId;

    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_proposal_details);

        db = FirebaseFirestore.getInstance();
        projectId = getIntent().getStringExtra("projectId");

        if (projectId == null) {
            Toast.makeText(this, "Error: No project ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Opening project details for: " + projectId);

        initializeViews();
        setupToolbar();
        loadProjectDetails();
    }

    private void initializeViews() {
        // Worker info
        tvWorkerName = findViewById(R.id.tvWorkerName);
        tvWorkerPhone = findViewById(R.id.tvWorkerPhone);
        tvWorkerEmail = findViewById(R.id.tvWorkerEmail);

        // Project info
        tvWorkDescription = findViewById(R.id.tvWorkDescription);
        tvLocation = findViewById(R.id.tvLocation);
        tvStatus = findViewById(R.id.tvStatus);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        tvNotes = findViewById(R.id.tvNotes);

        // Dates
        tvStartDate = findViewById(R.id.tvStartDate);
        tvCompletionDate = findViewById(R.id.tvCompletionDate);

        // Cost
        tvTotalCost = findViewById(R.id.tvTotalCost);

        // Cards
        cardWorkerInfo = findViewById(R.id.cardWorkerInfo);
        cardProjectInfo = findViewById(R.id.cardProjectInfo);
        cardDates = findViewById(R.id.cardDates);
        cardCost = findViewById(R.id.cardCost);
        cardActions = findViewById(R.id.cardActions);

        // Buttons
        btnCancelProject = findViewById(R.id.btnDeclineProposal);
        btnCompleteProject = findViewById(R.id.btnAcceptProposal);


        setupButtonListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Project Details");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupButtonListeners() {
        btnCancelProject.setOnClickListener(v -> showCancelDialog());
        btnCompleteProject.setOnClickListener(v -> showCompleteDialog());

    }

    private void loadProjectDetails() {
        Log.d(TAG, "Loading project details for: " + projectId);

        db.collection("BookingOrder")
                .document(projectId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Project found: " + documentSnapshot.getData());

                        project = documentSnapshot.toObject(ClientProjectModel.class);
                        if (project != null) {
                            project.setProjectId(documentSnapshot.getId());
                            displayProjectDetails();
                        }
                    } else {
                        Log.e(TAG, "Project not found");
                        Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading project", e);
                    Toast.makeText(this, "Error loading project: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayProjectDetails() {
        // Worker info
        if (project.getWorkerName() != null && !project.getWorkerName().isEmpty()) {
            cardWorkerInfo.setVisibility(View.VISIBLE);
            tvWorkerName.setText(project.getWorkerName());
        }

        // Project info
        tvWorkDescription.setText(project.getWorkDescription() != null ?
                project.getWorkDescription() : "No description");
        tvLocation.setText(project.getLocation() != null ?
                project.getLocation() : "Location not specified");

        String status = project.getStatus() != null ? project.getStatus() : "pending";
        tvStatus.setText(getStatusText(status));
        tvStatus.setBackgroundResource(getStatusBackground(status));

        if (project.getCreatedAt() != null) {
            tvCreatedDate.setText(dateFormat.format(project.getCreatedAt().toDate()));
        } else {
            tvCreatedDate.setText("Recently");
        }

        if (project.getNotes() != null && !project.getNotes().isEmpty()) {
            tvNotes.setVisibility(View.VISIBLE);
            tvNotes.setText(project.getNotes());
        } else {
            tvNotes.setVisibility(View.GONE);
        }

        // Dates
        if (project.getStartDate() != null || project.getCompletionDate() != null) {
            cardDates.setVisibility(View.VISIBLE);

            if (project.getStartDate() != null) {
                tvStartDate.setText(dateFormat.format(project.getStartDate().toDate()));
            } else {
                tvStartDate.setText("Not set");
            }

            if (project.getCompletionDate() != null) {
                tvCompletionDate.setText(dateFormat.format(project.getCompletionDate().toDate()));
            } else {
                tvCompletionDate.setText("Not set");
            }
        } else {
            cardDates.setVisibility(View.GONE);
        }

        // Cost
        if (project.getTotalCost() > 0) {
            cardCost.setVisibility(View.VISIBLE);
            tvTotalCost.setText("₱" + currencyFormat.format(project.getTotalCost()));
        } else {
            cardCost.setVisibility(View.GONE);
        }

        // Update action buttons based on status
        updateActionButtons(status);
    }

    private void updateActionButtons(String status) {
        if ("pending".equals(status)) {
            cardActions.setVisibility(View.VISIBLE);
            btnCancelProject.setVisibility(View.VISIBLE);
            btnCompleteProject.setVisibility(View.GONE);

        } else if ("active".equals(status)) {
            cardActions.setVisibility(View.VISIBLE);
            btnCancelProject.setVisibility(View.VISIBLE);
            btnCompleteProject.setVisibility(View.VISIBLE);

        } else {
            cardActions.setVisibility(View.GONE);
        }
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Project")
                .setMessage("Are you sure you want to cancel this project? This action cannot be undone.")
                .setPositiveButton("Cancel Project", (dialog, which) -> cancelProject())
                .setNegativeButton("Keep Project", null)
                .show();
    }

    private void showCompleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Mark as Completed")
                .setMessage("Is this project completed? This will mark the project as finished.")
                .setPositiveButton("Mark Complete", (dialog, which) -> completeProject())
                .setNegativeButton("Not Yet", null)
                .show();
    }

    private void cancelProject() {
        Log.d(TAG, "Cancelling project: " + projectId);

        db.collection("BookingOrder")
                .document(projectId)
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Project cancelled", Toast.LENGTH_SHORT).show();
                    loadProjectDetails(); // Refresh
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cancelling project", e);
                    Toast.makeText(this, "Error cancelling project: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void completeProject() {
        Log.d(TAG, "Completing project: " + projectId);

        db.collection("BookingOrder")
                .document(projectId)
                .update("status", "completed")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Project marked as completed!", Toast.LENGTH_SHORT).show();
                    loadProjectDetails(); // Refresh
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error completing project", e);
                    Toast.makeText(this, "Error completing project: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }



    private String getStatusText(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return "Pending";
            case "active":
                return "Active";
            case "completed":
                return "Completed";
            case "cancelled":
                return "Cancelled";
            default:
                return "Unknown";
        }
    }

    private int getStatusBackground(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return R.drawable.bg_status_pending;
            case "active":
                return R.drawable.bg_status_accepted;
            case "completed":
                return R.drawable.bg_status_completed;
            case "cancelled":
                return R.drawable.bg_status_declined;
            default:
                return R.drawable.bg_status_pending;
        }
    }
}