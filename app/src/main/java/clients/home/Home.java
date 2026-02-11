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

                        Post post = new Post();
                        post.setPostId(doc.getId());

                        // ===== BASIC FIELDS =====
                        post.setUserId(doc.getString("userId"));
                        post.setUserName(doc.getString("userName"));
                        post.setProfilePicUrl(doc.getString("profilePicUrl"));
                        post.setContent(doc.getString("content"));
                        post.setTitle(doc.getString("title"));
                        post.setImageUrl(doc.getString("imageUrl"));

                        Long likes = doc.getLong("likeCount");
                        post.setLikeCount(likes != null ? likes.intValue() : 0);

                        // ===== TIMESTAMP =====
                        Object rawTime = doc.get("timestamp");
                        if (rawTime instanceof Timestamp) {
                            post.setTimestamp(((Timestamp) rawTime)
                                    .toDate()
                                    .getTime());
                        } else if (rawTime instanceof Long) {
                            post.setTimestamp((Long) rawTime);
                        } else {
                            post.setTimestamp(System.currentTimeMillis());
                        }

                        // ===== SHARED POST HANDLING =====
                        Boolean shared = doc.getBoolean("isShared");
                        post.setShared(shared != null && shared);

                        if (post.isShared()) {

                            post.setOriginalPostId(doc.getString("originalPostId"));
                            post.setOriginalUserId(doc.getString("originalUserId"));
                            post.setOriginalUserName(doc.getString("originalUserName"));
                            post.setOriginalContent(doc.getString("originalContent"));
                            post.setOriginalImageUrl(doc.getString("originalImageUrl"));

                            // 🔥 FETCH ORIGINAL USER PROFILE FROM USERS COLLECTION
                            String originalUserId = post.getOriginalUserId();

                            if (originalUserId != null) {
                                FirebaseUtil.getUserReference(originalUserId)
                                        .get()
                                        .addOnSuccessListener(userSnap -> {
                                            if (userSnap.exists()) {
                                                post.setOriginalProfilePicUrl(
                                                        userSnap.getString("profilePicUrl")
                                                );
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        }

                        posts.add(post);
                        adapter.notifyItemInserted(posts.size() - 1);
                        checkIfLiked(post, currentUserId);
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
