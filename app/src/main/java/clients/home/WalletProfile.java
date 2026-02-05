package clients.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ConstructionApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Locale;

public class WalletProfile extends AppCompatActivity {

    private TextView tvWalletBalance;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration walletListener;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wallet_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvWalletBalance = findViewById(R.id.tvWalletBalance);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadWalletBalanceRealtime();
    }

    private void loadWalletBalanceRealtime() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        DocumentReference walletRef = db.collection("wallets").document(uid);

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
    protected void onDestroy() {
        super.onDestroy();
        if (walletListener != null) {
            walletListener.remove(); // prevent memory leaks
        }
    }
}
