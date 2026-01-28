package clients.posts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import data.FirebaseUtil;
import models.Post;

import adapters.PostAdapter;
import com.example.ConstructionApp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Posts extends Fragment {

    private RecyclerView recyclerView;
    private TextInputEditText txtContent;
    private ImageButton btnSend;
    private ImageView imgProfile;

    private FirebaseFirestore db;
    private ArrayList<Post> posts;
    private PostAdapter adapter;
    private String currentUserProfilePicUrl;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post, container, false);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        currentUserProfilePicUrl =
                                snapshot.getString("profilePicUrl");
                        loadPosts();
                    });
        }


        recyclerView = view.findViewById(R.id.recyclerview);
        imgProfile = view.findViewById(R.id.imgProfile);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        posts = new ArrayList<>();
        adapter = new PostAdapter(requireContext(), posts);
        recyclerView.setAdapter(adapter);

        txtContent = view.findViewById(R.id.edtPost);
        btnSend = view.findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> createPost());

        loadPosts();

        String uid = FirebaseUtil.currentUserId();
        if (uid != null && isAdded()) {
            FirebaseUtil.listenToProfilePic(
                    requireContext(),
                    imgProfile,
                    uid
            );
        }
        return view;
    }

    private void createPost() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String content = txtContent.getText().toString().trim();
        if (content.isEmpty()) return;

        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {

                    String userName = userDoc.getString("username");

                    Map<String, Object> post = new HashMap<>();
                    post.put("userId", uid);
                    post.put("userName", userName);
                    post.put("content", content);
                    post.put("timestamp", System.currentTimeMillis());

                    db.collection("posts").add(post);


                    txtContent.setText("");
                    Toast.makeText(requireContext(),
                            "Post created", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPosts() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("posts")
                .whereEqualTo("userId", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (value == null) return;

                    posts.clear();

                    for (QueryDocumentSnapshot doc : value) {

                        String content = doc.getString("content");

                        // ----- TIMESTAMP -----
                        Object rawTime = doc.get("timestamp");
                        String time;

                        if (rawTime instanceof com.google.firebase.Timestamp) {
                            time = formatTimestamp(
                                    ((com.google.firebase.Timestamp) rawTime)
                                            .toDate().getTime()
                            );
                        } else if (rawTime instanceof Long) {
                            time = formatTimestamp((Long) rawTime);
                        } else {
                            time = "Just now";
                        }

                        Post post = new Post(
                                user.getUid(),
                                "You",
                                "",
                                content != null ? content : "",
                                time,
                                currentUserProfilePicUrl
                        );

                        // 🔥 REQUIRED FOR LIKES
                        post.setPostId(doc.getId());

                        // 🔥 LIKE COUNT
                        Long likes = doc.getLong("likeCount");
                        post.setLikeCount(likes != null ? likes.intValue() : 0);

                        // 🔥 CHECK IF CURRENT USER LIKED
                        String currentUserId = user.getUid();
                        db.collection("posts")
                                .document(post.getPostId())
                                .collection("likes")
                                .document(currentUserId)
                                .get()
                                .addOnSuccessListener(likeSnap -> {
                                    post.setLikedByMe(likeSnap.exists());
                                    adapter.notifyDataSetChanged();
                                });

                        posts.add(post);
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
