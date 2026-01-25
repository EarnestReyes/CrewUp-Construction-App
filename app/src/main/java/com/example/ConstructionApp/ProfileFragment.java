package com.example.ConstructionApp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

public class ProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private Button logout;
    private ImageView imgProfile;
    private Uri selectedImageUri;
    private TextView username;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null && isAdded()) {

                            // Show preview immediately (good UX)
                            Glide.with(requireContext())
                                    .load(uri)
                                    .circleCrop()
                                    .into(imgProfile);

                            // Upload to Cloudinary
                            FirebaseUtil.uploadProfilePic(
                                    requireContext(),
                                    uri
                            );
                        }
                    }
            );

    public ProfileFragment() {

    }

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
        username = view.findViewById(R.id.txtName);

        loadUsername();

        String uid = FirebaseUtil.currentUserId();
        if (uid != null && isAdded()) {
            FirebaseUtil.listenToProfilePic(
                    requireContext(),
                    imgProfile,
                    uid
            );
        }

        imgProfile.setOnClickListener(v -> {

            //add permisions first
            imagePickerLauncher.launch("image/*");
        });

        /*
        to see other users picture

        FirebaseUtil.listenToProfilePic(
        context,
        holder.profilePic,
        userId
);
         */

        logout.setOnClickListener(v -> {
            logout();
        });
            return view;
        }

    private void loadUsername() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("username");
                        if (name != null) {
                            username.setText(name);
                        }
      }});
    }
    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Account Logout")
                .setMessage("Are you sure you want to logout from this account?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseUtil.logout();
                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), GetStartedActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        getActivity().finish();
                    }
                    Toast.makeText(requireContext(), "Logout successful!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) ->
                        Toast.makeText(requireContext(), "Your wish is my command", Toast.LENGTH_SHORT).show())
                .show();
    }
}
