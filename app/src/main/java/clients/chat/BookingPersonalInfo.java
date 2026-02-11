package clients.chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class BookingPersonalInfo extends AppCompatActivity {

    EditText firstname, lastname, initial, mobilenum, email, location;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String otherId; // Worker ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_personal_info);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        otherId = getIntent().getStringExtra("otherId");

        Button btnNext = findViewById(R.id.btnNext);
        ImageView back = findViewById(R.id.btnBack);

        firstname = findViewById(R.id.firstname);
        lastname = findViewById(R.id.lastname);
        initial = findViewById(R.id.initial);
        mobilenum = findViewById(R.id.mobilenum);
        email = findViewById(R.id.email);
        location = findViewById(R.id.location);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        back.setOnClickListener(v -> finish());

        // Auto-fill user info from profile
        loadUserProfile();

        btnNext.setOnClickListener(v -> {
            String firstName = firstname.getText().toString().trim();
            String lastName = lastname.getText().toString().trim();
            String middleInitial = initial.getText().toString().trim();
            String mobileNumber = mobilenum.getText().toString().trim();
            String emailAddress = email.getText().toString().trim();
            String homeAddress = location.getText().toString().trim();

            // Validation
            if (firstName.isEmpty() || lastName.isEmpty() || mobileNumber.isEmpty()) {
                Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Construct full name
            String fullName = firstName +
                    (middleInitial.isEmpty() ? "" : " " + middleInitial + ".") +
                    " " + lastName;

            // Pass data to next activity via Intent
            Intent intent = new Intent(this, ServiceInfo.class);
            intent.putExtra("fullName", fullName);
            intent.putExtra("email", emailAddress);
            intent.putExtra("mobileNumber", mobileNumber);
            intent.putExtra("homeAddress", homeAddress);
            intent.putExtra("otherId", otherId); // Pass worker ID forward
            startActivity(intent);
        });
    }

    /**
     * Load user profile data and auto-fill the form
     */
    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    String firstName = documentSnapshot.getString("firstName");
                    String mInitial = documentSnapshot.getString("mInitial");
                    String lastName = documentSnapshot.getString("lastName");
                    String mobile = documentSnapshot.getString("Mobile Number");
                    String emailAddr = documentSnapshot.getString("email");

                    String address = documentSnapshot.getString("Home_Address");


                            firstname.setText(firstName);
                            initial.setText(mInitial);
                            lastname.setText(lastName);
                            if (emailAddr != null) email.setText(emailAddr);
                            if (mobile != null) mobilenum.setText(mobile);
                            if (address != null) location.setText(address);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                );
    }
}