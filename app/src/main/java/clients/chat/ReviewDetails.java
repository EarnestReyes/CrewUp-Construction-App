package clients.chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import models.ProjectModel;

public class ReviewDetails extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    String projectId;

    TextView Username, UserMobile, UserAddress,
            SiteAddress, DateTime, Budget, Description;

    ImageView photoLeftTop, photoRight, photoLeftBottom;

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

        Username = findViewById(R.id.Username);
        UserMobile = findViewById(R.id.UserMobile);
        UserAddress = findViewById(R.id.UserAddrees);
        SiteAddress = findViewById(R.id.SiteAddress);
        DateTime = findViewById(R.id.DateTime);
        Budget = findViewById(R.id.Budget);
        Description = findViewById(R.id.Description);

        //imageviews
        photoLeftBottom = findViewById(R.id.photoLeftBottom);
        photoRight = findViewById(R.id.photoRight);
        photoLeftTop = findViewById(R.id.photoLeftTop);

        //place the images here
        FirebaseFirestore.getInstance()
                .collection("BookingOrder")
                .document(projectId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String url = doc.getString("Service_info_image");
                    if (url != null && !url.isEmpty()) {
                        Glide.with(this)
                                .load(url)
                                .centerCrop()
                                .into(photoLeftTop);
                    }
                });

        loadDetails();

        findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            //after finish there's a splash screen
            Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("projectId", projectId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    });

        findViewById(R.id.btnPrevious).setOnClickListener(v ->
                startActivity(new Intent(this, ServiceInfo.class))
        );

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadDetails() {

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

                    projectId = doc.getId();

                    Username.setText(getSafe(doc, "Name"));
                    UserMobile.setText(getSafe(doc, "Mobile Number"));
                    UserAddress.setText(getSafe(doc, "Home_Address"));
                    SiteAddress.setText(getSafe(doc, "Site_Address"));
                    DateTime.setText(getSafe(doc, "Date & Time"));
                    Description.setText(getSafe(doc, "Description"));
                    Budget.setText("₱" + getSafe(doc, "Budget"));

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load details", Toast.LENGTH_SHORT).show()
                );
    }

    private String getSafe(DocumentSnapshot doc, String field) {
        String value = doc.getString(field);
        return value != null && !value.isEmpty() ? value : "—";
    }
}
