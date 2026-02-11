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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import workers.app.MainActivity;

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
        btnSubmit.setOnClickListener(v -> saveAllDataToFirestore());
    }

    private void saveAllDataToFirestore() {
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

        if (topUpAmount <= 200) {
            topUpInput.setError("Amount must be greater than 200");
            return;
        }

        String uid = currentUser.getUid();

        // Get all data passed from WorkerDetails
        String email = getIntent().getStringExtra("email");
        String location = getIntent().getStringExtra("location");
        double lat = getIntent().getDoubleExtra("lat", 0.0);
        double lng = getIntent().getDoubleExtra("lng", 0.0);

        String firstName = getIntent().getStringExtra("firstName");
        String lastName = getIntent().getStringExtra("lastName");
        String middleInitial = getIntent().getStringExtra("middleInitial");
        String fullName = getIntent().getStringExtra("fullName");

        // Get expertise list
        ArrayList<String> expertiseList = getIntent().getStringArrayListExtra("expertiseList");
        String address = getIntent().getStringExtra("address");

        String birthday = getIntent().getStringExtra("birthday");
        String gender = getIntent().getStringExtra("gender");
        String mobileNumber = getIntent().getStringExtra("mobileNumber");
        String social = getIntent().getStringExtra("social");

        // Create complete user data map with wallet balance
        Map<String, Object> user = new HashMap<>();

        // Data from WorkerSignUp
        user.put("email", email != null ? email : "");
        user.put("Role", "worker");
        user.put("createdAt", System.currentTimeMillis());

        // Location data (if available)
        if (location != null && !location.isEmpty()) {
            user.put("location", location);
            user.put("lat", lat);
            user.put("lng", lng);
            user.put("locationUpdatedAt", System.currentTimeMillis());
        }

        // Name fields
        user.put("FirstName", firstName != null ? firstName : "");
        user.put("LastName", lastName != null ? lastName : "");
        user.put("MiddleInitial", middleInitial != null ? middleInitial : "");
        user.put("username", fullName != null ? fullName : "");
        user.put("username_lower", fullName != null ? fullName.toLowerCase() : "");

        // Worker-specific fields - store expertise as array
        user.put("Expertise", expertiseList != null ? expertiseList : new ArrayList<String>());
        user.put("Address", address != null ? address : "");

        // Personal details
        user.put("Birthday", birthday != null ? birthday : "");
        user.put("Gender", gender != null ? gender : "");
        user.put("Mobile Number", mobileNumber != null ? mobileNumber : "");
        user.put("Social", social != null ? social : "");

        // Wallet balance
        user.put("balance", topUpAmount);
        user.put("lastUpdated", com.google.firebase.Timestamp.now());

        // Save all data at once
        db.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Worker profile and wallet setup successful!", Toast.LENGTH_SHORT).show();
                    Intent in = new Intent(this, MainActivity.class);
                    startActivity(in);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Setup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}