package com.example.ConstructionApp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        if (getWindow().getInsetsController() != null) {
            getWindow().getInsetsController().setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            );
        }

        View main = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    topInset,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
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
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(
                                    this,
                                    task.getException() != null
                                            ? task.getException().getMessage()
                                            : "Login failed",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });


        signup.setOnClickListener(v ->
                startActivity(new Intent(this, CreateAccount.class)));

        forgot.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPass.class)));
    }
}
