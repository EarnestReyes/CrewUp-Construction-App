package clients.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import adapters.PostAdapter;
import clients.chat.ChatActivity;
import data.FirebaseUtil;
import com.example.ConstructionApp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import app.Splash_activity;
import models.Post;

public class ProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private Button logout;
    private ImageView imgProfile, imgCoverPhoto;
    private TextView username, birthday, Gender, Location, Mobile, Social;
    private RecyclerView recyclerView;

    private ArrayList<Post> posts;
    private PostAdapter adapter;

    private ProgressBar progressLoading;
    private SwipeRefreshLayout swipeRefresh;

    private String currentUserProfilePicUrl;

    private boolean isFirstLoad = true;
    private boolean isRefreshing = false;
    private int lastItemCount = -1;

    // ================= IMAGE PICKERS =================
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null && isAdded()) {
                            Glide.with(requireContext())
                                    .load(uri)
                                    .circleCrop()
                                    .into(imgProfile);
                            FirebaseUtil.uploadProfilePic(requireContext(), uri);
                        }
                    }
            );

    private final ActivityResultLauncher<String> coverImagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null && isAdded()) {
                            Glide.with(requireContext())
                                    .load(uri)
                                    .centerCrop()
                                    .into(imgCoverPhoto);
                            FirebaseUtil.uploadCoverProfilePic(requireContext(), uri);
                        }
                    }
            );

    // ================= LIFECYCLE =================

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        logout = view.findViewById(R.id.logout_btn);
        imgProfile = view.findViewById(R.id.imgProfile);
        imgCoverPhoto = view.findViewById(R.id.imgCoverPhoto);
        username = view.findViewById(R.id.workertxtName);
        birthday = view.findViewById(R.id.birthday);
        Gender = view.findViewById(R.id.gender);
        Location = view.findViewById(R.id.location);
        Mobile = view.findViewById(R.id.mobile);
        Social = view.findViewById(R.id.social);

        progressLoading = view.findViewById(R.id.progressLoading);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        recyclerView = view.findViewById(R.id.recyclerPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        posts = new ArrayList<>();
        adapter = new PostAdapter(requireContext(), posts);
        recyclerView.setAdapter(adapter);

        progressLoading.setVisibility(View.VISIBLE);

        swipeRefresh.setColorSchemeResources(
                R.color.primary,
                R.color.icon_share,
                R.color.icon_comment
        );

        swipeRefresh.setOnRefreshListener(() -> {
            isRefreshing = true;
            progressLoading.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        });


        loadUserDetails();
        loadCurrentUserProfilePic();
        loadPosts();

        String uid = FirebaseUtil.currentUserId();
        if (uid != null && isAdded()) {
            FirebaseUtil.listenToProfilePic(requireContext(), imgProfile, uid);
            FirebaseUtil.CoverlistenToProfilePic(requireContext(), imgCoverPhoto, uid);
        }

        imgProfile.setOnClickListener(v -> permission(imagePickerLauncher));
        imgCoverPhoto.setOnClickListener(v -> permission(coverImagePickerLauncher));

        logout.setOnClickListener(v -> logout());

        return view;
    }

    // ================= USER DATA =================

    private void loadUserDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    username.setText(doc.getString("username"));
                    birthday.setText(doc.getString("Birthday"));
                    Gender.setText(doc.getString("Gender"));
                    Location.setText(doc.getString("location"));
                    Mobile.setText(doc.getString("Mobile Number"));
                    Social.setText(doc.getString("Social"));
                });
    }

    private void loadCurrentUserProfilePic() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc ->
                        currentUserProfilePicUrl = doc.getString("profilePicUrl")
                );
    }

    // ================= POSTS =================

    private void loadPosts() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            stopLoading();
            return;
        }

        db.collection("posts")
                .whereEqualTo("userId", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) {
                        stopLoading();
                        return;
                    }

                    int newCount = value.size();

                    // 🔒 If nothing changed and not refreshing → STOP
                    if (!isRefreshing && !isFirstLoad && newCount == lastItemCount) {
                        stopLoading();
                        return;
                    }

                    posts.clear();

                    for (QueryDocumentSnapshot doc : value) {

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

                        Post post = new Post(
                                user.getUid(),
                                doc.getString("userName"),
                                "",
                                doc.getString("content"),
                                timestamp,
                                currentUserProfilePicUrl,
                                doc.getString("imageUrl")
                        );

                        post.setPostId(doc.getId());
                        posts.add(post);
                    }

                    lastItemCount = newCount;
                    adapter.notifyDataSetChanged();

                    stopLoading();
                });
    }


    // ================= LOGOUT =================

    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Account Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (d, w) ->
                        FirebaseMessaging.getInstance().deleteToken()
                                .addOnCompleteListener(Task::isSuccessful)
                                .addOnSuccessListener(v -> {
                                    FirebaseUtil.logout();
                                    startActivity(new Intent(getContext(), Splash_activity.class));
                                    Toast.makeText(requireContext(),
                                            "Logout successful!", Toast.LENGTH_SHORT).show();
                                })
                )
                .setNegativeButton("No", null)
                .show();
    }

    private void permission(ActivityResultLauncher<String> launcher) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Media Permission")
                .setMessage("Allow app to access your gallery?")
                .setPositiveButton("Yes", (d, w) -> launcher.launch("image/*"))
                .setNegativeButton("No", null)
                .show();
    }
    private void stopLoading() {
        progressLoading.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        isFirstLoad = false;
        isRefreshing = false;
    }

}

