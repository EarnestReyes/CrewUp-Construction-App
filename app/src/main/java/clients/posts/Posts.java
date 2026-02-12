package clients.posts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import data.FirebaseUtil;
import models.Post;

import adapters.PostAdapter;

import com.bumptech.glide.Glide;
import com.example.ConstructionApp.R;
import com.google.android.material.textfield.TextInputEditText;
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

public class Posts extends Fragment {
    private EditText txtContent;
    private Button btnSend;
    private ImageView imgProfile, imgPreview;
    private ImageButton gallery;
    private FirebaseFirestore db;
    private TextView txtName;
    private Uri selectedImageUri;

    private String currentUserProfilePicUrl;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null && isAdded()) {

                            selectedImageUri = uri;

                            imgPreview.setVisibility(View.VISIBLE);
                            Glide.with(requireContext())
                                    .load(uri)
                                    .into(imgPreview);
                        }
                    }
            );
;
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post, container, false);

        imgProfile = view.findViewById(R.id.imgProfile);
        txtName = view.findViewById(R.id.txtName);
        txtContent = view.findViewById(R.id.edtPost);
        btnSend = view.findViewById(R.id.btnPost);
        gallery = view.findViewById(R.id.gallery);
        imgPreview = view.findViewById(R.id.imgPreview);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(DocumentSnapshot -> {

                        currentUserProfilePicUrl =
                                DocumentSnapshot.getString("profilePicUrl");

                        String username = DocumentSnapshot.getString("username");
                        txtName.setText(username);
                    });
        }

        gallery.setOnClickListener(v -> {
            //open camera
            imagePickerLauncher.launch("image/*");
        });

        btnSend.setOnClickListener(v -> createPost());

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

        if (content.isEmpty() && selectedImageUri == null) {
            Toast.makeText(requireContext(),
                    "Post cannot be empty",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            FirebaseUtil.ImagePost(
                    requireContext(),
                    selectedImageUri,
                    imageUrl -> savePost(content, imageUrl)
            );
        } else {
            savePost(content, null);
        }
    }

    private void savePost(
            String content,
            @Nullable String imageUrl
    ) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {

                    if (!userDoc.exists()) return;

                    Map<String, Object> post = new HashMap<>();

                    post.put("userId", uid);
                    post.put("userName", userDoc.getString("username"));
                    post.put("profilePicUrl", userDoc.getString("profilePicUrl"));
                    post.put("content", content);
                    post.put("imageUrl", imageUrl);
                    post.put("likeCount", 0);
                    post.put("timestamp", System.currentTimeMillis());

                    db.collection("posts")
                            .add(post)
                            .addOnSuccessListener(doc -> {
                                txtContent.setText("");
                                imgPreview = null;

                                Toast.makeText(
                                        requireContext(),
                                        "Post created",
                                        Toast.LENGTH_SHORT
                                ).show();
                            });
                });
    }




    private String formatTimestamp(long millis) {
        return new SimpleDateFormat(
                "MMM dd, yyyy • hh:mm a",
                Locale.getDefault()
        ).format(new Date(millis));
    }
}
