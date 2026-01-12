package com.example.ConstructionApp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GetStartedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getstarted);

        // Check login state FIRST
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
/*
        if (currentUser != null) {
            // User already logged in → go to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

 */

        Button login = findViewById(R.id.btnLogin);
        Button signup = findViewById(R.id.btnSignUp);

        login.setOnClickListener(view -> {
            startActivity(new Intent(this, Login.class));
        });

        signup.setOnClickListener(view -> {
            startActivity(new Intent(this, CreateAccount.class));
        });
    }
}
