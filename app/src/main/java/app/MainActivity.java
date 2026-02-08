package app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import clients.Notifications;
import clients.chat.ChatActivity;
import clients.chat.ChatFragment;
import clients.works.history;
import clients.home.Home;
import clients.posts.Posts;
import clients.profile.ProfileFragment;
import clients.home.SearchUserActivity;
import clients.workers.workers;

import com.example.ConstructionApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.onesignal.OneSignal;

import java.util.HashMap;
import java.util.Map;

import data.FirebaseUtil;

public class MainActivity extends AppCompatActivity {

    ImageButton navHome, navBell, navAdd, navChat, navActivity;
    ImageView btnSearch, notification, Profile;
    TextView txtNewsFeed;

    private FirebaseFirestore db;
    private boolean oneSignalLoggedIn = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // ✅ MUST BE FIRST
        setContentView(R.layout.activity_main);

        // Android 13+ notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, auth.Login.class));
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initNavigation();

        getUserLocationFromDatabase();
        getFCMToken();

        if (savedInstanceState == null) {
            loadFragment(new Home());
            highlight(navHome);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || oneSignalLoggedIn) return;

        try {
            OneSignal.login(user.getUid());
            oneSignalLoggedIn = true;
        } catch (Exception e) {
            Log.w("OneSignal", "Not ready yet, retrying later");
        }
    }

    private void initViews() {
        navHome = findViewById(R.id.navHome);
        navBell = findViewById(R.id.navBell);
        navAdd = findViewById(R.id.navAdd);
        navChat = findViewById(R.id.navChat);
        navActivity = findViewById(R.id.navActivity);

        txtNewsFeed = findViewById(R.id.txtNewsfeed);
        btnSearch = findViewById(R.id.btnSearch);
        notification = findViewById(R.id.btnBell);
        Profile = findViewById(R.id.Profile);
    }

    private void initNavigation() {

        txtNewsFeed.setOnClickListener(v -> {
            startActivity(new Intent(this, WorkersLocationMap.class));
            Toast.makeText(this, "Opening Location..", Toast.LENGTH_SHORT).show();
        });

        btnSearch.setOnClickListener(v ->
                startActivity(new Intent(this, SearchUserActivity.class))
        );

        notification.setOnClickListener(v ->
                startActivity(new Intent(this, Notifications.class))
        );

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
            loadFragment(new ChatFragment());
            highlight(navChat);
        });

        Profile.setOnClickListener(v ->
                loadFragment(new ProfileFragment())
        );

        navActivity.setOnClickListener(v -> {
            loadFragment(new history());
            highlight(navActivity);
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .commit();
    }

    private void highlight(ImageButton selected) {
        navHome.setColorFilter(getColor(R.color.text_secondary));
        navBell.setColorFilter(getColor(R.color.text_secondary));
        navChat.setColorFilter(getColor(R.color.text_secondary));
        navActivity.setColorFilter(getColor(R.color.text_secondary));

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
                    if (document != null && document.exists()) {
                        String location = document.getString("location");
                        txtNewsFeed.setText(
                                location != null ? location : "Location not specified"
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("FIRESTORE", "Failed to get location", e)
                );
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) return;

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) return;

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("fcmToken", task.getResult());
                    updates.put("userId", user.getUid());

                    FirebaseUtil.currentUserDetails().update(updates);
                });
    }
}
