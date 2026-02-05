package workers.wallet;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.ConstructionApp.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Locale;

import workers.auth.TopUpWallet;

public class WalletProfile extends Fragment {

    private TextView tvWalletBalance;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration walletListener;
    private MaterialButton btnTopUp, btnWithdraw;

    public WalletProfile() {

    }

    @Nullable
    @Override
    public View onCreateView(

            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        View view = inflater.inflate(R.layout.activity_wallet_profile, container, false);
        tvWalletBalance = view.findViewById(R.id.tvWalletBalance);
        btnTopUp = view.findViewById(R.id.btnTopUp);
        btnWithdraw = view.findViewById(R.id.btnWithdraw);

        btnTopUp.setOnClickListener(v -> {
            Intent in = new Intent(getContext(), TopUpWallet.class);
            startActivity(in);
        });

        btnWithdraw.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Location Permission")
                    .setMessage("Allow app to access your location?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (d, w) -> Toast.makeText(getContext(), "Okay!", Toast.LENGTH_SHORT).show())
                    .setNegativeButton("No", (d, w) -> Toast.makeText(getContext(), "NOTED BOSS", Toast.LENGTH_SHORT).show())
                    .show();
                });


        loadWalletBalanceRealtime();

        return view;
    }

    private void loadWalletBalanceRealtime() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        DocumentReference walletRef = db.collection("users")
                .document(uid);

        walletListener = walletRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e("WalletProfile", "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Double balance = snapshot.getDouble("balance");

                if (balance == null) balance = 0.0;

                tvWalletBalance.setText("₱" + String.format(Locale.getDefault(), "%.2f", balance));
            } else {
                tvWalletBalance.setText("₱0.00");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (walletListener != null) {
            walletListener.remove();
        }
    }
}
