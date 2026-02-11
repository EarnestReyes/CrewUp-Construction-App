package workers.auth;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ConstructionApp.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WorkerDetails extends AppCompatActivity {
    TextInputEditText edtFirstName, edtLastName, edtMiddleInitial, edtAddress, edtBirthday, mobilenum, socials;
    TextInputEditText edtCustomExpertise;
    AutoCompleteTextView edtGender, edtExpertise;
    LinearLayout layoutCustomExpertise;
    ChipGroup chipGroupSelectedExpertise;
    Button btnSubmit, btnAddCustomExpertise;
    ImageButton btnBack;

    FirebaseFirestore db;
    private static final String CUSTOM_OPTION = "Custom";
    private List<String> selectedExpertiseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_worker_details);

        db = FirebaseFirestore.getInstance();
        selectedExpertiseList = new ArrayList<>();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtMiddleInitial = findViewById(R.id.edtMiddleInitial);

        edtExpertise = findViewById(R.id.edtExpertise);
        edtCustomExpertise = findViewById(R.id.edtCustomExpertise);
        layoutCustomExpertise = findViewById(R.id.layoutCustomExpertise);
        chipGroupSelectedExpertise = findViewById(R.id.chipGroupSelectedExpertise);
        btnAddCustomExpertise = findViewById(R.id.btnAddCustomExpertise);
        edtGender = findViewById(R.id.edtGender);

        edtAddress = findViewById(R.id.edtAddress);
        edtBirthday = findViewById(R.id.edtBirthday);
        mobilenum = findViewById(R.id.edtMobile);
        socials = findViewById(R.id.edtSocials);

        btnBack = findViewById(R.id.btnBack);
        btnSubmit = findViewById(R.id.btnSubmit);

        setupDropdowns();
        setupListeners();
    }

    private void setupDropdowns() {
        // Gender dropdown
        String[] genderOptions = {"Male", "Female", "Prefer not to say"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genderOptions);
        edtGender.setAdapter(genderAdapter);

        // Expertise dropdown with "Custom" option
        String[] expertiseOptions = {
                "General Construction",
                "Plumbing",
                "Electrical",
                "Carpentry",
                "Masonry",
                "HVAC",
                "Roofing",
                "Painting",
                CUSTOM_OPTION
        };
        ArrayAdapter<String> expertiseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, expertiseOptions);
        edtExpertise.setAdapter(expertiseAdapter);
    }

    private void setupListeners() {
        edtBirthday.setOnClickListener(v -> showDatePicker());

        edtGender.setOnClickListener(v -> edtGender.showDropDown());
        edtExpertise.setOnClickListener(v -> edtExpertise.showDropDown());

        // Handle expertise selection
        edtExpertise.setOnItemClickListener((parent, view, position, id) -> {
            String selected = edtExpertise.getText().toString();
            if (CUSTOM_OPTION.equals(selected)) {
                // Show custom expertise field
                layoutCustomExpertise.setVisibility(View.VISIBLE);
                edtCustomExpertise.requestFocus();
            } else {
                // Add predefined expertise
                addExpertiseChip(selected);
                edtExpertise.setText("");
            }
        });

        // Add custom expertise button
        btnAddCustomExpertise.setOnClickListener(v -> addCustomExpertise());

        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) {
                passDataToTopUpWallet();
            }
        });
    }

    private void addExpertiseChip(String expertiseName) {
        // Check if already added
        if (selectedExpertiseList.contains(expertiseName)) {
            Toast.makeText(this, "This expertise is already added", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add to list
        selectedExpertiseList.add(expertiseName);

        // Create chip
        Chip chip = new Chip(this);
        chip.setText(expertiseName);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);
        chip.setChipBackgroundColorResource(R.color.primary);
        chip.setTextColor(getResources().getColor(android.R.color.white, null));

        // Remove on close icon click
        chip.setOnCloseIconClickListener(v -> {
            chipGroupSelectedExpertise.removeView(chip);
            selectedExpertiseList.remove(expertiseName);
        });

        chipGroupSelectedExpertise.addView(chip);
    }

    private void addCustomExpertise() {
        String customText = edtCustomExpertise.getText().toString().trim();

        if (customText.isEmpty()) {
            edtCustomExpertise.setError("Enter expertise name");
            return;
        }

        // Add the custom expertise as a chip
        addExpertiseChip(customText);

        // Clear and hide custom field
        edtCustomExpertise.setText("");
        layoutCustomExpertise.setVisibility(View.GONE);
        edtExpertise.setText("");

        Toast.makeText(this, "Custom expertise added", Toast.LENGTH_SHORT).show();
    }

    private boolean validateInputs() {
        String first = edtFirstName.getText().toString().trim();
        String last = edtLastName.getText().toString().trim();
        String middleInitial = edtMiddleInitial.getText().toString().trim();
        String birthday = edtBirthday.getText().toString().trim();
        String gender = edtGender.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String mobile = mobilenum.getText().toString().trim();
        String social = socials.getText().toString().trim();

        if (first.isEmpty()) {
            edtFirstName.setError("First name is required");
            edtFirstName.requestFocus();
            return false;
        }

        if (last.isEmpty()) {
            edtLastName.setError("Last name is required");
            edtLastName.requestFocus();
            return false;
        }

        if (middleInitial.isEmpty()) {
            edtMiddleInitial.setError("Middle initial is required");
            edtMiddleInitial.requestFocus();
            return false;
        }

        // Check if at least one expertise is selected
        if (selectedExpertiseList.isEmpty()) {
            Toast.makeText(this, "Please add at least one expertise", Toast.LENGTH_SHORT).show();
            return false;
        }

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

        if (address.isEmpty()) {
            edtAddress.setError("Address is required");
            edtAddress.requestFocus();
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

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedMonth + 1, selectedDay, selectedYear);
                    edtBirthday.setText(date);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void passDataToTopUpWallet() {
        // Get data passed from WorkerSignUp activity
        String email = getIntent().getStringExtra("email");
        String location = getIntent().getStringExtra("location");
        double lat = getIntent().getDoubleExtra("lat", 0.0);
        double lng = getIntent().getDoubleExtra("lng", 0.0);

        // Build full name
        String fullName = edtFirstName.getText().toString().trim() + " "
                + edtMiddleInitial.getText().toString().trim() + " "
                + edtLastName.getText().toString().trim();

        // Pass all data to TopUpWallet
        Intent intent = new Intent(this, TopUpWallet.class);

        // Data from WorkerSignUp
        intent.putExtra("email", email);
        intent.putExtra("location", location);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);

        // Name fields
        intent.putExtra("firstName", edtFirstName.getText().toString().trim());
        intent.putExtra("lastName", edtLastName.getText().toString().trim());
        intent.putExtra("middleInitial", edtMiddleInitial.getText().toString().trim());
        intent.putExtra("fullName", fullName);

        // Worker-specific fields - pass expertise as ArrayList
        intent.putStringArrayListExtra("expertiseList", new ArrayList<>(selectedExpertiseList));
        intent.putExtra("address", edtAddress.getText().toString().trim());

        // Personal details
        intent.putExtra("birthday", edtBirthday.getText().toString().trim());
        intent.putExtra("gender", edtGender.getText().toString().trim());
        intent.putExtra("mobileNumber", mobilenum.getText().toString().trim());
        intent.putExtra("social", socials.getText().toString().trim());

        startActivity(intent);
        finish();
    }
}