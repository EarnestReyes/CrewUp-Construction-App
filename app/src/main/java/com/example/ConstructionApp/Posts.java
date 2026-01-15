package com.example.ConstructionApp;

import static android.content.Intent.getIntent;
import static android.content.Intent.getIntentOld;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private ImageView imgProfile;
    private TextInputEditText txtcontent;
    private ImageButton btnSend;
    private FirebaseFirestore db;
    private ArrayList<Post> posts;
    private PostAdapter adapter;
    private RecyclerView recyclerview;
    private ActivityResultLauncher<String> imagePickerLauncher;

    public Posts() {


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imgProfile.setImageURI(uri);
                    }
                }
        );

    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_post, container, false);

        db = FirebaseFirestore.getInstance();

        recyclerview = view.findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));

        posts = new ArrayList<>();

        adapter = new PostAdapter(posts);
        recyclerview.setAdapter(adapter);

        loadPosts();

        imgProfile = view.findViewById(R.id.imgProfile);
        btnSend = view.findViewById(R.id.btnSend);
        txtcontent = view.findViewById(R.id.edtPost);

        String content = txtcontent.getText().toString().trim();

        if (content.isEmpty()){
            btnSend.setOnClickListener(v -> openPostDialog());
        }

        imgProfile = view.findViewById(R.id.imgProfile);

        imgProfile.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        return view;
    }



    private void openPostDialog() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;
                    String username = doc.getString("username");
                    Map<String, Object> post = new HashMap<>();
                    post.put("Username", username);
                    post.put("content", txtcontent.getText().toString().trim());
                    post.put("timestamp", System.currentTimeMillis());
                    post.put("userId", uid);

                    db.collection("posts")
                            .add(post)
                            .addOnSuccessListener(d -> {
                                if (!isAdded()) return;
                                Toast.makeText(requireContext(),
                                        "Post created", Toast.LENGTH_SHORT).show();
                                txtcontent.setText(null);
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                Toast.makeText(requireContext(),
                                        "Failed to post", Toast.LENGTH_SHORT).show();
                            });
                });
    }


    private void loadPosts() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    posts.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        String title = doc.getString("title");
                        String content = doc.getString("content");

                        long timestamp = doc.getLong("timestamp") != null
                                ? doc.getLong("timestamp")
                                : 0;

                        String time = formatTimestamp(timestamp);

                        String name = currentUser.getDisplayName() != null
                                ? currentUser.getDisplayName()
                                : "You";

                        posts.add(new Post(
                                name,
                                title,
                                content,
                                time
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
