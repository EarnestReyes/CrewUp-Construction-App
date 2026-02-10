package auth;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_REQUEST_CODE = 100;
    private static final String CHANNEL_ID = "crew_up_channel";

    private String userAddress = "";

    private TextInputEditText username, email, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    v.setPadding(
                            insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                            insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                            insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                            insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                    );
                    return insets;
                });

        username = findViewById(R.id.edtUsername);
        email = findViewById(R.id.edtEmail);
        password = findViewById(R.id.edtPassword);
        confirmPassword = findViewById(R.id.edtConfirmPassword);

        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView txtLogin = findViewById(R.id.txtLogin);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnSignUp.setOnClickListener(v -> registerUser());

        txtLogin.setOnClickListener(v ->
                startActivity(new Intent(this, Login.class)));
    }
    private void registerUser() {

        String rawName = username.getText().toString().trim();
        String emailTxt = email.getText().toString().trim();
        String passTxt = password.getText().toString().trim();
        String confirmTxt = confirmPassword.getText().toString().trim();

        if (rawName.isEmpty() || emailTxt.isEmpty()
                || passTxt.isEmpty() || confirmTxt.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!passTxt.equals(confirmTxt)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passTxt.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        String formattedName = formatName(rawName);

        mAuth.createUserWithEmailAndPassword(emailTxt, passTxt)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        saveUserToFirestore(formattedName, emailTxt);
                        createNotification(formattedName);
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
    private String formatName(String name) {
        String[] parts = name.split("\\s+");
        for (int i = 0; i < parts.length && i < 2; i++) {
            parts[i] = parts[i].substring(0, 1).toUpperCase()
                    + parts[i].substring(1).toLowerCase();
        }
        return String.join(" ", parts);
    }
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

        db.collection("users")
                .document(user.getUid())
                .set(data);
    }
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
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        userAddress = getAddressFromLocation(lat, lng);
                        saveLocationToFirestore(userAddress, lat, lng);
                    }
                    goToUserDetails();
                });
    }
    private String getAddressFromLocation(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses =
                    geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address a = addresses.get(0);
                return a.getLocality() + ", " + a.getAdminArea();
            }
        } catch (IOException e) {
            Log.e("GEO", e.getMessage());
        }
        return "Unknown location";
    }

    private void saveLocationToFirestore(String address, double lat, double lng) {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("location", address);
        data.put("lat", lat);
        data.put("lng", lng);
        data.put("locationUpdatedAt", System.currentTimeMillis());

        db.collection("users")
                .document(user.getUid())
                .update(data);
    }

    private void goToUserDetails() {
        Intent intent = new Intent(this, UserDetails.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

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
