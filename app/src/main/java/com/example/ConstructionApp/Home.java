package com.example.ConstructionApp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

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
        recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new PostAdapter(requireContext(), posts);
        recyclerPosts.setAdapter(adapter);

        loadPosts();
        return view;
    }

    private void loadPosts() {

        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    posts.clear();
                    adapter.notifyDataSetChanged();

                    for (DocumentSnapshot postDoc : value.getDocuments()) {

                        String userId = postDoc.getString("userId");
                        if (userId == null) continue;

                        String content = postDoc.getString("content");
                        String title = postDoc.getString("title");

                        // ---- timestamp ----
                        Object rawTime = postDoc.get("timestamp");
                        String time;

                        if (rawTime instanceof Timestamp) {
                            Timestamp ts = (Timestamp) rawTime;
                            time = formatTimestamp(ts.toDate().getTime());

                        } else if (rawTime instanceof Long) {
                            time = formatTimestamp((Long) rawTime);

                        } else {
                            time = "Just now";
                        }

                        // ---- if user already cached ----
                        if (profileCache.containsKey(userId)) {

                            Post post = new Post(
                                    userId,
                                    profileCache.get(userId + "_name"),
                                    title != null ? title : "",
                                    content != null ? content : "",
                                    time,
                                    profileCache.get(userId)
                            );

                            posts.add(post);
                            adapter.notifyItemInserted(posts.size() - 1);
                            continue;
                        }

                        // ---- load user like ChatActivity ----
                        FirebaseUtil.getUserReference(userId)
                                .get()
                                .addOnSuccessListener(userSnap -> {

                                    if (!userSnap.exists()) return;

                                    String username =
                                            userSnap.getString("username");

                                    String profilePicUrl =
                                            userSnap.getString("profilePicUrl");

                                    // cache user data
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

                                    posts.add(post);
                                    adapter.notifyItemInserted(posts.size() - 1);
                                });
                    }
                });
    }

    private String formatTimestamp(long millis) {
        return new SimpleDateFormat(
                "MMM dd, yyyy • hh:mm a",
                Locale.getDefault()
        ).format(new Date(millis));
    }
}
