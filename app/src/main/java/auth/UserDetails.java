package auth;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;

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

import app.MainActivity;
import workers.auth.TopUpWallet;

public class UserDetails extends AppCompatActivity {
    TextInputEditText edtBirthday, mobilenum, socials;
    AutoCompleteTextView edtGender;
    Button  btnSubmit;
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
            saveUserToFirestore();
            Intent in = new Intent(this, SignupSuccessActivity.class);
            startActivity(in);
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

        Map<String, Object> user = new HashMap<>();
        user.put("Birthday", edtBirthday.getText().toString().trim());
        user.put("Gender", edtGender.getText().toString().trim());
        user.put("Mobile Number", mobilenum.getText().toString().trim());
        user.put("Social", socials.getText().toString().trim());

        db.collection("users")
                .document(uid)
                .set(user, SetOptions.merge());
    }

}