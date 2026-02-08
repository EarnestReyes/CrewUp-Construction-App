package auth;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ConstructionApp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

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

    private TextInputEditText username, email, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
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
        txtLogin.setOnClickListener(v -> startActivity(new Intent(this, Login.class)));
        btnSignUp.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String name = username.getText().toString().trim();
        String emailTxt = email.getText().toString().trim();
        String passTxt = password.getText().toString().trim();
        String confirmTxt = confirmPassword.getText().toString().trim();

        if (name.isEmpty() || emailTxt.isEmpty()
                || passTxt.isEmpty() || confirmTxt.isEmpty()) {
            toast("All fields are required");
            return;
        }

        if (!passTxt.equals(confirmTxt)) {
            toast("Passwords do not match");
            return;
        }

        if (passTxt.length() < 6) {
            toast("Password must be at least 6 characters");
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailTxt, passTxt)
                .addOnSuccessListener(authResult -> {
                    String formattedName = formatName(name);
                    saveUserToFirestore(formattedName, emailTxt);
                    createNotification(name);
                    showLocationDialog();
                })
                .addOnFailureListener(e ->
                        toast(e.getMessage()));
    }
    private void saveUserToFirestore(String username, String email) {
        String role = getIntent().getStringExtra("client");
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("Role", role);
        data.put("username_lower", username.toLowerCase());
        data.put("email", email);
        data.put("createdAt", System.currentTimeMillis());

        db.collection("users")
                .document(user.getUid())
                .set(data, SetOptions.merge());
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
            fetchAndSaveLocation();
        }
    }

    private void fetchAndSaveLocation() {

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        saveLocationAsync(location);
                    }
                    goToUserDetails();
                });
    }

    private void saveLocationAsync(Location location) {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        double lat = location.getLatitude();
        double lng = location.getLongitude();

        new Thread(() -> {
            String address = "Unknown location";

            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(lat, lng, 1);
                if (list != null && !list.isEmpty()) {
                    Address a = list.get(0);
                    address = a.getLocality() + ", " + a.getAdminArea();
                }
            } catch (IOException ignored) {}

            String finalAddress = address;

            runOnUiThread(() -> {
                Map<String, Object> data = new HashMap<>();
                data.put("lat", lat);
                data.put("lng", lng);
                data.put("location", finalAddress);
                data.put("locationUpdatedAt", System.currentTimeMillis());
                data.put("shareLocation", true);

                db.collection("users")
                        .document(user.getUid())
                        .set(data, SetOptions.merge());

                Log.d("LOCATION_DEBUG", "Saved: " + finalAddress);
            });

        }).start();
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
                        .setContentTitle("Account Created")
                        .setContentText("Welcome " + name + "!");

        manager.notify(1, builder.build());
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchAndSaveLocation();
        } else {
            goToUserDetails();
        }

    }
    private String formatName(String name) {
        if (name == null || name.trim().isEmpty()) return "";

        String[] parts = name.trim().toLowerCase().split("\\s+");
        StringBuilder formatted = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                formatted.append(
                        part.substring(0, 1).toUpperCase()
                                + part.substring(1)
                ).append(" ");
            }
        }

        return formatted.toString().trim();
    }
}
