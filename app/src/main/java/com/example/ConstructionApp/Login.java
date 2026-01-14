package com.example.ConstructionApp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

        /*
        //CHECK IF USER IS ALREADY LOGGED IN
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
            return;
        }
         */

        getWindow().getInsetsController().setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        );

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

        TextInputEditText edtusername = findViewById(R.id.edtUsername);
        TextInputEditText edtpass = findViewById(R.id.edtPassword);
        TextView signup = findViewById(R.id.txtSignup);
        TextView forgot = findViewById(R.id.txtForgot);
        Button login = findViewById(R.id.btnLogin);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(view -> {
            finish();
        });

        login.setOnClickListener(view -> {

                String usernamelTxt = edtusername.getText().toString().trim();
                String passTxt = edtpass.getText().toString().trim();

            if (usernamelTxt.isEmpty() || passTxt.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("username", usernamelTxt)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(query -> {

                        if (query.getDocuments().get(0).getString("email") == null) {
                            Toast.makeText(this, "Account data is corrupted", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mAuth.signInWithEmailAndPassword(query.getDocuments().get(0).getString("email"), passTxt)
                                .addOnCompleteListener(task -> {

                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Login.this, MainActivity.class));
                                        finish();
                                    } else {

                                        Toast.makeText(this,
                                                task.getException() != null
                                                        ? task.getException().getMessage()
                                                        : "Login failed",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Login Failed!", Toast.LENGTH_SHORT).show());
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