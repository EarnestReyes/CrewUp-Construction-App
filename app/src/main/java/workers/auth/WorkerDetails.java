package workers.auth;

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


public class WorkerDetails extends AppCompatActivity {
    Button btnSubmit;
    ImageButton btnBack;

    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        db = FirebaseFirestore.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtGender = findViewById(R.id.edtGender);
        mobilenum = findViewById(R.id.edtMobile);
        socials = findViewById(R.id.edtSocials);

        btnBack = findViewById(R.id.btnBack);
        btnSubmit = findViewById(R.id.btnSubmit);

        edtBirthday.setOnClickListener(v -> showDatePicker());
        edtGender.setOnClickListener(v -> edtGender.showDropDown());


        btnSubmit.setOnClickListener(v -> {
                saveUserToFirestore();
                Intent in = new Intent(this, TopUpWallet.class);
                startActivity(in);
        });





        }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    edtBirthday.setText(date);

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
    }
}