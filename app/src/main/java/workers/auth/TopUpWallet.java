package workers.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ConstructionApp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class TopUpWallet extends AppCompatActivity {

    private TextInputEditText topUpInput;
    private Button btnSubmit;
    ImageButton btnBack;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wallet_topup);

        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        topUpInput = findViewById(R.id.WalletTopUp);
        btnBack = findViewById(R.id.btnBack);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> saveTopUpToWallet());
    }

    private void saveTopUpToWallet() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String input = topUpInput.getText() != null
                ? topUpInput.getText().toString().trim()
                : "";

        if (input.isEmpty()) {
            topUpInput.setError("Enter amount");
            return;
        }

        double topUpAmount;
        try {
            topUpAmount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            topUpInput.setError("Invalid amount");
            return;
        }

        if (topUpAmount <= 0) {
            topUpInput.setError("Amount must be greater than 0");
            return;
        }

        String uid = currentUser.getUid();

        DocumentReference workerWalletRef = db
                .collection("users")
                .document(uid);

        workerWalletRef.get().addOnSuccessListener(snapshot -> {

            double currentBalance = 0.0;
            if (snapshot.exists() && snapshot.getDouble("balance") != null) {
                currentBalance = snapshot.getDouble("balance");
            }

            double newBalance = currentBalance + topUpAmount;

            Map<String, Object> walletUpdate = new HashMap<>();
            walletUpdate.put("balance", newBalance);
            walletUpdate.put("lastUpdated", com.google.firebase.Timestamp.now());

            workerWalletRef.set(walletUpdate, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Top-up successful!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to top up: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load wallet", Toast.LENGTH_SHORT).show()
        );
    }
}
