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
import clients.chat.RealTimeLocation;
import clients.works.history;
import data.FirebaseUtil;
import clients.home.Home;
import clients.posts.Posts;
import clients.profile.ProfileFragment;
import com.example.ConstructionApp.R;
import clients.home.SearchUserActivity;
import clients.workers.workers;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.onesignal.OneSignal;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ImageButton navHome, navBell, navAdd, navChat, navActivity;
    TextView txtNewsFeed;
    ImageView btnSearch, notification, Profile ;
    private FirebaseFirestore db;
    private String userLocation = "";
    private TextView txtLocation;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            OneSignal.login(user.getUid());
        }

        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
        if (users == null) {
            startActivity(new Intent(this, auth.Login.class));
            finish();
            return;
        }

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

        getUserLocationFromDatabase();
        getFCMToken();

        // Init buttons
        navHome = findViewById(R.id.navHome);
        navBell = findViewById(R.id.navBell);
        navAdd = findViewById(R.id.navAdd);
        navChat = findViewById(R.id.navChat);
        navActivity = findViewById(R.id.navActivity);
        txtNewsFeed = findViewById(R.id.txtNewsfeed);
        btnSearch = findViewById(R.id.btnSearch);
        notification = findViewById(R.id.btnBell);
        Profile = findViewById((R.id.Profile));

        txtNewsFeed.setOnClickListener(v -> {
            Intent in = new Intent(this, WorkersLocationMap.class);
            startActivity(in);
            Toast.makeText(this, "Opening Location..", Toast.LENGTH_SHORT).show();
        });

        btnSearch.setOnClickListener(v -> {
            Intent in = new Intent(MainActivity.this, SearchUserActivity.class);
            startActivity(in);
        });

        navChat.setOnClickListener(v -> {
            Intent in = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(in);
        });
        notification.setOnClickListener(v -> {
            Intent in = new Intent(this, Notifications.class);
            startActivity(in);
        });

        if (savedInstanceState == null) {
            loadFragment(new Home());
            highlight(navHome);
        }

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

        Profile.setOnClickListener(v -> {
            loadFragment(new ProfileFragment());
        });
        navActivity.setOnClickListener(v -> {
            //add function list of projects of user
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

    // Highlight selected icon
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

    void getFCMToken(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                task.addOnCompleteListener(taskResult -> {
                    if (!taskResult.isSuccessful()) {
                        return;
                    }

                    String token = taskResult.getResult();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user == null) return;

                    String userId = user.getUid();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("fcmToken", token);
                    updates.put("userId", userId);

                    FirebaseUtil.currentUserDetails().update(updates);
                });

            }
        });
    }
}

