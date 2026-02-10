package workers.auth;

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
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import workers.app.MainActivity;

public class WorkerDetails extends AppCompatActivity {
    TextInputEditText edtFirstName, edtLastName, edtMiddleInitial, edtAddress, edtBirthday, mobilenum, socials;
    AutoCompleteTextView edtGender, edtExpertise;
    Button btnSubmit;
    ImageButton btnBack;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_worker_details);

        db = FirebaseFirestore.getInstance();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtMiddleInitial = findViewById(R.id.edtMiddleInitial);


        edtExpertise = findViewById(R.id.edtExpertise);
        edtGender = findViewById(R.id.edtGender);


        edtAddress = findViewById(R.id.edtAddress);
        edtBirthday = findViewById(R.id.edtBirthday);
        mobilenum = findViewById(R.id.edtMobile);
        socials = findViewById(R.id.edtSocials);


        btnBack = findViewById(R.id.btnBack);
        btnSubmit = findViewById(R.id.btnSubmit);


        String[] genderOptions = {"Male", "Female", "Prefer not to say"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genderOptions);
        edtGender.setAdapter(genderAdapter);

        String[] expertiseOptions = {"General Construction", "Plumbing", "Electrical", "Carpentry", "Masonry", "HVAC", "Roofing", "Painting"};
        ArrayAdapter<String> expertiseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, expertiseOptions);
        edtExpertise.setAdapter(expertiseAdapter);

        edtBirthday.setOnClickListener(v -> showDatePicker());


        edtGender.setOnClickListener(v -> edtGender.showDropDown());
        edtExpertise.setOnClickListener(v -> edtExpertise.showDropDown());

        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) {
                saveUserToFirestore();
                Intent in = new Intent(this, TopUpWallet.class);
                startActivity(in);
            }
        });
    }

    private boolean validateInputs() {


        String first = edtFirstName.getText().toString().trim();
        String last = edtLastName.getText().toString().trim();
        String birthday = edtBirthday.getText().toString().trim();
        String gender = edtGender.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String mobile = mobilenum.getText().toString().trim();

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

    private void saveUserToFirestore() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> user = new HashMap<>();
        String FullName = edtFirstName.getText().toString().trim() + " " +  edtMiddleInitial.getText().toString().trim() + " " + edtLastName.getText().toString().trim();
        user.put("FirstName", edtFirstName.getText().toString().trim());
        user.put("LastName", edtLastName.getText().toString().trim());
        user.put("MiddleInitial", edtMiddleInitial.getText().toString().trim());
        user.put("username", FullName);
        user.put("username_lower", FullName.toLowerCase());
        user.put("Expertise", edtExpertise.getText().toString().trim());
        user.put("Address", edtAddress.getText().toString().trim());


        user.put("Birthday", edtBirthday.getText().toString().trim());
        user.put("Gender", edtGender.getText().toString().trim());
        user.put("Mobile Number", mobilenum.getText().toString().trim());
        user.put("Social", socials.getText().toString().trim());


        db.collection("users")
                .document(uid)
                .set(user, SetOptions.merge())
                .addOnFailureListener(e ->
                        Toast.makeText(WorkerDetails.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}