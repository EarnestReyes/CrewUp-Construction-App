package clients.profile;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ConstructionApp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import adapters.PostAdapter;
import clients.chat.ChatActivity;
import data.FirebaseUtil;
import models.Post;
import models.UserModel;

public class UserProfile extends AppCompatActivity {

    public RecyclerView recyclerPosts;
    public PostAdapter adapter;
    private ArrayList<Post> posts;
    private final Map<String, String> profileCache = new HashMap<>();
    TextView username;
    ImageView profile;
    ImageButton backBtn;
    Button button;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        username = findViewById(R.id.workertxtName);
        profile = findViewById(R.id.imgProfile);
        button = findViewById(R.id.btnMessage);
        backBtn = findViewById(R.id.back_btn);

        backBtn.setOnClickListener(v -> finish());

        String userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            finish();
            return;
        }

        posts = new ArrayList<>();

        loadPosts();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    username.setText(doc.getString("username"));

                    String url = doc.getString("profilePicUrl");
                    if (url != null && !url.isEmpty()) {
                        Glide.with(this)
                                .load(url)
                                .placeholder(R.drawable.ic_profile_placeholder_foreground)
                                .circleCrop()
                                .into(profile);
                    }
                });

        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }

    private void loadPosts() {

        String profileUserId = getIntent().getStringExtra("userId");
        if (profileUserId == null) return;

        recyclerPosts = findViewById(R.id.recyclerPosts);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));

        posts.clear();
        adapter = new PostAdapter(this, posts);
        recyclerPosts.setAdapter(adapter);

        String currentUserId = FirebaseAuth.getInstance().getUid();

        db.collection("posts")
                .whereEqualTo("userId", profileUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    posts.clear();

                    for (DocumentSnapshot postDoc : value.getDocuments()) {

                        String userId = postDoc.getString("userId");
                        if (userId == null) continue;

                        String content = postDoc.getString("content");
                        String title = postDoc.getString("title");

                        // ---- timestamp ----
                        Object rawTime = postDoc.get("timestamp");
                        String time;

                        if (rawTime instanceof Timestamp) {
                            time = formatTimestamp(
                                    ((Timestamp) rawTime).toDate().getTime()
                            );
                        } else if (rawTime instanceof Long) {
                            time = formatTimestamp((Long) rawTime);
                        } else {
                            time = "Just now";
                        }

                        // ---- like count ----
                        Long likes = postDoc.getLong("likeCount");
                        int likeCount = likes != null ? likes.intValue() : 0;

                        // ---- cached profile ----
                        String cachedName = profileCache.get(userId + "_name");
                        String cachedPic = profileCache.get(userId);

                        if (cachedName != null) {

                            Post post = new Post(
                                    userId,
                                    cachedName,
                                    title != null ? title : "",
                                    content != null ? content : "",
                                    time,
                                    cachedPic
                            );

                            post.setPostId(postDoc.getId());
                            post.setLikeCount(likeCount);

                            posts.add(post);

                            // 🔥 check if liked by current user
                            if (currentUserId != null) {
                                db.collection("posts")
                                        .document(post.getPostId())
                                        .collection("likes")
                                        .document(currentUserId)
                                        .get()
                                        .addOnSuccessListener(likeSnap -> {
                                            post.setLikedByMe(likeSnap.exists());
                                            adapter.notifyDataSetChanged();
                                        });
                            }
                            continue;
                        }

                        FirebaseUtil.getUserReference(userId)
                                .get()
                                .addOnSuccessListener(userSnap -> {

                                    if (!userSnap.exists()) return;

                                    String username =
                                            userSnap.getString("username");

                                    String profilePicUrl =
                                            userSnap.getString("profilePicUrl");

                                    profileCache.put(userId, profilePicUrl);
                                    profileCache.put(userId + "_name", username);

                                    Post post = new Post(
                                            userId,
                                            username != null ? username : "Unknown",
                                            title != null ? title : "",
                                            content != null ? content : "",
                                            time,
                                            profilePicUrl
                                    );

                                    // 🔥 REQUIRED FOR LIKES
                                    post.setPostId(postDoc.getId());
                                    post.setLikeCount(likeCount);

                                    posts.add(post);

                                    // 🔥 check if liked by current user
                                    if (currentUserId != null) {
                                        db.collection("posts")
                                                .document(post.getPostId())
                                                .collection("likes")
                                                .document(currentUserId)
                                                .get()
                                                .addOnSuccessListener(likeSnap -> {
                                                    post.setLikedByMe(likeSnap.exists());
                                                    adapter.notifyDataSetChanged();
                                                });
                                    }

                                    adapter.notifyDataSetChanged();
                                });
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private String formatTimestamp(long millis) {
        return new SimpleDateFormat(
                "MMM dd, yyyy • hh:mm a",
                Locale.getDefault()
        ).format(new Date(millis));
    }

}
