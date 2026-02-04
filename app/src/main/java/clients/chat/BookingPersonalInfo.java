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

import java.util.HashMap;
import java.util.Map;

public class BookingPersonalInfo extends AppCompatActivity {

    EditText firstname, lastname, initial, mobilenum, email, location;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

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

        approved.setOnClickListener(v -> {

            // ✅ GET VALUES HERE
            String F_name = firstname.getText().toString().trim();
            String L_name = lastname.getText().toString().trim();
            String I_name = initial.getText().toString().trim();
            String M_num = mobilenum.getText().toString().trim();
            String E_M = email.getText().toString().trim();
            String L = location.getText().toString().trim();

            // OPTIONAL: basic validation
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

            Intent in = new Intent(this, ServiceInfo.class);
            startActivity(in);
        });
    }

    private void saveUserToFirestore(String username, String email, String mobilenum, String location) {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("Name", username);
        orderData.put("Email", email);
        orderData.put("Mobile Number", mobilenum);
        orderData.put("Home_Address", location);

        db.collection("BookingOrder")
                .document(user.getUid())
                .set(orderData);
    }
}
