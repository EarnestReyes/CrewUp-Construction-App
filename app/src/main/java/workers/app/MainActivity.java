package workers.app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.ConstructionApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.onesignal.OneSignal;

import java.util.HashMap;
import java.util.Map;

import clients.chat.ChatActivity;
import clients.home.Home;
import clients.home.SearchUserActivity;
import clients.posts.Posts;
import clients.works.ClientHistoryFragment;
import workers.chat.WorkersChat;
import workers.home.NotificationsWorker;
import workers.profile.WorkerProfile;
import workers.wallet.WalletProfile;
import workers.works.WorkerHistoryFragment;
import workers.works.works;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    ImageButton navHome, navBell, navAdd, navChat, navActivity;
    ImageView btnSearch, Notification, Profile;
    TextView txtNewsFeed;

    private FirebaseFirestore db;
    private boolean oneSignalLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        createNotificationChannel();
        requestNotificationPermissionIfNeeded();

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
        saveFCMToken();

        if (savedInstanceState == null) {
            loadFragment(new Home());
            highlight(navHome);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        attemptOneSignalLogin();
    }

    // ----------------------------------------------------
    // NOTIFICATION PERMISSION
    // ----------------------------------------------------

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    "default", // 🔥 MUST MATCH Cloud Function
                    "General Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("CrewUp notifications");

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.e("ONESIGNAL", "Notification permission GRANTED");
                oneSignalLoggedIn = false;
                attemptOneSignalLogin();

            } else {
                Log.e("ONESIGNAL", "Notification permission DENIED");
            }
        }
    }

    // ----------------------------------------------------
    // ONESIGNAL LOGIN (SAFE + RETRYABLE)
    // ----------------------------------------------------

    private void attemptOneSignalLogin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || oneSignalLoggedIn) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        try {
            String uid = user.getUid();

            Log.e("ONESIGNAL", "Logging in UID = " + uid);

            // ✅ LOGIN
            OneSignal.login(uid);

            // ✅ THIS IS THE MISSING PIECE
            OneSignal.getUser().addTag("userId", uid);

            oneSignalLoggedIn = true;

            Log.e("ONESIGNAL", "Tag set: userId = " + uid);

        } catch (Exception e) {
            Log.w("ONESIGNAL", "OneSignal not ready yet", e);
        }
    }



    // ----------------------------------------------------
    // UI SETUP
    // ----------------------------------------------------

    private void initViews() {
        navHome = findViewById(R.id.navHome);
        navBell = findViewById(R.id.navBell);
        navAdd = findViewById(R.id.navAdd);
        navChat = findViewById(R.id.navChat);
        navActivity = findViewById(R.id.navActivity);

        txtNewsFeed = findViewById(R.id.txtNewsfeed);
        btnSearch = findViewById(R.id.btnSearch);
        Notification = findViewById(R.id.btnBell);
        Profile = findViewById(R.id.Profile);
    }

    private void initNavigation() {

        txtNewsFeed.setOnClickListener(v ->
                startActivity(new Intent(this, UserAroundLocation.class))
        );

        Notification.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsWorker.class))
        );

        btnSearch.setOnClickListener(v ->
                startActivity(new Intent(this, SearchUserActivity.class))
        );

        navHome.setOnClickListener(v -> {
            loadFragment(new Home());
            highlight(navHome);
        });

        navBell.setOnClickListener(v -> {
            loadFragment(new WorkerHistoryFragment());
            highlight(navBell);
        });


        navAdd.setOnClickListener(v -> {
            loadFragment(new Posts());
            highlight(null);
        });

        navChat.setOnClickListener(v -> {
            loadFragment(new WorkersChat());
            highlight(navChat);
        });

        Profile.setOnClickListener(v ->
                loadFragment(new WorkerProfile())
        );

        navActivity.setOnClickListener(v -> {
            loadFragment(new WalletProfile());
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

    // ----------------------------------------------------
    // FIREBASE DATA
    // ----------------------------------------------------

    private void getUserLocationFromDatabase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String location = document.getString("location");
                        txtNewsFeed.setText(
                                location != null ? location : "Location not specified"
                        );
                    }
                });
    }

    private void saveFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) return;

                    Map<String, Object> data = new HashMap<>();
                    data.put("userId", user.getUid());
                    data.put("role", "worker");
                    data.put("fcmToken", token);

                    db.collection("users")
                            .document(user.getUid())
                            .set(data, SetOptions.merge());
                });
    }
}
