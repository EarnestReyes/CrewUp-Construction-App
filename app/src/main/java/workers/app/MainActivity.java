package workers.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

import java.util.HashMap;
import java.util.Map;

import clients.chat.ChatActivity;
import clients.home.Home;
import clients.home.SearchUserActivity;
import clients.posts.Posts;
import workers.chat.WorkersChat;
import workers.home.NotificationsWorker;
import workers.profile.WorkerProfile;
import workers.wallet.WalletProfile;
import workers.works.works;

public class MainActivity extends AppCompatActivity {

    // Bottom nav buttons
    ImageButton navHome, navBell, navAdd, navChat, navActivity;
    TextView txtNewsFeed;
    ImageView btnSearch, Notification, Profile;
    private FirebaseFirestore db;

    private String userLocation = "";

    private TextView txtLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, auth.Login.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main2);
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
        Profile = findViewById(R.id.Profile);
        txtNewsFeed = findViewById(R.id.txtNewsfeed);
        btnSearch = findViewById(R.id.btnSearch);
        Notification = findViewById(R.id.btnBell);
        navActivity = findViewById(R.id.navActivity);

        Notification.setOnClickListener(v -> {
            Intent in = new Intent(this, NotificationsWorker.class);
            startActivity(in);
        });

        btnSearch.setOnClickListener(v -> {
            Intent in = new Intent(MainActivity.this, SearchUserActivity.class);
            startActivity(in);
        });

        navChat.setOnClickListener(v -> {
            Intent in = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(in);
        });

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
            loadFragment(new works());
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

        Profile.setOnClickListener(v -> {
            loadFragment(new WorkerProfile());

        });

        navActivity.setOnClickListener(v -> {
            loadFragment(new WalletProfile());
            highlight(navActivity);

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

    public void getFCMToken() {

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) return;

                    String uid = user.getUid();

                    Map<String, Object> data = new HashMap<>();
                    data.put("userId", uid);
                    data.put("Role", "worker");
                    data.put("fcmToken", token);

                    db.collection("users")
                            .document(uid)
                            .set(data, SetOptions.merge())
                            .addOnSuccessListener(unused ->
                                    Log.d("FIRESTORE", "Worker data saved"))
                            .addOnFailureListener(e ->
                                    Log.e("FIRESTORE", "Worker save failed", e));
                });
}
}

