package com.example.ConstructionApp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Home extends Fragment {

    private RecyclerView recyclerPosts;
    private ArrayList<Post> posts;
    private PostAdapter adapter;
    private FirebaseFirestore db;

    public Home() {
        // Required empty public constructor
    }

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

        // Connect RecyclerView
        recyclerPosts = view.findViewById(R.id.recyclerPosts);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Adapter
        adapter = new PostAdapter(posts);
        recyclerPosts.setAdapter(adapter);

        // Firebase query ordered by timestamp descending
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    posts.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        String title = doc.getString("title");
                        String content = doc.getString("content");
                        long timestamp = doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0;

                        String formattedTime = formatTimestamp(timestamp);

                        // For now, name is static "User"
                        posts.add(new Post("Earnest Reyes", title, content, formattedTime));
                    }
                    adapter.notifyDataSetChanged();
                });

        return view;
    }

    // Helper to format timestamp nicely
    private String formatTimestamp(long millis) {
        if (millis == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}
