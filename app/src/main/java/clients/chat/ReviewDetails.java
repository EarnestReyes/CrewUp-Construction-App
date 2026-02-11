package clients.chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ConstructionApp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewDetails extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    // UI Elements
    TextView Username, UserMobile, UserAddress;
    TextView SiteAddress, DateTime, Budget, Description, ServiceType;
    ImageView photoLeftTop, photoRight, photoLeftBottom, btnBack;
    Button btnSubmit, btnPrevious;

    // Data from previous activities
    private String fullName, email, mobileNumber, homeAddress, otherId;
    private String serviceType, siteAddress, dateTime, description, budget;
    private String photo1, photo2, photo3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        loadDataFromIntent();
        displayReviewData();
        setupButtons();
    }

    private void initializeViews() {
        // Personal Info
        Username = findViewById(R.id.Username);
        UserMobile = findViewById(R.id.UserMobile);
        UserAddress = findViewById(R.id.UserAddrees);

        // Service Info
        SiteAddress = findViewById(R.id.SiteAddress);
        DateTime = findViewById(R.id.DateTime);
        Budget = findViewById(R.id.Budget);
        Description = findViewById(R.id.Description);

        // Photos
        photoLeftTop = findViewById(R.id.photoLeftTop);
        photoRight = findViewById(R.id.photoRight);
        photoLeftBottom = findViewById(R.id.photoLeftBottom);

        // Buttons
        btnSubmit = findViewById(R.id.btnSubmit);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnBack = findViewById(R.id.btnBack);
    }

    /**
     * Load all data passed from previous activities
     */
    private void loadDataFromIntent() {
        Intent intent = getIntent();

        // Personal info
        fullName = intent.getStringExtra("fullName");
        email = intent.getStringExtra("email");
        mobileNumber = intent.getStringExtra("mobileNumber");
        homeAddress = intent.getStringExtra("homeAddress");
        otherId = intent.getStringExtra("otherId");

        // Service info
        serviceType = intent.getStringExtra("serviceType");
        siteAddress = intent.getStringExtra("siteAddress");
        dateTime = intent.getStringExtra("dateTime");
        description = intent.getStringExtra("description");
        budget = intent.getStringExtra("budget");

        // Photos
        photo1 = intent.getStringExtra("photo1");
        photo2 = intent.getStringExtra("photo2");
        photo3 = intent.getStringExtra("photo3");
    }

    /**
     * Display all collected data for review
     */
    private void displayReviewData() {
        // Display personal info
        Username.setText(fullName != null && !fullName.isEmpty() ? fullName : "—");
        UserMobile.setText(mobileNumber != null && !mobileNumber.isEmpty() ? mobileNumber : "—");
        UserAddress.setText(homeAddress != null && !homeAddress.isEmpty() ? homeAddress : "—");

        // Display service info
        SiteAddress.setText(siteAddress != null && !siteAddress.isEmpty() ? siteAddress : "—");
        DateTime.setText(dateTime != null && !dateTime.isEmpty() ? dateTime : "—");
        Description.setText(description != null && !description.isEmpty() ? description : "—");

        // Display budget with currency symbol
        if (budget != null && !budget.isEmpty()) {
            Budget.setText("₱" + budget);
        } else {
            Budget.setText("—");
        }

        // Display photos
        displayPhotos();
    }

    /**
     * Display uploaded photos using Glide
     */
    private void displayPhotos() {
        if (photo1 != null && !photo1.isEmpty()) {
            Glide.with(this)
                    .load(photo1)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(photoLeftTop);
        }

        if (photo2 != null && !photo2.isEmpty()) {
            Glide.with(this)
                    .load(photo2)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(photoRight);
        }

        if (photo3 != null && !photo3.isEmpty()) {
            Glide.with(this)
                    .load(photo3)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(photoLeftBottom);
        }
    }

    /**
     * Setup button listeners
     */
    private void setupButtons() {
        // Submit - Save all data to Firebase
        btnSubmit.setOnClickListener(v -> saveBookingToFirebase());

        // Back to ServiceInfo
        btnPrevious.setOnClickListener(v -> finish());

        // Close
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Save all booking data to Firebase in one operation
     */
    private void saveBookingToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        // Create new document reference (generates projectId)
        DocumentReference bookingRef = db.collection("BookingOrder").document();
        String projectId = bookingRef.getId();

        // Prepare photos list
        List<String> photos = new ArrayList<>();
        if (photo1 != null && !photo1.isEmpty()) photos.add(photo1);
        if (photo2 != null && !photo2.isEmpty()) photos.add(photo2);
        if (photo3 != null && !photo3.isEmpty()) photos.add(photo3);

        // Create booking data map
        Map<String, Object> bookingData = new HashMap<>();

        // Project metadata
        bookingData.put("projectId", projectId);
        bookingData.put("userId", currentUser.getUid());
        bookingData.put("workerId", otherId); // Worker ID if assigned
        bookingData.put("status", "pending");
        bookingData.put("createdAt", Timestamp.now());

        // Personal information
        bookingData.put("Name", fullName);
        bookingData.put("Email", email != null ? email : "");
        bookingData.put("Mobile Number", mobileNumber);
        bookingData.put("Home_Address", homeAddress);

        // Service information
        bookingData.put("Service_Type", serviceType);
        bookingData.put("Site_Address", siteAddress);
        bookingData.put("Date & Time", dateTime != null ? dateTime : "");
        bookingData.put("Description", description != null ? description : "");
        bookingData.put("Budget", budget != null ? budget : "");

        // Photos
        bookingData.put("photos", photos);

        // Save to Firebase
        bookingRef.set(bookingData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Booking submitted successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate to ChatActivity with projectId
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("projectId", projectId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit booking: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();

                    // Re-enable button
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit");
                });
    }
}