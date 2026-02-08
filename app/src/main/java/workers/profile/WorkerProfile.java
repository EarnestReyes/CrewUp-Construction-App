package workers.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ConstructionApp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import adapters.PostAdapter;
import app.Splash_activity;
import data.FirebaseUtil;
import models.Post;

public class WorkerProfile extends Fragment {

    private FirebaseFirestore db;
    private ListenerRegistration postsListener;
    private Button logout;
    private ImageView imgProfile, imgCoverPhoto;
    private TextView username, birthday, Gender, Location, Mobile, Social;
    private RecyclerView recyclerView;

    private ArrayList<Post> posts;
    private PostAdapter adapter;

    private ProgressBar progressLoading;
    private SwipeRefreshLayout swipeRefresh;

    private String currentUserProfilePicUrl;

    private SwitchCompat switchLocation;
    private FusedLocationProviderClient fusedLocationClient;

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

        switchLocation = view.findViewById(R.id.switchLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        progressLoading = view.findViewById(R.id.progressLoading);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        recyclerView = view.findViewById(R.id.recyclerPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        posts = new ArrayList<>();
        adapter = new PostAdapter(requireContext(), posts);
        recyclerView.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(
                R.color.primary,
                R.color.icon_share,
                R.color.icon_comment
        );

        swipeRefresh.setOnRefreshListener(() -> {
            progressLoading.setVisibility(View.VISIBLE);
            loadPosts();
        });

        progressLoading.setVisibility(View.VISIBLE);

        loadUserDetails();
        loadCurrentUserProfilePic();
        loadPosts();
        setupLocationSwitch(switchLocation);

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

        if (postsListener != null) postsListener.remove();

        postsListener = db.collection("posts")
                .whereEqualTo("userId", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    posts.clear();

                    if (error == null && value != null) {
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
                    }

                    adapter.notifyDataSetChanged();
                    stopLoading();
                });
    }

    private void stopLoading() {
        if (isAdded()) {
            swipeRefresh.setRefreshing(false);
            progressLoading.setVisibility(View.GONE);
        }
    }

    // ================= LOCATION SWITCH =================

    private void setupLocationSwitch(SwitchCompat switchLocation) {

        String uid = FirebaseUtil.currentUserId();
        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    Boolean enabled = doc.getBoolean("shareLocation");
                    switchLocation.setChecked(enabled != null && enabled);
                });

        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {

            Map<String, Object> update = new HashMap<>();
            update.put("shareLocation", isChecked);

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .set(update, SetOptions.merge());

            if (isChecked) {
                fetchAndSaveLocation();
            } else {
                stopLocationUpdates();
            }
        });
    }

    private void fetchAndSaveLocation() {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) saveLocationAsync(location);
                });
    }

    private void saveLocationAsync(Location location) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        double lat = location.getLatitude();
        double lng = location.getLongitude();

        new Thread(() -> {
            String address = "Unknown location";

            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(lat, lng, 1);
                if (list != null && !list.isEmpty()) {
                    Address a = list.get(0);
                    address = a.getLocality() + ", " + a.getAdminArea();
                }
            } catch (IOException ignored) {}

            String finalAddress = address;

            requireActivity().runOnUiThread(() -> {
                Map<String, Object> data = new HashMap<>();
                data.put("lat", lat);
                data.put("lng", lng);
                data.put("location", finalAddress);
                data.put("locationUpdatedAt", System.currentTimeMillis());

                db.collection("users")
                        .document(user.getUid())
                        .set(data, SetOptions.merge());

                Log.d("LOCATION", "Saved location");
            });
        }).start();
    }

    private void stopLocationUpdates() {
        Map<String, Object> update = new HashMap<>();
        update.put("lat", null);
        update.put("lng", null);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseUtil.currentUserId())
                .set(update, SetOptions.merge());
    }

    // ================= LOGOUT =================

    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Account Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (d, w) ->
                        FirebaseMessaging.getInstance().deleteToken()
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

    @Override
    public void onStop() {
        super.onStop();
        if (postsListener != null) {
            postsListener.remove();
            postsListener = null;
        }
    }
}
