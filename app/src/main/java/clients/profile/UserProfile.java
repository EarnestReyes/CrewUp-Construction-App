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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    ImageView profile, imgCoverPhoto;
    ImageButton backBtn;
    Button button;
    String currentUserProfilePicUrl;
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
        imgCoverPhoto = findViewById(R.id.imgCoverPhoto);

        backBtn.setOnClickListener(v -> finish());

        String profileUserId = getIntent().getStringExtra("userId");
        if (profileUserId == null) {
            finish();
            return;
        }

        recyclerPosts = findViewById(R.id.recyclerPosts);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));

        posts = new ArrayList<>();
        adapter = new PostAdapter(this, posts);
        recyclerPosts.setAdapter(adapter);

        loadUserInfo(profileUserId);
        loadPosts(profileUserId);

        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("userId", profileUserId);
            startActivity(intent);
        });
    }

    // ================= USER INFO =================

    private void loadUserInfo(String userId) {

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    username.setText(doc.getString("username"));

                    String cover = doc.getString("CoverprofilePicUrl");
                    if (cover != null && !cover.isEmpty()) {
                        Glide.with(this)
                                .load(cover)
                                .centerCrop()
                                .into(imgCoverPhoto);
                    }

                    String profileUrl = doc.getString("profilePicUrl");
                    if (profileUrl != null && !profileUrl.isEmpty()) {
                        Glide.with(this)
                                .load(profileUrl)
                                .circleCrop()
                                .into(profile);
                    }
                });
    }

    // ================= POSTS =================

    private void loadPosts(String profileUserId) {

        String currentUserId = FirebaseAuth.getInstance().getUid();

        db.collection("posts")
                .whereEqualTo("userId", profileUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    posts.clear();

                    for (QueryDocumentSnapshot doc : value) {

                        String userId = doc.getString("userId");
                        if (userId == null) continue;

                        String content = doc.getString("content");
                        String title = doc.getString("title");
                        String imageUrl = doc.getString("imageUrl");

                        // ---- TIMESTAMP (ALWAYS LONG) ----
                        long timestamp;
                        Object rawTime = doc.get("timestamp");

                        if (rawTime instanceof com.google.firebase.Timestamp) {
                            timestamp = ((com.google.firebase.Timestamp) rawTime)
                                    .toDate().getTime();
                        } else if (rawTime instanceof Long) {
                            timestamp = (Long) rawTime;
                        } else {
                            timestamp = System.currentTimeMillis();
                        }

                        Long likes = doc.getLong("likeCount");
                        int likeCount = likes != null ? likes.intValue() : 0;

                        String cachedName = profileCache.get(userId + "_name");
                        String cachedPic = profileCache.get(userId);

                        if (cachedName != null) {

                            Post post = new Post(
                                    userId,
                                    cachedName,
                                    title != null ? title : "",
                                    content != null ? content : "",
                                    timestamp,
                                    cachedPic,
                                    imageUrl
                            );

                            post.setPostId(doc.getId());
                            post.setLikeCount(likeCount);
                            posts.add(post);

                            checkLike(post, currentUserId);
                            continue;
                        }

                        FirebaseUtil.getUserReference(userId)
                                .get()
                                .addOnSuccessListener(userSnap -> {

                                    if (!userSnap.exists()) return;

                                    String name = userSnap.getString("username");
                                    String pic = userSnap.getString("profilePicUrl");

                                    profileCache.put(userId, pic);
                                    profileCache.put(userId + "_name", name);

                                    Post post = new Post(
                                            userId,
                                            name != null ? name : "Unknown",
                                            title != null ? title : "",
                                            content != null ? content : "",
                                            timestamp,
                                            pic,
                                            imageUrl
                                    );

                                    post.setPostId(doc.getId());
                                    post.setLikeCount(likeCount);
                                    posts.add(post);

                                    checkLike(post, currentUserId);
                                    adapter.notifyDataSetChanged();
                                });
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void checkLike(Post post, String currentUserId) {
        if (currentUserId == null) return;

        db.collection("posts")
                .document(post.getPostId())
                .collection("likes")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(like ->
                        post.setLikedByMe(like.exists())
                );
    }

}

