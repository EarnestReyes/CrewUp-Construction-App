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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import data.FirebaseUtil;
import com.example.ConstructionApp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import app.Splash_activity;

public class ProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private Button logout;
    private ImageView imgProfile, imgCoverPhoto;
    private Uri selectedImageUri;
    private TextView username, birthday, Gender, Location, Mobile, Social;

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

    private final ActivityResultLauncher<String> CoverimagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null && isAdded()) {

                            // Show preview immediately (good UX)
                            Glide.with(requireContext())
                                    .load(uri)
                                    .centerCrop()
                                    .into(imgCoverPhoto);

                            // Upload to Cloudinary
                            FirebaseUtil.uploadCoverProfilePic(
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
        username = view.findViewById(R.id.workertxtName);
        imgCoverPhoto = view.findViewById(R.id.imgCoverPhoto);
        birthday = view.findViewById(R.id.birthday);
        Gender = view.findViewById(R.id.gender);
        Location = view.findViewById(R.id.location);
        Mobile = view.findViewById(R.id.mobile);
        Social = view.findViewById(R.id.social);

        loaddetails();

        String uid = FirebaseUtil.currentUserId();
        if (uid != null && isAdded()) {
            FirebaseUtil.listenToProfilePic(
                    requireContext(),
                    imgProfile,
                    uid
            );
        }

        String uidc = FirebaseUtil.currentUserId();
        if (uidc != null && isAdded()) {
            FirebaseUtil.CoverlistenToProfilePic(
                    requireContext(),
                    imgCoverPhoto,
                    uidc
            );
        }

        imgCoverPhoto.setOnClickListener(v -> {

            permission(CoverimagePickerLauncher);

        });

        imgProfile.setOnClickListener(v -> {

            permission(imagePickerLauncher);
        });


        logout.setOnClickListener(v -> {
            logout();
        });
            return view;
        }

    private void loaddetails() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("username");
                        String Birthday = documentSnapshot.getString("Birthday");
                        String gender = documentSnapshot.getString("Gender");
                        String location = documentSnapshot.getString("location");
                        String mobile = documentSnapshot.getString("Mobile Number");
                        String socials = documentSnapshot.getString("Social");
                        if (name != null) {
                            username.setText(name);
                            birthday.setText(Birthday);
                            Gender.setText(gender);
                            Location.setText(location);
                            Mobile.setText(mobile);
                            Social.setText(socials);
                        }
      }});
    }
    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Account Logout")
                .setMessage("Are you sure you want to logout from this account?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                FirebaseUtil.logout();
                                Intent intent = new Intent(getContext(), Splash_activity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                    });
                    Toast.makeText(requireContext(), "Logout successful!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) ->
                        Toast.makeText(requireContext(), "Your wish is my command", Toast.LENGTH_SHORT).show())
                .show();
    }
    private void permission(ActivityResultLauncher act) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Media Permission")
                .setMessage("Allow app to access your gallery?")
                .setCancelable(false)
                .setPositiveButton("Yes", (d, w) -> act.launch("image/*"))
                .setNegativeButton("No", (d, w) ->
                        Toast.makeText(requireContext(), "We need permission of camera to proceed", Toast.LENGTH_SHORT).show())
                .show();
    }
}
