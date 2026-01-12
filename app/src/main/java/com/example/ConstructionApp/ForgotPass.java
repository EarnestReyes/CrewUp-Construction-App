package com.example.ConstructionApp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPass extends AppCompatActivity {

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_pass);
        mAuth = FirebaseAuth.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button resetBtn = findViewById(R.id.resetBtn);
        EditText emailEt = findViewById(R.id.emailEt);
        ImageButton backbtn = findViewById(R.id.backBtn);

        backbtn.setOnClickListener(view -> {
            finish();
        });

        resetBtn.setOnClickListener(v -> {
            String email = emailEt.getText().toString().trim();

            if (email.isEmpty()) {
                emailEt.setError("Email required");
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this,
                                    "Reset link sent to your email",
                                    Toast.LENGTH_LONG).show();
                            Intent in = new Intent(ForgotPass.this, MainActivity.class);
                            startActivity(in);
                        } else {
                            Toast.makeText(this,
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });


    }
}