package com.example.ConstructionApp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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

        recyclerPosts = view.findViewById(R.id.recyclerPosts);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new PostAdapter(posts);
        recyclerPosts.setAdapter(adapter);

        loadPosts();

        return view;
    }

    private void loadPosts() {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        Log.e("HOME_POSTS", "Firestore error", error);
                        return;
                    }

                    if (value == null) return;

                    posts.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        String username = doc.getString("Username"); // MUST match Firestore
                        String content = doc.getString("content");

                        Long time = doc.getLong("timestamp");
                        long timestamp = time != null ? time : 0;

                        posts.add(new Post(
                                username != null ? username : "Unknown",
                                "", // no title (field does not exist)
                                content != null ? content : "",
                                formatTimestamp(timestamp)
                        ));
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private String formatTimestamp(long millis) {
        if (millis == 0) return "";
        SimpleDateFormat sdf =
                new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}
