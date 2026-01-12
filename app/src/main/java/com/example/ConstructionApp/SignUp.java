package com.example.ConstructionApp;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class SignUp extends AppCompatActivity {

    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextInputEditText username = findViewById(R.id.edtUsername);
        TextInputEditText email = findViewById(R.id.edtEmail);
        TextInputEditText password = findViewById(R.id.edtPassword);
        TextInputEditText confirmpass = findViewById(R.id.edtConfirmPassword);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        Button signin = findViewById(R.id.btnSignUp);
        TextView login = findViewById(R.id.txtLogin);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(view -> {
            finish();
        });

        signin.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);

            String emailTxt = email.getText().toString().trim();
            String passTxt = password.getText().toString().trim();
            String confirmTxt = confirmpass.getText().toString().trim();

            if (emailTxt.isEmpty() || passTxt.isEmpty() || confirmTxt.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!passTxt.equals(confirmTxt)) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (passTxt.length() < 6) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(emailTxt, passTxt)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUp.this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUp.this,
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });


        login.setOnClickListener(view -> {
            Intent in = new Intent(this, Login.class);
            startActivity(in);
        });
    }
}