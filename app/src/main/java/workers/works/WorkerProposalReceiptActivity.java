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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ConstructionApp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Worker's Proposal Receipt Activity
 * Shows cost breakdown, VAT, grand total, and calculates 10% platform fee from labor
 * Deducts fee from worker's wallet balance when marking project complete
 */
public class WorkerProposalReceiptActivity extends AppCompatActivity {

    private static final String TAG = "WorkerProposalReceipt";
    private static final double PLATFORM_FEE_RATE = 0.10; // 10% of labor cost
    private static final double MINIMUM_BALANCE = -200.0; // Minimum allowed balance

    // Client info
    private TextView tvClientName, tvClientPhone, tvClientEmail;

    // Work details
    private TextView tvWorkDescription, tvNotes, tvStatus;

    // Cost breakdown
    private TextView tvTotalMaterials, tvTotalLabor, tvTotalMisc;
    private TextView tvSubtotal, tvVat, tvGrandTotal;

    // Wallet info
    private TextView tvWalletBalance, tvDeductionAmount;

    // Cards
    private MaterialCardView cardActions, cardWallet, cardStatus;

    // Actions
    private Button btnMarkComplete;

    private FirebaseFirestore db;
    private String proposalId;
    private String workerId;

    // Cost values
    private double materialsCost = 0.0;
    private double laborCost = 0.0;
    private double miscCost = 0.0;
    private double subtotal = 0.0;
    private double vat = 0.0;
    private double grandTotal = 0.0;
    private double platformFee = 0.0; // 10% of labor

    // Wallet
    private double currentBalance = 0.0;

    // Status
    private String currentStatus = "";

    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_proposal_receipt);

        db = FirebaseFirestore.getInstance();
        proposalId = getIntent().getStringExtra("proposalId");

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            workerId = auth.getCurrentUser().getUid();
        }

        if (proposalId == null) {
            Toast.makeText(this, "Invalid proposal", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadWorkerBalance();
        loadProposalDetails();
    }

    private void initViews() {
        // Client info
        tvClientName = findViewById(R.id.tvClientName);
        tvClientPhone = findViewById(R.id.tvClientPhone);
        tvClientEmail = findViewById(R.id.tvClientEmail);

        // Work details
        tvWorkDescription = findViewById(R.id.tvWorkDescription);
        tvNotes = findViewById(R.id.tvNotes);
        tvStatus = findViewById(R.id.tvStatus);

        // Cost breakdown
        tvTotalMaterials = findViewById(R.id.tvTotalMaterials);
        tvTotalLabor = findViewById(R.id.tvTotalLabor);
        tvTotalMisc = findViewById(R.id.tvTotalMisc);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvVat = findViewById(R.id.tvVat);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);

        // Wallet
        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        tvDeductionAmount = findViewById(R.id.tvDeductionAmount);

        // Cards
        cardActions = findViewById(R.id.cardActions);
        cardWallet = findViewById(R.id.cardWallet);
        cardStatus = findViewById(R.id.cardStatus);

        // Button
        btnMarkComplete = findViewById(R.id.btnMarkComplete);
        if (btnMarkComplete != null) {
            btnMarkComplete.setOnClickListener(v -> showCompleteDialog());
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
     * 🔥 LOAD WORKER'S WALLET BALANCE
     */
    private void loadWorkerBalance() {
        if (workerId == null) return;

        Log.d(TAG, "Loading wallet balance for worker: " + workerId);

        db.collection("users")
                .document(workerId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double balance = doc.getDouble("balance");
                        currentBalance = balance != null ? balance : 0.0;

                        Log.d(TAG, "Current wallet balance: " + currentBalance);
                        updateWalletDisplay();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading wallet balance", e);
                    currentBalance = 0.0;
                    updateWalletDisplay();
                });
    }

    /**
     * 🔥 LOAD PROPOSAL FROM WORKERINPUT COLLECTION
     */
    private void loadProposalDetails() {
        Log.d(TAG, "Loading proposal: " + proposalId);

        db.collection("WorkerInput")
                .document(proposalId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Proposal not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Get client info
                    String clientName = doc.getString("clientName");
                    String clientPhone = doc.getString("clientPhone");
                    String clientEmail = doc.getString("clientEmail");

                    // Get work description
                    String workDesc = doc.getString("workerDescription");
                    if (workDesc == null) workDesc = doc.getString("workDescription");

                    // Get status
                    currentStatus = doc.getString("status");
                    if (currentStatus == null) currentStatus = "pending";

                    // 🔥 GET COST BREAKDOWN FROM FIREBASE
                    Double materials = doc.getDouble("totalMaterials");
                    Double labor = doc.getDouble("totalLabor");
                    Double misc = doc.getDouble("totalMisc");
                    Double total = doc.getDouble("totalCost");
                    Double vatAmount = doc.getDouble("vat");
                    Double grand = doc.getDouble("grandTotal");

                    materialsCost = materials != null ? materials : 0.0;
                    laborCost = labor != null ? labor : 0.0;
                    miscCost = misc != null ? misc : 0.0;
                    subtotal = total != null ? total : (materialsCost + laborCost + miscCost);
                    vat = vatAmount != null ? vatAmount : (subtotal * 0.12);
                    grandTotal = grand != null ? grand : (subtotal + vat);

                    // 🔥 CALCULATE 10% PLATFORM FEE FROM LABOR COST
                    platformFee = laborCost * PLATFORM_FEE_RATE;

                    Log.d(TAG, String.format("Costs - Materials: %.2f, Labor: %.2f, Misc: %.2f",
                            materialsCost, laborCost, miscCost));
                    Log.d(TAG, String.format("Totals - Subtotal: %.2f, VAT: %.2f, Grand: %.2f",
                            subtotal, vat, grandTotal));
                    Log.d(TAG, String.format("Platform Fee (10%% of labor): %.2f", platformFee));

                    // Display everything
                    displayProposalDetails(clientName, clientPhone, clientEmail, workDesc);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading proposal", e);
                    Toast.makeText(this, "Failed to load proposal: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayProposalDetails(String clientName, String clientPhone,
                                        String clientEmail, String workDesc) {
        // Client info
        tvClientName.setText(clientName != null ? clientName : "N/A");
        tvClientPhone.setText(clientPhone != null ? clientPhone : "N/A");
        tvClientEmail.setText(clientEmail != null ? clientEmail : "N/A");

        // Work description
        tvWorkDescription.setText(workDesc != null ? workDesc : "No description");

        // Status
        tvStatus.setText(getStatusText(currentStatus));
        tvStatus.setBackgroundResource(getStatusBackground(currentStatus));

        // 🔥 COST BREAKDOWN
        tvTotalMaterials.setText("₱" + currencyFormat.format(materialsCost));
        tvTotalLabor.setText("₱" + currencyFormat.format(laborCost));
        tvTotalMisc.setText("₱" + currencyFormat.format(miscCost));
        tvSubtotal.setText("₱" + currencyFormat.format(subtotal));
        tvVat.setText("₱" + currencyFormat.format(vat));
        tvGrandTotal.setText("₱" + currencyFormat.format(grandTotal));

        // Update wallet display with platform fee
        updateWalletDisplay();

        // Show/hide action buttons based on status
        updateActionButtons();
    }

    /**
     * 🔥 UPDATE WALLET DISPLAY WITH PLATFORM FEE
     */
    private void updateWalletDisplay() {
        if (tvWalletBalance != null) {
            tvWalletBalance.setText("₱" + currencyFormat.format(currentBalance));
        }

        if (tvDeductionAmount != null) {
            tvDeductionAmount.setText("₱" + currencyFormat.format(platformFee));
        }

        // Show wallet card only if status is active
        if (cardWallet != null) {
            if ("active".equals(currentStatus)) {
                cardWallet.setVisibility(View.VISIBLE);
            } else {
                cardWallet.setVisibility(View.GONE);
            }
        }
    }

    private void updateActionButtons() {
        if (cardActions == null) return;

        // Only show "Mark Complete" button if status is active
        if ("active".equals(currentStatus)) {
            cardActions.setVisibility(View.VISIBLE);
        } else {
            cardActions.setVisibility(View.GONE);
        }
    }

    /**
     * 🔥 SHOW DIALOG TO CONFIRM PROJECT COMPLETION
     */
    private void showCompleteDialog() {
        // Calculate new balance after fee deduction
        double newBalance = currentBalance - platformFee;

        // Check if balance will go below minimum threshold
        if (newBalance < MINIMUM_BALANCE) {
            new AlertDialog.Builder(this)
                    .setTitle("Insufficient Balance")
                    .setMessage(String.format(
                            "Cannot complete project.\n\n" +
                                    "Current Balance: ₱%.2f\n" +
                                    "Platform Fee (10%% of labor): ₱%.2f\n" +
                                    "New Balance: ₱%.2f\n\n" +
                                    "Your balance cannot go below ₱%.2f.\n" +
                                    "Please top up your wallet first.",
                            currentBalance, platformFee, newBalance, MINIMUM_BALANCE))
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Complete Project")
                .setMessage(String.format(
                        "Mark this project as complete?\n\n" +
                                "Platform Fee (10%% of ₱%.2f labor): ₱%.2f\n" +
                                "Current Balance: ₱%.2f\n" +
                                "New Balance: ₱%.2f",
                        laborCost, platformFee, currentBalance, newBalance))
                .setPositiveButton("Complete", (d, w) -> completeProject())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * 🔥 MARK PROJECT COMPLETE AND DEDUCT PLATFORM FEE FROM WALLET
     */
    private void completeProject() {
        if (workerId == null) {
            Toast.makeText(this, "Worker ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Completing project and deducting platform fee: ₱" + platformFee);

        // Start Firestore batch operation
        db.runTransaction(transaction -> {
            // 1. Update proposal status to completed
            transaction.update(
                    db.collection("WorkerInput").document(proposalId),
                    "status", "completed"
            );

            // 2. Deduct platform fee from worker's balance
            transaction.update(
                    db.collection("users").document(workerId),
                    "balance", FieldValue.increment(-platformFee)
            );

            // 3. Log the transaction
            Map<String, Object> transactionLog = new HashMap<>();
            transactionLog.put("userId", workerId);
            transactionLog.put("proposalId", proposalId);
            transactionLog.put("type", "platform_fee");
            transactionLog.put("amount", -platformFee);
            transactionLog.put("laborCost", laborCost);
            transactionLog.put("description", "10% platform fee from labor cost");
            transactionLog.put("timestamp", com.google.firebase.Timestamp.now());
            transactionLog.put("previousBalance", currentBalance);
            transactionLog.put("newBalance", currentBalance - platformFee);

            transaction.set(
                    db.collection("transactions").document(),
                    transactionLog
            );

            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Project completed successfully, fee deducted");
            Toast.makeText(this,
                    "Project completed! Platform fee of ₱" + currencyFormat.format(platformFee) +
                            " deducted from wallet.",
                    Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error completing project", e);
            Toast.makeText(this, "Failed to complete project: " + e.getMessage(),
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