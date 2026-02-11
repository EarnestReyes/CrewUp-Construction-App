package auth;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import app.MainActivity;

public class UserDetails extends AppCompatActivity {
    TextInputEditText edtBirthday, mobilenum, socials,edtFirstName,edtLastName, edtMiddleInitial;
    AutoCompleteTextView edtGender;
    Button btnSubmit;
    ImageButton btnBack;

    FirebaseFirestore db;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_user_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtMiddleInitial = findViewById(R.id.edtMiddleInitial);
        edtBirthday = findViewById(R.id.edtBirthday);
        edtGender = findViewById(R.id.edtGender);
        mobilenum = findViewById(R.id.edtMobile);
        socials = findViewById(R.id.edtSocials);

        btnBack = findViewById(R.id.btnBack);
        btnSubmit = findViewById(R.id.btnSubmit);

        edtBirthday.setOnClickListener(v -> showDatePicker());
        edtGender.setOnClickListener(v -> edtGender.showDropDown());

        btnBack.setOnClickListener(v -> {
            finish();
        });

        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) {
                saveUserToFirestore();
                Intent in = new Intent(this, MainActivity.class);
                startActivity(in);
                finish();
            }
        });

        String[] genderOptions = {
                "Male",
                "Female",
                "Prefer not to say"
        };

        ArrayAdapter<String> genderAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        genderOptions
                );

        edtGender.setAdapter(genderAdapter);
        edtGender.setKeyListener(null);
    }

    private boolean validateInputs() {
        String birthday = edtBirthday.getText().toString().trim();
        String gender = edtGender.getText().toString().trim();
        String mobile = mobilenum.getText().toString().trim();
        String social = socials.getText().toString().trim();

        if (birthday.isEmpty()) {
            edtBirthday.setError("Birthday is required");
            edtBirthday.requestFocus();
            return false;
        }

        if (gender.isEmpty()) {
            edtGender.setError("Please select your gender");
            edtGender.requestFocus();
            return false;
        }

        if (mobile.isEmpty()) {
            mobilenum.setError("Mobile number is required");
            mobilenum.requestFocus();
            return false;
        }

        if (!mobile.matches("^09\\d{9}$")) {
            mobilenum.setError("Enter a valid PH mobile number (09XXXXXXXXX)");
            mobilenum.requestFocus();
            return false;
        }

        if (social.isEmpty()) {
            socials.setError("Social media handle is required");
            socials.requestFocus();
            return false;
        }

        return true;
    }

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {

                            String date =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            selectedMonth + 1,
                                            selectedDay,
                                            selectedYear
                                    );

                            edtBirthday.setText(date);
                        },
                        year,
                        month,
                        day
                );

        datePickerDialog.getDatePicker()
                .setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void saveUserToFirestore() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get data passed from SignUp activity
        String email = getIntent().getStringExtra("email");
        String location = getIntent().getStringExtra("location");
        double lat = getIntent().getDoubleExtra("lat", 0.0);
        double lng = getIntent().getDoubleExtra("lng", 0.0);

        // Get role (default to "client" if not specified)
        String role = "client";
        String FullName = edtFirstName.getText().toString().trim() + " " + edtMiddleInitial.getText().toString().trim() + ". " + edtLastName.getText().toString().trim();
        // Create complete user data map
        Map<String, Object> user = new HashMap<>();

        user.put("firstName",edtFirstName.getText().toString().trim());
        user.put("mInitial", edtMiddleInitial.getText().toString().trim() + ".");
        user.put("lastName",edtLastName.getText().toString().trim());
        user.put("username",FullName.toString().trim() );
        user.put("username_lower", FullName.toString().trim());
        user.put("email", email != null ? email : "");
        user.put("Role", role);
        user.put("createdAt", System.currentTimeMillis());
        // Location data (if available)
        if (location != null && !location.isEmpty()) {
            user.put("location", location);
            user.put("lat", lat);
            user.put("lng", lng);
            user.put("locationUpdatedAt", System.currentTimeMillis());
        }

        // Data from UserDetails form
        user.put("Birthday", edtBirthday.getText().toString().trim());
        user.put("Gender", edtGender.getText().toString().trim());
        user.put("Mobile Number", mobilenum.getText().toString().trim());
        user.put("Social", socials.getText().toString().trim());

        // Save all data at once
        db.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile completed successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}