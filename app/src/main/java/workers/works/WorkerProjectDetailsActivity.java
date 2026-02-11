package workers.works;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import workers.works.invoice.ProjectCostQuote;


public class WorkerProjectDetailsActivity extends AppCompatActivity {

    private TextView tvClientName, tvClientPhone, tvClientEmail, tvClientAddress;
    private TextView tvWorkDescription, tvLocation, tvStatus, tvCreatedDate;
    private TextView  tvTotalCost, tvNotes;

    private MaterialCardView cardClientInfo, cardProjectInfo, cardDates, cardCost, cardActions, cardPhotos;
    private LinearLayout layoutPhotos;
    private Button btnCreateProposal, btnMarkComplete, btnContactClient;
    private Button btnCancelProject;

    private FirebaseFirestore db;
    private WorkerProjectModel project;
    private String projectId;

    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

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

        initializeViews();
        setupToolbar();
        loadProjectDetails();

    }

    private void initializeViews() {

        tvClientName = findViewById(R.id.tvClientName);
        tvClientPhone = findViewById(R.id.tvClientPhone);
        tvClientEmail = findViewById(R.id.tvClientEmail);
        tvClientAddress = findViewById(R.id.tvClientAddress);

        tvWorkDescription = findViewById(R.id.tvWorkDescription);
        tvLocation = findViewById(R.id.tvLocation);
        tvStatus = findViewById(R.id.tvStatus);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        tvNotes = findViewById(R.id.tvNotes);


        tvTotalCost = findViewById(R.id.tvTotalCost);
        layoutPhotos = findViewById(R.id.layoutPhotos);


        cardClientInfo = findViewById(R.id.cardClientInfo);
        cardProjectInfo = findViewById(R.id.cardProjectInfo);
        cardDates = findViewById(R.id.cardDates);
        cardCost = findViewById(R.id.cardCost);
        cardActions = findViewById(R.id.cardActions);
        cardPhotos = findViewById(R.id.cardPhotos);

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
        db.collection("BookingOrder")
                .document(projectId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        project = new WorkerProjectModel();

                        project.setProjectId(documentSnapshot.getId());
                        project.setClientName(documentSnapshot.getString("Name"));
                        project.setClientPhone(documentSnapshot.getString("Mobile Number"));
                        project.setClientEmail(documentSnapshot.getString("Email"));
                        project.setWorkDescription(documentSnapshot.getString("Description"));
                        project.setClientAddress(documentSnapshot.getString("Home_Address"));
                        project.setLocation(documentSnapshot.getString("Site_Address"));
                        project.setStatus(documentSnapshot.getString("status"));
                        project.setServiceType(documentSnapshot.getString("Service_Type"));
                        project.setCreatedAt(documentSnapshot.getString("Date & Time"));
                        project.setWorkerId(documentSnapshot.getString("workerId"));
                        project.setClientId(documentSnapshot.getString("userId"));

                        Object photosObj = documentSnapshot.get("photos");
                        if (photosObj != null) {
                            if (photosObj instanceof List) {

                                List<String> photosList = (List<String>) documentSnapshot.get("photos");
                                project.setPhotos(photosList);
                            }
                        }

                        String budgetStr = documentSnapshot.getString("Budget");
                        if (budgetStr != null) {
                            try {
                                project.setTotalCost(Double.parseDouble(budgetStr));
                            } catch (Exception e) {
                                project.setTotalCost(0);
                            }
                        }

                        displayProjectDetails();
                        displayPhotos();

                    } else {
                        Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading project: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
    private void displayPhotos() {
        // Check if photos exist
        if (project.getPhotos() != null && !project.getPhotos().isEmpty()) {
            cardPhotos.setVisibility(View.VISIBLE);
            layoutPhotos.removeAllViews(); // Clear old photos

            // 🔥 LOOP through each photo URL
            for (int i = 0; i < project.getPhotos().size(); i++) {
                String photoUrl = project.getPhotos().get(i);

                // Create ImageView
                ImageView imageView = new ImageView(this);

                // Set size (200dp x 200dp)
                int size = (int) (200 * getResources().getDisplayMetrics().density);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                imageView.setLayoutParams(params);

                // Load image with Glide
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_error)
                        .into(imageView);

                // Add to horizontal scroll layout
                layoutPhotos.addView(imageView);
            }
        } else {
            // No photos - hide card
            cardPhotos.setVisibility(View.GONE);
        }
    }



    private void displayProjectDetails() {

        tvClientName.setText(project.getClientName());
        tvClientPhone.setText(project.getClientPhone() != null ?
                project.getClientPhone() : "Not provided");
        tvClientEmail.setText(project.getClientEmail() != null ?
                project.getClientEmail() : "Not provided");
        tvClientAddress.setText(project.getClientAddress() != null ?
                project.getClientAddress() : "Not provided");



        tvWorkDescription.setText(project.getWorkDescription());
        tvLocation.setText(project.getLocation());

        String status = project.getStatus() != null ? project.getStatus() : "pending";
        tvStatus.setText(getStatusText(status));
        tvStatus.setBackgroundResource(getStatusBackground(status));

        if (project.getCreatedAt() != null) {
            tvCreatedDate.setText(project.getCreatedAt());
        }

        if (project.getNotes() != null && !project.getNotes().isEmpty()) {
            tvNotes.setVisibility(View.VISIBLE);
            tvNotes.setText(project.getNotes());
        } else {
            tvNotes.setVisibility(View.GONE);
        }



        if (project.getTotalCost() > 0) {
            cardCost.setVisibility(View.VISIBLE);
            tvTotalCost.setText("₱" + currencyFormat.format(project.getTotalCost()));
        } else {
            cardCost.setVisibility(View.GONE);
        }


        updateActionButtons(status);
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
        db.collection("BookingOrder")
                .document(projectId)
                .update("status", "completed")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Project marked as completed!", Toast.LENGTH_SHORT).show();
                    loadProjectDetails();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error completing project: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void cancelProject() {
        db.collection("BookingOrder")
                .document(projectId)
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Project cancelled", Toast.LENGTH_SHORT).show();
                    loadProjectDetails();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error cancelling project: " + e.getMessage(),
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