package clients.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ConstructionApp.R;

import data.FirebaseUtil;

public class ServiceInfo extends AppCompatActivity {

    AutoCompleteTextView type;
    EditText etAddress, etDateTime, etDescription, etBudget;

    ImageView p1, p2, p3;
    LinearLayout Photos;
    View map;

    private String photo1 = null;
    private String photo2 = null;
    private String photo3 = null;
    private int photoCount = 0;

    // Data from previous activity
    private String fullName, email, mobileNumber, homeAddress, otherId;

    /* ================= IMAGE PICKER ================= */

    private final ActivityResultLauncher<String> UploadedImage =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri == null) return;

                        if (photoCount >= 3) {
                            Toast.makeText(this,
                                    "You can upload only 3 photos",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FirebaseUtil.UploadServiceType(
                                this,
                                uri,
                                imageUrl -> {
                                    if (photoCount == 0) {
                                        photo1 = imageUrl;
                                        Glide.with(this).load(imageUrl).into(p1);
                                    } else if (photoCount == 1) {
                                        photo2 = imageUrl;
                                        Glide.with(this).load(imageUrl).into(p2);
                                    } else {
                                        photo3 = imageUrl;
                                        Glide.with(this).load(imageUrl).into(p3);
                                    }

                                    photoCount++;
                                }
                        );
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_service_info);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get data from previous activity
        fullName = getIntent().getStringExtra("fullName");
        email = getIntent().getStringExtra("email");
        mobileNumber = getIntent().getStringExtra("mobileNumber");
        homeAddress = getIntent().getStringExtra("homeAddress");
        otherId = getIntent().getStringExtra("otherId");

        Button btnSubmit = findViewById(R.id.btnSubmit);
        type = findViewById(R.id.spServiceType);
        etAddress = findViewById(R.id.etAddress);
        etDateTime = findViewById(R.id.etDateTime);
        etDescription = findViewById(R.id.etDescription);
        etBudget = findViewById(R.id.etBudget);
        Photos = findViewById(R.id.photos);
        p1 = findViewById(R.id.photo1);
        p2 = findViewById(R.id.photo2);
        p3 = findViewById(R.id.photo3);
        map = findViewById(R.id.map);

        p1.setOnClickListener(v -> permission(UploadedImage));
        p2.setOnClickListener(v -> permission(UploadedImage));
        p3.setOnClickListener(v -> permission(UploadedImage));

        map.setOnClickListener(v -> {
            Toast.makeText(this, "Opening location..", Toast.LENGTH_SHORT).show();
            // TODO: Handle location selection
            // You can implement RealTimeLocation activity here
        });

        Photos.setOnClickListener(v -> permission(UploadedImage));

        // Date and Time Picker
        setupDateTimePicker();

        // Service Type Dropdown
        setupServiceTypeDropdown();

        // Next button - collect service data and pass everything to ReviewDetails
        btnSubmit.setOnClickListener(v -> {
            String serviceType = type.getText().toString().trim();
            String siteAddress = etAddress.getText().toString().trim();
            String dateTime = etDateTime.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String budget = etBudget.getText().toString().trim();

            // Validation
            if (serviceType.isEmpty() || siteAddress.isEmpty()) {
                Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pass all data to ReviewDetails
            Intent intent = new Intent(this, ReviewDetails.class);

            // Personal info
            intent.putExtra("fullName", fullName);
            intent.putExtra("email", email);
            intent.putExtra("mobileNumber", mobileNumber);
            intent.putExtra("homeAddress", homeAddress);
            intent.putExtra("otherId", otherId);

            // Service info
            intent.putExtra("serviceType", serviceType);
            intent.putExtra("siteAddress", siteAddress);
            intent.putExtra("dateTime", dateTime);
            intent.putExtra("description", description);
            intent.putExtra("budget", budget);

            // Photos
            intent.putExtra("photo1", photo1);
            intent.putExtra("photo2", photo2);
            intent.putExtra("photo3", photo3);

            startActivity(intent);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupDateTimePicker() {
        Calendar selectedDateTime = Calendar.getInstance();

        etDateTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();

            new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDateTime.set(year, month, dayOfMonth);

                        new TimePickerDialog(
                                this,
                                (timeView, hourOfDay, minute) -> {
                                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    selectedDateTime.set(Calendar.MINUTE, minute);

                                    SimpleDateFormat sdf = new SimpleDateFormat(
                                            "MMMM dd, yyyy • h:mm a",
                                            Locale.getDefault()
                                    );

                                    etDateTime.setText(sdf.format(selectedDateTime.getTime()));
                                },
                                now.get(Calendar.HOUR_OF_DAY),
                                now.get(Calendar.MINUTE),
                                false
                        ).show();
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private void setupServiceTypeDropdown() {
        String[] options = {
                "Construction",
                "Plumbing",
                "Cement fix",
                "Electrical",
                "Painting",
                "Carpentry",
                "Custom"
        };

        type.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        options
                )
        );
        type.setKeyListener(null);
        type.setOnClickListener(v -> type.showDropDown());
    }

    /* ================= PERMISSION ================= */

    private void permission(ActivityResultLauncher<String> act) {
        new AlertDialog.Builder(this)
                .setTitle("Media Permission")
                .setMessage("Allow app to access your gallery?")
                .setCancelable(false)
                .setPositiveButton("Yes", (d, w) -> act.launch("image/*"))
                .setNegativeButton("No", (d, w) ->
                        Toast.makeText(this,
                                "Permission required to upload photos",
                                Toast.LENGTH_SHORT).show())
                .show();
    }
}