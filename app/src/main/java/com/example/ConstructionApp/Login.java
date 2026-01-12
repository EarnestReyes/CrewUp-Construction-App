package com.example.ConstructionApp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        /*
        //CHECK IF USER IS ALREADY LOGGED IN
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
            return;
        }
         */

        TextInputEditText edtEmail = findViewById(R.id.edtEmail);
        TextInputEditText edtpass = findViewById(R.id.edtPassword);
        TextView signup = findViewById(R.id.txtSignup);
        TextView forgot = findViewById(R.id.txtForgot);
        Button login = findViewById(R.id.btnLogin);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(view -> {
            finish();
        });

        login.setOnClickListener(view -> {

                String emailTxt = edtEmail.getText().toString().trim();
                String passTxt = edtpass.getText().toString().trim();

                if (emailTxt.isEmpty() || passTxt.isEmpty()) {
                    Toast.makeText(Login.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(emailTxt, passTxt)
                        .addOnCompleteListener(Login.this, task -> {
                            if (task.isSuccessful()) {

                                Toast.makeText(Login.this,
                                        "Login Successfully!",
                                        Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(Login.this, MainActivity.class));
                                finish();

                            } else {
                                Toast.makeText(Login.this,
                                        task.getException() != null
                                                ? task.getException().getMessage()
                                                : "Login failed",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            });

        signup.setOnClickListener(view-> {
            //if correct
            Intent in = new Intent(this, CreateAccount.class);
            startActivity(in);
        });

        forgot.setOnClickListener(View-> {
            Intent in = new Intent(this, ForgotPass.class);
            startActivity(in);
        });

    }
}