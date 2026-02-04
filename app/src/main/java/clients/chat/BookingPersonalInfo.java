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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BookingPersonalInfo extends AppCompatActivity {

    EditText firstname, lastname, initial, mobilenum, email, location;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String projectId;

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

        Button approved = findViewById(R.id.btnNext);
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

        SuggestToUser();

        approved.setOnClickListener(v -> {

            String F_name = firstname.getText().toString().trim();
            String L_name = lastname.getText().toString().trim();
            String I_name = initial.getText().toString().trim();
            String M_num = mobilenum.getText().toString().trim();
            String E_M = email.getText().toString().trim();
            String L = location.getText().toString().trim();

            if (F_name.isEmpty() || L_name.isEmpty() || M_num.isEmpty()) {
                Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            saveUserToFirestore(
                    F_name + " " + I_name + ". " + L_name,
                    E_M,
                    M_num,
                    L
            );

            Intent intent = new Intent(this, ServiceInfo.class);
            intent.putExtra("projectId", projectId);
            startActivity(intent);
        });
    }

    private void saveUserToFirestore(String username, String email, String mobilenum, String location) {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        DocumentReference ref = db.collection("BookingOrder").document();
        projectId = ref.getId();

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("projectId", projectId);
        orderData.put("userId", user.getUid());
        orderData.put("Name", username);
        orderData.put("Email", email);
        orderData.put("Mobile Number", mobilenum);
        orderData.put("Home_Address", location);
        orderData.put("status", "pending");

        ref.set(orderData)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Booking created", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to create booking", Toast.LENGTH_SHORT).show()
                );
    }
    private void SuggestToUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    String name = documentSnapshot.getString("username");
                    String mobile = documentSnapshot.getString("Mobile Number");
                    String Email = documentSnapshot.getString("email");

                    if (name == null || name.trim().isEmpty()) return;

                    String[] parts = name.trim().split("\\s+");

                    String firstName = "";
                    String initials = "";
                    String lastName = "";

                    if (parts.length >= 1) {
                        firstName = parts[0];
                    }

                    if (parts.length == 3) {
                        initials = parts[1].replace(".", "");
                        lastName = parts[2];
                    } else if (parts.length >= 2) {
                        lastName = parts[parts.length - 1];
                    }

                    firstname.setText(firstName);
                    initial.setText(initials);
                    lastname.setText(lastName);
                    email.setText(Email);

                    if (mobile != null) {
                        mobilenum.setText(mobile);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch user info", Toast.LENGTH_SHORT).show()
                );
    }


}
