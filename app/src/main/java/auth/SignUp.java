package auth;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ConstructionApp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_REQUEST_CODE = 100;
    private static final String CHANNEL_ID = "crew_up_channel";

    private TextInputEditText email, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // ===== ONLY THESE FIELDS NOW =====
        email = findViewById(R.id.edtEmail);
        password = findViewById(R.id.edtPassword);
        confirmPassword = findViewById(R.id.edtConfirmPassword);

        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView txtLogin = findViewById(R.id.txtLogin);
        ImageButton btnBack = findViewById(R.id.btnBack);

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });

        btnBack.setOnClickListener(v -> finish());

        btnSignUp.setOnClickListener(v -> registerUser());

        txtLogin.setOnClickListener(v ->
                startActivity(new Intent(this, Login.class)));
    }

    // ================= REGISTER =================

    private void registerUser() {

        String emailTxt = getSafeText(email);
        String passTxt = getSafeText(password);
        String confirmTxt = getSafeText(confirmPassword);

        if (emailTxt.isEmpty() || passTxt.isEmpty() || confirmTxt.isEmpty()) {
            Toast.makeText(this,
                    "All fields are required",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!passTxt.equals(confirmTxt)) {
            Toast.makeText(this,
                    "Passwords do not match",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (passTxt.length() < 6) {
            Toast.makeText(this,
                    "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailTxt, passTxt)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        String autoUsername = generateUsernameFromEmail(emailTxt);
                        saveUserToFirestore(autoUsername, emailTxt);
                        createNotification(autoUsername);
                        showLocationDialog();

                    } else {

                        Toast.makeText(
                                this,
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Registration failed",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private String getSafeText(TextInputEditText editText) {
        if (editText.getText() == null) return "";
        return editText.getText().toString().trim();
    }

    private String generateUsernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "user";
    }

    // ================= FIRESTORE SAVE =================

    private void saveUserToFirestore(String username, String email) {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String role = getIntent().getStringExtra("client");
        if (role == null) role = "client";

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("username_lower", username.toLowerCase());
        data.put("email", email);
        data.put("Role", role);
        data.put("createdAt", System.currentTimeMillis());
        data.put("isPinned", false);

        db.collection("users")
                .document(user.getUid())
                .set(data);
    }

    // ================= LOCATION =================

    private void showLocationDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Location Permission")
                .setMessage("Allow app to access your location?")
                .setCancelable(false)
                .setPositiveButton("Yes", (d, w) -> requestLocationPermission())
                .setNegativeButton("No", (d, w) -> goToUserDetails())
                .show();
    }

    private void requestLocationPermission() {

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);

        } else {
            getUserLocation();
        }
    }

    private void getUserLocation() {

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {

                        Map<String, Object> data = new HashMap<>();
                        data.put("lat", location.getLatitude());
                        data.put("lng", location.getLongitude());
                        data.put("locationUpdatedAt", System.currentTimeMillis());

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            db.collection("users")
                                    .document(user.getUid())
                                    .update(data);
                        }
                    }

                    goToUserDetails();
                });
    }

    private void goToUserDetails() {

        Intent intent = new Intent(this, UserDetails.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ================= NOTIFICATION =================

    private void createNotification(String name) {

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Crew Up Notifications",
                            NotificationManager.IMPORTANCE_DEFAULT
                    );

            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.crewup_logo)
                        .setContentTitle("Account Created Successfully!")
                        .setContentText("Welcome " + name + "!")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        manager.notify(1, builder.build());
    }

    // ================= PERMISSION RESULT =================

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getUserLocation();

            } else {
                goToUserDetails();
            }
        }
    }
}
