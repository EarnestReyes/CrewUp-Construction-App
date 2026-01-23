package com.example.ConstructionApp;

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
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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

    FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final int LOCATION_REQUEST_CODE = 100;

    private FusedLocationProviderClient fusedLocationClient;
    private String userAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
                            saveUserToFirestore(user, emailTxt);
                            Toast.makeText(SignUp.this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                            showLocationDialog();

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

                            username.setText(null);
                            email.setText(null);
                            password.setText(null);
                            confirmpass.setText(null);

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
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void showLocationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Location Permission")
                .setMessage("Do you give this app permission to access your location?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    Toast.makeText(this, "Location Enabled", Toast.LENGTH_SHORT).show();
                    requestLocationPermission();
                })
                .setNegativeButton("No", (dialog, which) ->
                        Toast.makeText(this, "Location Disabled", Toast.LENGTH_SHORT).show())
                .show();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE
            );
        } else {
            getUserLocation();
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        userAddress = getAddressFromLocation(lat, lng);

                        saveLocationToFirestore(userAddress, lat, lng);

                        Log.d("LOCATION", userAddress);
                    } else {
                        Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private String getAddressFromLocation(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getLocality() + ", " + address.getAdminArea();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown location";
    }

    private void saveUserToFirestore(String username, String email) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("createdAt", System.currentTimeMillis());
        user.put("location", userAddress);

        db.collection("users")
                .document(uid)
                .set(user);
    }

    private void saveLocationToFirestore(String address, double lat, double lng) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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


}