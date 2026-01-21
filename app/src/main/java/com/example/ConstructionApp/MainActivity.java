package com.example.ConstructionApp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    // Bottom nav buttons
    ImageButton navHome, navBell, navAdd, navChat, navProfile;
    TextView txtNewsFeed;
    private FirebaseFirestore db;

    private String userLocation = "";

    private TextView txtLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();

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

        getUserLocationFromDatabase();

        // Init buttons
        navHome = findViewById(R.id.navHome);
        navBell = findViewById(R.id.navBell);
        navAdd = findViewById(R.id.navAdd);
        navChat = findViewById(R.id.navChat);
        navProfile = findViewById(R.id.navProfile);
        txtNewsFeed = findViewById(R.id.txtNewsfeed);


        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new Home());
            highlight(navHome);
        }

        // Click listeners
        navHome.setOnClickListener(v -> {
            loadFragment(new Home());
            highlight(navHome);
        });

        navBell.setOnClickListener(v -> {
            loadFragment(new workers());
            highlight(navBell);
        });

        navAdd.setOnClickListener(v -> {
            loadFragment(new Posts());
            highlight(null);
        });

        navChat.setOnClickListener(v -> {
            loadFragment(new Messages());
            highlight(navChat);
        });

        navProfile.setOnClickListener(v -> {
            loadFragment(new Activity());
            highlight(navProfile);
        });
    }

    // Fragment loader
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .commit();
    }

    // Highlight selected icon
    private void highlight(ImageButton selected) {
        navHome.setColorFilter(getColor(R.color.text_secondary));
        navBell.setColorFilter(getColor(R.color.text_secondary));
        navChat.setColorFilter(getColor(R.color.text_secondary));
        navProfile.setColorFilter(getColor(R.color.text_secondary));

        if (selected != null) {
            selected.setColorFilter(getColor(R.color.primary));
        }
    }
    private void getUserLocationFromDatabase() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {

                    if (isFinishing() || isDestroyed() || document == null || !document.exists())
                        return;

                    userLocation = document.getString("location");

                    if (userLocation != null && !userLocation.isEmpty()) {
                        txtNewsFeed.setText(userLocation);
                    } else {
                        txtNewsFeed.setText("Location not specified");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("FIRESTORE", "Failed to get location", e));
    }
}

