package clients.chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ConstructionApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ServiceInfo extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    AutoCompleteTextView type;
    EditText etAddress, etDateTime, etDescription, etBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_service_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button nxt = findViewById(R.id.btnSubmit);
        type = findViewById(R.id.spServiceType);
        etAddress = findViewById(R.id.etAddress);
        etDateTime = findViewById(R.id.etDateTime);
        etDescription = findViewById(R.id.etDescription);
        etBudget = findViewById(R.id.etBudget);

        Calendar selectedDateTime = Calendar.getInstance();

        etDateTime.setOnClickListener(v -> {

            Calendar now = Calendar.getInstance();

            // 📅 DATE PICKER
            new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {

                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // ⏰ TIME PICKER (opens after date)
                        new TimePickerDialog(
                                this,
                                (timeView, hourOfDay, minute) -> {

                                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    selectedDateTime.set(Calendar.MINUTE, minute);

                                    // Format like real apps
                                    SimpleDateFormat sdf =
                                            new SimpleDateFormat(
                                                    "MMMM dd, yyyy • h:mm a",
                                                    Locale.getDefault()
                                            );

                                    etDateTime.setText(
                                            sdf.format(selectedDateTime.getTime())
                                    );

                                },
                                now.get(Calendar.HOUR_OF_DAY),
                                now.get(Calendar.MINUTE),
                                false   // false = 12-hour clock (AM/PM)
                        ).show();

                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            ).show();
        });


        type.setOnClickListener(v -> type.showDropDown());

        String[] Options = {
                "Construction",
                "Plumbing",
                "Cement fix",
                "Custom :"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        Options
                );

        type.setAdapter(adapter);
        type.setKeyListener(null);

        nxt.setOnClickListener(v -> {
            Intent in = new Intent(this, ReviewDetails.class);
            saveUserToFirestore(getIntent().getStringExtra("projectId"));
            startActivity(in);
        });

        ImageView back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> {
            finish();
        });

    }

    private void saveUserToFirestore(String projectId) {

        Map<String, Object> user = new HashMap<>();
        user.put("Service Type", type.getText().toString().trim());
        user.put("Site_Address", etAddress.getText().toString().trim());
        user.put("Date & Time", etDateTime.getText().toString().trim());
        user.put("Description", etDescription.getText().toString().trim());
        user.put("Photo", "Image // put image here");
        user.put("Budget", etBudget.getText().toString().trim());

        db.collection("BookingOrder")
                .document(projectId)
                .set(user, SetOptions.merge());
    }
}
