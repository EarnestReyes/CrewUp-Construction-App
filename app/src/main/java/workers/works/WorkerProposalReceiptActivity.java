package workers.works;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ConstructionApp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import clients.works.ClientProjectModel;

/**
 * Activity for workers to view their own submitted proposals
 * Shows the proposal receipt with materials, labor, misc costs, VAT, and grand total
 * Allows marking the project as complete when status is "active"
 * Implements wallet balance checking and 10% labor fee deduction
 */
public class WorkerProposalReceiptActivity extends AppCompatActivity {

    private static final String TAG = "WorkerProposalReceipt";
    private static final double VAT_RATE = 0.12; // 12% VAT
    private static final double PLATFORM_FEE_RATE = 0.10; // 10% platform fee from labor
    private static final double MINIMUM_BALANCE_THRESHOLD = -200.0; // Can't complete if balance < -200

    // Client info
    private TextView tvClientName, tvClientPhone, tvClientEmail;

    // Project info
    private TextView tvWorkDescription, tvNotes;
    private TextView tvTotalMaterials, tvTotalLabor, tvTotalMisc;
    private TextView tvSubtotal, tvVat, tvGrandTotal;
    private TextView tvStatus, tvWalletBalance, tvDeductionAmount;

    // Dates
    private TextView tvStartDate, tvCompletionDate;

    // Actions
    private MaterialCardView cardActions, cardStatus, cardWallet;
    private Button btnMarkComplete;

    private FirebaseFirestore db;
    private ClientProjectModel project;
    private String proposalId;
    private String workerId;
    private double currentBalance = 0.0;

    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_proposal_receipt);

        db = FirebaseFirestore.getInstance();
        proposalId = getIntent().getStringExtra("proposalId");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            workerId = auth.getCurrentUser().getUid();
        }

        if (proposalId == null) {
            Toast.makeText(this, "Invalid proposal ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadWorkerBalance(); // Load balance first
        loadProposalDetails();
    }

    private void initViews() {
        // Client info
        tvClientName = findViewById(R.id.tvClientName);
        tvClientPhone = findViewById(R.id.tvClientPhone);
        tvClientEmail = findViewById(R.id.tvClientEmail);

        // Project details
        tvWorkDescription = findViewById(R.id.tvWorkDescription);
        tvNotes = findViewById(R.id.tvNotes);
        tvTotalMaterials = findViewById(R.id.tvTotalMaterials);
        tvTotalLabor = findViewById(R.id.tvTotalLabor);
        tvTotalMisc = findViewById(R.id.tvTotalMisc);

        // Cost summary
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvVat = findViewById(R.id.tvVat);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);

        tvStatus = findViewById(R.id.tvStatus);

        // Wallet info
        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        tvDeductionAmount = findViewById(R.id.tvDeductionAmount);

        // Dates
        tvStartDate = findViewById(R.id.tvStartDate);
        tvCompletionDate = findViewById(R.id.tvCompletionDate);

        // Action cards
        cardActions = findViewById(R.id.cardActions);
        cardStatus = findViewById(R.id.cardStatus);
        cardWallet = findViewById(R.id.cardWallet);
        btnMarkComplete = findViewById(R.id.btnMarkComplete);

        if (btnMarkComplete != null) {
            btnMarkComplete.setOnClickListener(v -> validateAndShowCompleteDialog());
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Proposal");
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadWorkerBalance() {
        if (workerId == null) {
            Log.e(TAG, "Worker ID is null");
            return;
        }

        db.collection("Users")
                .document(workerId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double balance = doc.getDouble("walletBalance");
                        currentBalance = balance != null ? balance : 0.0;
                        updateWalletDisplay();
                        Log.d(TAG, "Current wallet balance: " + currentBalance);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load wallet balance", e);
                    Toast.makeText(this, "Failed to load wallet balance", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadProposalDetails() {
        db.collection("WorkerInput")
                .document(proposalId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Proposal not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    project = doc.toObject(ClientProjectModel.class);
                    if (project == null) {
                        Toast.makeText(this, "Invalid proposal data", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    project.setProjectId(doc.getId());

                    // Calculate and set VAT and Grand Total if not already set
                    if (project.getVat() == 0 && project.getTotalCost() > 0) {
                        double vat = project.getTotalCost() * VAT_RATE;
                        double grandTotal = project.getTotalCost() + vat;
                        project.setVat(vat);
                        project.setGrandTotal(grandTotal);

                        // Update in Firestore
                        updateVatAndGrandTotal(vat, grandTotal);
                    }

                    displayProposalDetails();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load proposal", e);
                    Toast.makeText(this, "Failed to load proposal", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateVatAndGrandTotal(double vat, double grandTotal) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("vat", vat);
        updates.put("grandTotal", grandTotal);

        db.collection("WorkerInput")
                .document(proposalId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "VAT and Grand Total updated"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update VAT/Grand Total", e));
    }

    private void displayProposalDetails() {
        // Client info
        String clientDisplay = "Client";
        if (project.getWorkerName() != null && !project.getWorkerName().isEmpty()) {
            clientDisplay = project.getWorkerName();
        }

        tvClientName.setText(clientDisplay);
        tvClientPhone.setText(nonNull(project.getWorkerPhone(), "N/A"));
        tvClientEmail.setText(nonNull(project.getWorkerEmail(), "N/A"));

        // Work description
        tvWorkDescription.setText(nonNull(project.getWorkDescription(), "No description"));

        // Cost breakdown
        tvTotalMaterials.setText("₱" + currencyFormat.format(project.getMaterialsCost()));
        tvTotalLabor.setText("₱" + currencyFormat.format(project.getLaborCost()));
        tvTotalMisc.setText("₱" + currencyFormat.format(project.getMiscCost()));

        // Subtotal, VAT, Grand Total
        if (tvSubtotal != null) {
            tvSubtotal.setText("₱" + currencyFormat.format(project.getTotalCost()));
        }

        if (tvVat != null) {
            tvVat.setText("₱" + currencyFormat.format(project.getVat()));
        }

        tvGrandTotal.setText("₱" + currencyFormat.format(project.getGrandTotal()));

        // Notes
        if (tvNotes != null) {
            if (project.getNotes() != null && !project.getNotes().isEmpty()) {
                tvNotes.setVisibility(View.VISIBLE);
                tvNotes.setText(project.getNotes());
            } else {
                tvNotes.setVisibility(View.GONE);
            }
        }

        // Dates
        if (tvStartDate != null && project.getStartDate() != null) {
            tvStartDate.setText(dateFormat.format(project.getStartDate().toDate()));
        }

        if (tvCompletionDate != null && project.getCompletionDate() != null) {
            tvCompletionDate.setText(dateFormat.format(project.getCompletionDate().toDate()));
        }

        // Status and actions
        String status = project.getStatus() != null ? project.getStatus() : "pending";
        updateStatusAndActions(status);
        updateWalletDisplay();
    }

    private void updateWalletDisplay() {
        if (cardWallet != null && project != null && "active".equals(project.getStatus())) {
            cardWallet.setVisibility(View.VISIBLE);

            if (tvWalletBalance != null) {
                tvWalletBalance.setText("₱" + currencyFormat.format(currentBalance));

                // Color code the balance
                if (currentBalance >= 0) {
                    tvWalletBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else if (currentBalance > MINIMUM_BALANCE_THRESHOLD) {
                    tvWalletBalance.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                } else {
                    tvWalletBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }

            if (tvDeductionAmount != null) {
                double deduction = calculatePlatformFee();
                tvDeductionAmount.setText("₱" + currencyFormat.format(deduction));
            }
        } else if (cardWallet != null) {
            cardWallet.setVisibility(View.GONE);
        }
    }

    private double calculatePlatformFee() {
        return project.getLaborCost() * PLATFORM_FEE_RATE;
    }

    private void updateStatusAndActions(String status) {
        // Show status
        if (cardStatus != null && tvStatus != null) {
            cardStatus.setVisibility(View.VISIBLE);
            tvStatus.setText(getStatusText(status));
            tvStatus.setBackgroundResource(getStatusBackground(status));
        }

        // Show/hide action button based on status
        if (cardActions != null && btnMarkComplete != null) {
            if ("active".equals(status)) {
                cardActions.setVisibility(View.VISIBLE);
                btnMarkComplete.setVisibility(View.VISIBLE);

                // Update button state based on balance
                if (currentBalance <= MINIMUM_BALANCE_THRESHOLD) {
                    btnMarkComplete.setEnabled(false);
                    btnMarkComplete.setText("Insufficient Balance");
                    btnMarkComplete.setAlpha(0.5f);
                } else {
                    btnMarkComplete.setEnabled(true);
                    btnMarkComplete.setText("Mark Project Complete");
                    btnMarkComplete.setAlpha(1.0f);
                }
            } else {
                cardActions.setVisibility(View.GONE);
            }
        }
    }

    private void validateAndShowCompleteDialog() {
        double platformFee = calculatePlatformFee();
        double balanceAfterDeduction = currentBalance - platformFee;

        // Check if balance is below threshold
        if (currentBalance <= MINIMUM_BALANCE_THRESHOLD) {
            showInsufficientBalanceDialog();
            return;
        }

        // Show warning if balance will be negative but above threshold
        if (balanceAfterDeduction < 0 && balanceAfterDeduction > MINIMUM_BALANCE_THRESHOLD) {
            showNegativeBalanceWarningDialog(platformFee, balanceAfterDeduction);
        } else {
            showCompleteDialog(platformFee, balanceAfterDeduction);
        }
    }

    private void showInsufficientBalanceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Insufficient Balance")
                .setMessage("Your wallet balance is below ₱" + currencyFormat.format(Math.abs(MINIMUM_BALANCE_THRESHOLD)) +
                        ". You cannot complete this project or book new appointments until you add funds to your wallet.\n\n" +
                        "Current Balance: ₱" + currencyFormat.format(currentBalance))
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showNegativeBalanceWarningDialog(double platformFee, double balanceAfterDeduction) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Balance Will Be Negative")
                .setMessage("Completing this project will make your balance negative:\n\n" +
                        "Current Balance: ₱" + currencyFormat.format(currentBalance) + "\n" +
                        "Platform Fee (10%): -₱" + currencyFormat.format(platformFee) + "\n" +
                        "Balance After: ₱" + currencyFormat.format(balanceAfterDeduction) + "\n\n" +
                        "⚠️ WARNING: You will NOT be able to book new appointments until you add funds.\n\n" +
                        "Do you want to proceed?")
                .setPositiveButton("Yes, Complete", (dialog, which) -> markProjectComplete())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void showCompleteDialog(double platformFee, double balanceAfterDeduction) {
        new AlertDialog.Builder(this)
                .setTitle("Mark Project Complete")
                .setMessage("Have you finished all work for this project?\n\n" +
                        "Platform Fee (10% of labor): ₱" + currencyFormat.format(platformFee) + "\n" +
                        "Current Balance: ₱" + currencyFormat.format(currentBalance) + "\n" +
                        "Balance After: ₱" + currencyFormat.format(balanceAfterDeduction) + "\n\n" +
                        "This will notify the client that the project is complete.")
                .setPositiveButton("Mark Complete", (dialog, which) -> markProjectComplete())
                .setNegativeButton("Not Yet", null)
                .show();
    }

    private void markProjectComplete() {
        double platformFee = calculatePlatformFee();

        // First, deduct from wallet
        deductFromWallet(platformFee, success -> {
            if (success) {
                // Then update proposal status
                db.collection("WorkerInput")
                        .document(proposalId)
                        .update("status", "completed")
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Project marked as completed!", Toast.LENGTH_SHORT).show();

                            // Update the original BookingOrder status
                            updateBookingOrderStatus();

                            // Reload to update UI
                            loadWorkerBalance();
                            loadProposalDetails();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to mark complete", e);
                            Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();

                            // Refund the deduction since status update failed
                            refundWallet(platformFee);
                        });
            } else {
                Toast.makeText(this, "Failed to process payment. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deductFromWallet(double amount, OnWalletUpdateListener listener) {
        if (workerId == null) {
            listener.onComplete(false);
            return;
        }

        db.collection("Users")
                .document(workerId)
                .update("walletBalance", FieldValue.increment(-amount))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Wallet deducted: ₱" + amount);

                    // Log transaction
                    logTransaction(amount, "Platform Fee - Project Completion");

                    listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to deduct from wallet", e);
                    listener.onComplete(false);
                });
    }

    private void refundWallet(double amount) {
        if (workerId == null) return;

        db.collection("Users")
                .document(workerId)
                .update("walletBalance", FieldValue.increment(amount))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Wallet refunded: ₱" + amount))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to refund wallet", e));
    }

    private void logTransaction(double amount, String description) {
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("workerId", workerId);
        transaction.put("amount", -amount); // Negative for deduction
        transaction.put("description", description);
        transaction.put("proposalId", proposalId);
        transaction.put("timestamp", FieldValue.serverTimestamp());
        transaction.put("type", "deduction");

        db.collection("Transactions")
                .add(transaction)
                .addOnSuccessListener(docRef -> Log.d(TAG, "Transaction logged"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to log transaction", e));
    }

    private void updateBookingOrderStatus() {
        if (project.getUserId() != null && workerId != null) {
            db.collection("BookingOrder")
                    .whereEqualTo("userId", project.getUserId())
                    .whereEqualTo("workerId", workerId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String bookingId = querySnapshot.getDocuments().get(0).getId();
                            db.collection("BookingOrder")
                                    .document(bookingId)
                                    .update("status", "completed")
                                    .addOnSuccessListener(aVoid ->
                                            Log.d(TAG, "BookingOrder status updated"))
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Failed to update BookingOrder", e));
                        }
                    });
        }
    }

    private String getStatusText(String status) {
        if (status == null) return "Unknown";

        switch (status.toLowerCase()) {
            case "pending":
                return "⏳ Pending Client Response";
            case "active":
                return "🔨 In Progress";
            case "completed":
                return "✅ Completed";
            case "cancelled":
                return "❌ Cancelled";
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

    private String nonNull(String value, String fallback) {
        return value != null ? value : fallback;
    }

    // Interface for wallet update callback
    private interface OnWalletUpdateListener {
        void onComplete(boolean success);
    }
}