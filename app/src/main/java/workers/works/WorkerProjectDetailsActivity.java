package workers.works;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.ConstructionApp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import workers.works.invoice.ProjectCostQuote;

public class WorkerProjectDetailsActivity extends AppCompatActivity {

    private static final String TAG = "WorkerProjectDetails";

    private TextView tvClientName, tvClientPhone, tvClientEmail, tvClientAddress;
    private TextView tvWorkDescription, tvLocation, tvStatus, tvCreatedDate;
    private TextView tvStartDate, tvCompletionDate, tvTotalCost;
    private TextView tvServiceType;

    private MaterialCardView cardClientInfo, cardProjectInfo, cardCost, cardActions, cardPhotos;
    private LinearLayout layoutPhotos;

    private Button btnCreateProposal, btnMarkComplete, btnCancelProject;

    private FirebaseFirestore db;
    private WorkerProjectModel project;
    private String projectId;

    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    //private SimpleDateFormat timeFormat = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_proposal_details);

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
        // Client info
        tvClientName = findViewById(R.id.tvClientName);
        tvClientPhone = findViewById(R.id.tvClientPhone);
        tvClientEmail = findViewById(R.id.tvClientEmail);
        tvClientAddress = findViewById(R.id.tvClientAddress);

        // Project info
        tvServiceType = findViewById(R.id.tvServiceType);
        tvWorkDescription = findViewById(R.id.tvWorkDescription);
        tvLocation = findViewById(R.id.tvLocation);
        tvStatus = findViewById(R.id.tvStatus);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);

        // Dates
        tvStartDate = findViewById(R.id.tvStartDate);
        tvCompletionDate = findViewById(R.id.tvCompletionDate);

        // Cost
        tvTotalCost = findViewById(R.id.tvTotalCost);

        // Cards
        cardClientInfo = findViewById(R.id.cardClientInfo);
        cardProjectInfo = findViewById(R.id.cardProjectInfo);

        cardCost = findViewById(R.id.cardCost);
        cardActions = findViewById(R.id.cardActions);
        cardPhotos = findViewById(R.id.cardPhotos);
        layoutPhotos = findViewById(R.id.layoutPhotos);

        // Buttons
        btnCreateProposal = findViewById(R.id.btnCreateProposal);
        btnMarkComplete = findViewById(R.id.btnMarkComplete);
        btnCancelProject = findViewById(R.id.btnCancelProject);

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
        btnCreateProposal.setOnClickListener(v -> createProposal());
        btnMarkComplete.setOnClickListener(v -> showCompleteDialog());
        btnCancelProject.setOnClickListener(v -> showCancelDialog());
    }

    private void loadProjectDetails() {
        Log.d(TAG, "Loading project from BookingOrder: " + projectId);

        db.collection("BookingOrder")
                .document(projectId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Document found: " + documentSnapshot.getData());

                        project = new WorkerProjectModel();

                        // Set IDs
                        project.setProjectId(documentSnapshot.getId());
                        project.setClientId(documentSnapshot.getString("userId"));
                        project.setWorkerId(documentSnapshot.getString("workerId"));

                        // Client Information
                        project.setClientName(documentSnapshot.getString("Name"));
                        project.setClientPhone(documentSnapshot.getString("Mobile Number"));
                        project.setClientEmail(documentSnapshot.getString("Email"));
                        project.setClientAddress(documentSnapshot.getString("Home_Address"));

                        // Project Information
                        project.setServiceType(documentSnapshot.getString("Service_Type"));
                        project.setWorkDescription(documentSnapshot.getString("Description"));
                        project.setLocation(documentSnapshot.getString("Site_Address"));
                        project.setStatus(documentSnapshot.getString("status"));

                        // Budget
                        String budgetStr = documentSnapshot.getString("Budget");
                        if (budgetStr != null && !budgetStr.isEmpty()) {
                            try {
                                project.setTotalCost(Double.parseDouble(budgetStr));
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error parsing budget: " + budgetStr, e);
                                project.setTotalCost(0);
                            }
                        }

                        // Timestamps
                        String dateTime = documentSnapshot.getString("Date & Time");
                        if (dateTime != null) {
                            project.setCreatedAt(dateTime);
                        }

                        // Photos
                        Object photosObj = documentSnapshot.get("photos");
                        if (photosObj instanceof List) {
                            List<String> photosList = (List<String>) photosObj;
                            project.setPhotos(photosList);
                            Log.d(TAG, "Found " + photosList.size() + " photos");
                        }

                        displayProjectDetails();

                    } else {
                        Log.e(TAG, "Document not found");
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
        // Client info
        tvClientName.setText(project.getClientName() != null ? project.getClientName() : "N/A");
        tvClientPhone.setText(project.getClientPhone() != null ? project.getClientPhone() : "Not provided");
        tvClientEmail.setText(project.getClientEmail() != null ? project.getClientEmail() : "Not provided");
        tvClientAddress.setText(project.getClientAddress() != null ? project.getClientAddress() : "Not provided");

        // Project info
        tvServiceType.setText(project.getServiceType() != null ? project.getServiceType() : "Construction");
        tvWorkDescription.setText(project.getWorkDescription() != null ? project.getWorkDescription() : "No description");
        tvLocation.setText(project.getLocation() != null ? project.getLocation() : "Location not specified");

        // Status
        String status = project.getStatus() != null ? project.getStatus() : "pending";
        tvStatus.setText(getStatusText(status));
        tvStatus.setBackgroundResource(getStatusBackground(status));

        // Created date
        if (project.getCreatedAt() != null) {
            tvCreatedDate.setText(project.getCreatedAt());
        } else {
            tvCreatedDate.setText("Recently");
        }



        // Budget/Cost
        if (project.getTotalCost() > 0) {
            cardCost.setVisibility(View.VISIBLE);
            tvTotalCost.setText("₱" + currencyFormat.format(project.getTotalCost()));
        } else {
            cardCost.setVisibility(View.GONE);
        }

        // Display photos
        displayPhotos();

        // Update action buttons based on status
        updateActionButtons(status);
    }

    private void displayPhotos() {
        if (project.getPhotos() != null && !project.getPhotos().isEmpty()) {
            cardPhotos.setVisibility(View.VISIBLE);
            layoutPhotos.removeAllViews();

            for (String photoUrl : project.getPhotos()) {
                ImageView imageView = new ImageView(this);

                // Set layout params: 200dp x 200dp with 8dp margin
                int size = (int) (200 * getResources().getDisplayMetrics().density);
                int margin = (int) (8 * getResources().getDisplayMetrics().density);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.setMargins(margin, 0, margin, 0);
                imageView.setLayoutParams(params);

                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setBackgroundColor(Color.parseColor("#E0E0E0"));

                // Load image with Glide
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_error)
                        .into(imageView);

                // Click to view full size
                imageView.setOnClickListener(v -> {
                    // TODO: Open full screen image viewer
                    Toast.makeText(this, "Photo viewer coming soon", Toast.LENGTH_SHORT).show();
                });

                layoutPhotos.addView(imageView);
            }

            Log.d(TAG, "Displayed " + project.getPhotos().size() + " photos");
        } else {
            cardPhotos.setVisibility(View.GONE);
            Log.d(TAG, "No photos to display");
        }
    }

    private void updateActionButtons(String status) {
        if ("pending".equals(status)) {
            cardActions.setVisibility(View.VISIBLE);
            btnCreateProposal.setVisibility(View.VISIBLE);
            btnMarkComplete.setVisibility(View.GONE);
            btnCancelProject.setVisibility(View.VISIBLE);
        } else if ("active".equals(status)) {
            cardActions.setVisibility(View.VISIBLE);
            btnCreateProposal.setVisibility(View.GONE);
            btnMarkComplete.setVisibility(View.VISIBLE);
            btnCancelProject.setVisibility(View.GONE);
        } else {
            // Completed or cancelled - hide all actions
            cardActions.setVisibility(View.GONE);
        }
    }

    private void createProposal() {
        Intent intent = new Intent(this, ProjectCostQuote.class);
        intent.putExtra("projectId", projectId);
        intent.putExtra("clientId", project.getClientId());
        intent.putExtra("clientName", project.getClientName());
        intent.putExtra("workDescription", project.getWorkDescription());
        startActivity(intent);
    }

    private void showCompleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Mark as Completed")
                .setMessage("Is this project completed? This will mark the project as finished.")
                .setPositiveButton("Mark Complete", (dialog, which) -> completeProject())
                .setNegativeButton("Not Yet", null)
                .show();
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Project")
                .setMessage("Are you sure you want to cancel this project? This action cannot be undone.")
                .setPositiveButton("Cancel Project", (dialog, which) -> cancelProject())
                .setNegativeButton("Keep Project", null)
                .show();
    }

    private void completeProject() {
        Log.d(TAG, "Marking project as completed: " + projectId);

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

    private String getStatusText(String status) {
        if (status == null) return "Unknown";

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
        if (status == null) return R.drawable.bg_status_pending;

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