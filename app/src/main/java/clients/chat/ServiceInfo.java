package clients.chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import data.FirebaseUtil;

public class ServiceInfo extends AppCompatActivity {

    public FirebaseFirestore db;
    FirebaseAuth mAuth;
    AutoCompleteTextView type;
    EditText etAddress, etDateTime, etDescription, etBudget;
    String projectId;
    ImageView p1, p2, p3;
    LinearLayout Photos;

    private String photo1 = null;
    private String photo2 = null;
    private String photo3 = null;
    private int photoCount = 0;


    //For image uploader
    private final ActivityResultLauncher<String> UploadedImage =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri == null) return;

                        if (photoCount >= 3) {
                            Toast.makeText(this, "You can upload only 3 photos", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FirebaseUtil.UploadServiceType(
                                this,
                                uri,
                                imageUrl -> {

                                    // 🔥 SHOW IMAGE IMMEDIATELY (REALTIME)
                                    if (photoCount == 0) {
                                        photo1 = imageUrl;
                                        Glide.with(this)
                                                .load(imageUrl)
                                                .centerCrop()
                                                .into(p1);
                                    } else if (photoCount == 1) {
                                        photo2 = imageUrl;
                                        Glide.with(this)
                                                .load(imageUrl)
                                                .centerCrop()
                                                .into(p2);
                                    } else if (photoCount == 2) {
                                        photo3 = imageUrl;
                                        Glide.with(this)
                                                .load(imageUrl)
                                                .centerCrop()
                                                .into(p3);
                                    }

                                    photoCount++;
                                }
                        );
                    }
            );



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

        projectId = getIntent().getStringExtra("projectId");

        Button nxt = findViewById(R.id.btnSubmit);
        type = findViewById(R.id.spServiceType);
        etAddress = findViewById(R.id.etAddress);
        etDateTime = findViewById(R.id.etDateTime);
        etDescription = findViewById(R.id.etDescription);
        etBudget = findViewById(R.id.etBudget);
        Photos = findViewById(R.id.photos);
        p1 = findViewById(R.id.photo1);
        p2 = findViewById(R.id.photo2);
        p3 = findViewById(R.id.photo3);

        p1.setOnClickListener(v -> permission(UploadedImage));
        p2.setOnClickListener(v -> permission(UploadedImage));
        p3.setOnClickListener(v -> permission(UploadedImage));

        Photos.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("BookingOrder")
                    .document(projectId)
                    .get()
                    .addOnSuccessListener(doc -> {

                        List<String> photos = (List<String>) doc.get("photos");

                        if (photos != null && photos.size() >= 3) {
                            Toast.makeText(this, "Maximum of 3 photos only", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        permission(UploadedImage);
                    });
        });

        loadpictures();

        Calendar selectedDateTime = Calendar.getInstance();

        etDateTime.setOnClickListener(v -> {

            Calendar now = Calendar.getInstance();

            new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {

                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

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
            in.putExtra("projectId", projectId);
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
        user.put("Budget", etBudget.getText().toString().trim());

        List<String> photos = new ArrayList<>();

        if (photo1 != null) photos.add(photo1);
        if (photo2 != null) photos.add(photo2);
        if (photo3 != null) photos.add(photo3);

        user.put("photos", photos); // ✅ store as array

        db.collection("BookingOrder")
                .document(projectId)
                .set(user, SetOptions.merge());
    }

    private void permission(ActivityResultLauncher act) {
        new AlertDialog.Builder(this)
                .setTitle("Media Permission")
                .setMessage("Allow app to access your gallery?")
                .setCancelable(false)
                .setPositiveButton("Yes", (d, w) -> act.launch("image/*"))
                .setNegativeButton("No", (d, w) ->
                        Toast.makeText(this, "We need permission of camera to proceed", Toast.LENGTH_SHORT).show())
                .show();
    }

    public void loadpictures(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("BookingOrder")
                .whereEqualTo("status", "pending")
                .whereEqualTo("userId", currentUser.getUid())
            .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "No pending booking found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);

                    List<String> photos = (List<String>) doc.get("photos");

                    if (photos != null) {
                        if (photos.size() > 0)
                            Glide.with(this).load(photos.get(0)).into(p1);

                        if (photos.size() > 1)
                            Glide.with(this).load(photos.get(1)).into(p2);

                        if (photos.size() > 2)
                            Glide.with(this).load(photos.get(2)).into(p3);
                    }
                });
    }

}
