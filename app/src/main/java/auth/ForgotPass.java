package auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ConstructionApp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import models.UserModel;

public class ForgotPass extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEt;
    private Button resetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        mAuth = FirebaseAuth.getInstance();

        emailEt = findViewById(R.id.emailEt);
        resetBtn = findViewById(R.id.resetBtn);
        ImageView backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());

        resetBtn.setOnClickListener(v -> sendResetEmail());
    }

    private void sendResetEmail() {
        String email = emailEt.getText().toString().trim();

        // 🔥 Email validation
        if (email.isEmpty()) {
            emailEt.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.setError("Enter a valid email address");
            return;
        }

        resetBtn.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    resetBtn.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(
                                this,
                                "Password reset link sent. Check your email.",
                                Toast.LENGTH_LONG
                        ).show();
                        pushNotification();
                        startActivity(new Intent(this, Login.class));
                        finish();

                    } else {
                        String errorMsg = "Something went wrong. Try again.";

                        if (task.getException() != null) {
                            errorMsg = task.getException().getMessage();
                        }

                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
    public static void pushNotification() {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("toUserId", uid);
        data.put("title", "CrewUp Email Reset!");
        data.put("message", "Password reset link is sent to your Gmail account.");
        data.put("type", "system");
        data.put("timestamp", Timestamp.now());
        data.put("read", false);

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .add(data);
    }
}
