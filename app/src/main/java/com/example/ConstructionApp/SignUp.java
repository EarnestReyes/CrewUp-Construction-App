package com.example.ConstructionApp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        db = FirebaseFirestore.getInstance();

        getWindow().getInsetsController().setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        );

        View main = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    topInset,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextInputEditText username = findViewById(R.id.edtUsername);
        TextInputEditText email = findViewById(R.id.edtEmail);
        TextInputEditText password = findViewById(R.id.edtPassword);
        TextInputEditText confirmpass = findViewById(R.id.edtConfirmPassword);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        Button signin = findViewById(R.id.btnSignUp);
        TextView login = findViewById(R.id.txtLogin);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(view -> {
            finish();
        });

        signin.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String user = username.getText().toString().trim();
            String emailTxt = email.getText().toString().trim();
            String passTxt = password.getText().toString().trim();
            String confirmTxt = confirmpass.getText().toString().trim();

            if (user.isEmpty() || emailTxt.isEmpty() || passTxt.isEmpty() || confirmTxt.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!passTxt.equals(confirmTxt)) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (passTxt.length() < 6) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(emailTxt, passTxt)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            saveUserToFirestore(user, emailTxt, passTxt);
                            Toast.makeText(SignUp.this, "Registered Successfully!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            manager.cancel(1);

                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                                NotificationChannel channel = new NotificationChannel("Crew Up", "Account Created Succesfully!", NotificationManager.IMPORTANCE_DEFAULT);
                                manager.createNotificationChannel(channel);
                            }

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"test")
                                    .setSmallIcon(R.drawable.crewup_logo)
                                    .setContentTitle("Account Created Succesfully!")
                                    .setContentText("Welcome " + user + ", enjoy your journey with us!")
                                    .setStyle(new NotificationCompat.BigTextStyle()
                                            .bigText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."))
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            manager.notify(1, builder.build());

                        } else {
                            Toast.makeText(SignUp.this,
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });


        login.setOnClickListener(view -> {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        });
    }

    private void saveUserToFirestore(String username, String email, String password) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("Password", password);
        user.put("createdAt", System.currentTimeMillis());

        db.collection("users")
                .document(uid)
                .set(user);
    }

}