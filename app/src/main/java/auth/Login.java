package auth;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import app.CreateAccount;
import com.example.ConstructionApp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import app.MainActivity;

public class Login extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        }

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });

        TextInputEditText edtUsername = findViewById(R.id.edtUsername);
        TextInputEditText edtPass = findViewById(R.id.edtPassword);
        TextView signup = findViewById(R.id.txtSignup);
        TextView forgot = findViewById(R.id.txtForgot);
        Button login = findViewById(R.id.btnLogin);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        login.setOnClickListener(v -> {

            String emailTxt = edtUsername.getText().toString().trim(); // use email
            String passTxt = edtPass.getText().toString().trim();

            if (emailTxt.isEmpty() || passTxt.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(emailTxt, passTxt)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            //check if exist
                            checkUserRole();
                        } else {

                            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();

                        }
                    });
                 });


        signup.setOnClickListener(v ->
                startActivity(new Intent(this, CreateAccount.class)));

        forgot.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPass.class)));
    }

    private void checkUserRole() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("workers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Toast.makeText(this, "Welcome Worker!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, workers.app.MainActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}

