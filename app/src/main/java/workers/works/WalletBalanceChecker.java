package workers.works;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;

/**
 * Utility class for checking worker wallet balance before performing operations
 * Implements business rules:
 * - Balance < -200: Cannot complete projects or book appointments
 * - Balance between 0 and -200: Can complete projects but cannot book
 * - Balance >= 0: Can do everything
 */
public class WalletBalanceChecker {

    private static final String TAG = "WalletBalanceChecker";
    private static final double MINIMUM_BALANCE_THRESHOLD = -200.0;

    private FirebaseFirestore db;
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    public WalletBalanceChecker() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Check if worker can book a new appointment
     * @param workerId Worker's user ID
     * @param context Context for showing dialogs
     * @param callback Callback with result
     */
    public void canBookAppointment(String workerId, Context context, OnBalanceCheckListener callback) {
        if (workerId == null) {
            callback.onResult(false, "User not logged in");
            return;
        }

        db.collection("Users")
                .document(workerId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double balance = doc.getDouble("walletBalance");
                        double currentBalance = balance != null ? balance : 0.0;

                        Log.d(TAG, "Checking booking eligibility. Balance: " + currentBalance);

                        if (currentBalance < 0) {
                            // Balance is negative - cannot book
                            showCannotBookDialog(context, currentBalance);
                            callback.onResult(false, "Negative balance: ₱" + currencyFormat.format(currentBalance));
                        } else {
                            // Balance is positive or zero - can book
                            callback.onResult(true, "Can book");
                        }
                    } else {
                        callback.onResult(false, "User profile not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check balance", e);
                    callback.onResult(false, "Failed to check balance: " + e.getMessage());
                });
    }

    /**
     * Check if worker can complete a project
     * @param workerId Worker's user ID
     * @param context Context for showing dialogs
     * @param callback Callback with result
     */
    public void canCompleteProject(String workerId, Context context, OnBalanceCheckListener callback) {
        if (workerId == null) {
            callback.onResult(false, "User not logged in");
            return;
        }

        db.collection("Users")
                .document(workerId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double balance = doc.getDouble("walletBalance");
                        double currentBalance = balance != null ? balance : 0.0;

                        Log.d(TAG, "Checking completion eligibility. Balance: " + currentBalance);

                        if (currentBalance <= MINIMUM_BALANCE_THRESHOLD) {
                            // Balance is too low - cannot complete
                            showCannotCompleteDialog(context, currentBalance);
                            callback.onResult(false, "Balance below threshold: ₱" + currencyFormat.format(currentBalance));
                        } else {
                            // Balance is above threshold - can complete
                            callback.onResult(true, "Can complete");
                        }
                    } else {
                        callback.onResult(false, "User profile not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check balance", e);
                    callback.onResult(false, "Failed to check balance: " + e.getMessage());
                });
    }

    /**
     * Get current wallet balance
     * @param workerId Worker's user ID
     * @param callback Callback with balance
     */
    public void getBalance(String workerId, OnBalanceRetrievedListener callback) {
        if (workerId == null) {
            callback.onBalance(0.0, false);
            return;
        }

        db.collection("Users")
                .document(workerId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double balance = doc.getDouble("walletBalance");
                        double currentBalance = balance != null ? balance : 0.0;
                        callback.onBalance(currentBalance, true);
                    } else {
                        callback.onBalance(0.0, false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get balance", e);
                    callback.onBalance(0.0, false);
                });
    }

    /**
     * Show dialog when worker cannot book due to negative balance
     */
    private void showCannotBookDialog(Context context, double balance) {
        new AlertDialog.Builder(context)
                .setTitle("❌ Cannot Book Appointment")
                .setMessage("Your wallet balance is negative. You must add funds before booking new appointments.\n\n" +
                        "Current Balance: ₱" + currencyFormat.format(balance) + "\n\n" +
                        "Please top up your wallet to continue.")
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Show dialog when worker cannot complete due to very low balance
     */
    private void showCannotCompleteDialog(Context context, double balance) {
        new AlertDialog.Builder(context)
                .setTitle("❌ Cannot Complete Project")
                .setMessage("Your wallet balance is below ₱" + currencyFormat.format(Math.abs(MINIMUM_BALANCE_THRESHOLD)) +
                        ". You cannot complete projects or book appointments until you add funds.\n\n" +
                        "Current Balance: ₱" + currencyFormat.format(balance) + "\n\n" +
                        "Please top up your wallet to continue.")
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Callback interface for balance checks
     */
    public interface OnBalanceCheckListener {
        void onResult(boolean canProceed, String message);
    }

    /**
     * Callback interface for balance retrieval
     */
    public interface OnBalanceRetrievedListener {
        void onBalance(double balance, boolean success);
    }
}