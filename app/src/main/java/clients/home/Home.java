package clients.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import adapters.PostAdapter;
import com.example.ConstructionApp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import data.FirebaseUtil;
import models.Post;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Home extends Fragment {

    private RecyclerView recyclerPosts;
    private ArrayList<Post> posts;
    private PostAdapter adapter;

    private FirebaseFirestore db;

    private ProgressBar progressLoading;
    private TextView txtEmpty;
    private SwipeRefreshLayout swipeRefresh;

    private final Map<String, String> profileCache = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        posts = new ArrayList<>();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerPosts = view.findViewById(R.id.recyclerPosts);
        progressLoading = view.findViewById(R.id.progressLoading);
        txtEmpty = view.findViewById(R.id.txtEmpty);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PostAdapter(requireContext(), posts);
        recyclerPosts.setAdapter(adapter);

        progressLoading.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);

        swipeRefresh.setColorSchemeResources(
                R.color.primary,
                R.color.icon_share,
                R.color.icon_comment
        );

        swipeRefresh.setOnRefreshListener(this::loadPosts);

        loadPosts();
        return view;
    }

    private void loadPosts() {

        swipeRefresh.setRefreshing(true);

        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {

                    posts.clear();
                    adapter.notifyDataSetChanged();

                    swipeRefresh.setRefreshing(false);
                    progressLoading.setVisibility(View.GONE);

                    if (snapshot == null || snapshot.isEmpty()) {
                        txtEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    txtEmpty.setVisibility(View.GONE);

                    String currentUserId = FirebaseAuth.getInstance().getUid();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {

                        final String postId = doc.getId();
                        final String userId = doc.getString("userId");
                        if (userId == null) continue;

                        final String content = doc.getString("content");
                        final String title = doc.getString("title");
                        final String imageUrl = doc.getString("imageUrl");

                        // ---------- TIMESTAMP ----------
                        Object rawTime = doc.get("timestamp");
                        final String time;

                        if (rawTime instanceof Timestamp) {
                            time = formatTimestamp(((Timestamp) rawTime).toDate().getTime());
                        } else if (rawTime instanceof Long) {
                            time = formatTimestamp((Long) rawTime);
                        } else {
                            time = "Just now";
                        }

                        Long likes = doc.getLong("likeCount");
                        final int likeCount = likes != null ? likes.intValue() : 0;

                        // ---------- CACHE HIT ----------
                        if (profileCache.containsKey(userId)) {

                            Post post = new Post(
                                    userId,
                                    profileCache.get(userId + "_name"),
                                    title != null ? title : "",
                                    content != null ? content : "",
                                    time,
                                    profileCache.get(userId)
                            );

                            post.setPostId(postId);
                            post.setImageUrl(imageUrl);
                            post.setLikeCount(likeCount);

                            posts.add(post);
                            adapter.notifyItemInserted(posts.size() - 1);
                            checkIfLiked(post, currentUserId);
                            continue;
                        }

                        // ---------- LOAD USER ----------
                        FirebaseUtil.getUserReference(userId)
                                .get()
                                .addOnSuccessListener(userSnap -> {

                                    if (!userSnap.exists()) return;

                                    String username = userSnap.getString("username");
                                    String profilePicUrl = userSnap.getString("profilePicUrl");

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

                                    post.setPostId(postId);
                                    post.setImageUrl(imageUrl);
                                    post.setLikeCount(likeCount);

                                    posts.add(post);
                                    adapter.notifyItemInserted(posts.size() - 1);
                                    checkIfLiked(post, currentUserId);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    progressLoading.setVisibility(View.GONE);
                    txtEmpty.setVisibility(View.VISIBLE);
                });
    }

    private void checkIfLiked(Post post, String currentUserId) {
        if (currentUserId == null) return;

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

    private String formatTimestamp(long millis) {
        return new SimpleDateFormat(
                "MMM dd, yyyy • hh:mm a",
                Locale.getDefault()
        ).format(new Date(millis));
    }
}
