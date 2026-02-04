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

import com.example.ConstructionApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReviewDetails extends AppCompatActivity {
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    TextView Username, UserMobile, UserAddress, SiteAddress, DateTime, Budget, Description;
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

        loaddetails();

        Button nxt = findViewById(R.id.btnSubmit);
        nxt.setOnClickListener(v -> {
            Intent in = new Intent(this, ServiceBreakdown.class);
            startActivity(in);
        });

        Button prv = findViewById(R.id.btnPrevious);
        prv.setOnClickListener(v -> {
            Intent in = new Intent(this, ServiceInfo.class);
            startActivity(in);
        });

        ImageView back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> {
            finish();
        });

    }
    private void loaddetails() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("BookingOrder")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("Name");
                        String mobile = documentSnapshot.getString("Mobile Number");
                        String address = documentSnapshot.getString("Home_Address");
                        String site = documentSnapshot.getString("Site_Address");
                        String timestamp = documentSnapshot.getString("Date & Time");
                        String budget = documentSnapshot.getString("Budget");
                        String description = documentSnapshot.getString("Description");

                        if (name != null) {
                            Username.setText(name);
                            UserMobile.setText(mobile);
                            UserAddress.setText(address);
                            SiteAddress.setText(site);
                            DateTime.setText(timestamp);
                            Description.setText(description);
                            Budget.setText(budget);
                        } else {
                            Toast.makeText(this, "User information unable to be fetch", Toast.LENGTH_SHORT).show();
                        }
                    }});
    }
}