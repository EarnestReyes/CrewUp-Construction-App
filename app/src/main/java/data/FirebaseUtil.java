package data;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.ConstructionApp.R;
import com.google.firebase.Firebase;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import models.UserModel;

public class FirebaseUtil {

    /* ---------------- AUH ---------------- */

    public static String currentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    public static boolean isLoggedIn() {
        return currentUserId() != null;
    }

    /* ---------------- USERS ---------------- */

    public static CollectionReference allUserCollectionReference() {
        return FirebaseFirestore.getInstance().collection("users");
    }

    public static DocumentReference currentUserDetails() {
        String uid = currentUserId();
        if (uid == null) return null;
        return allUserCollectionReference().document(uid);
    }

    public static DocumentReference getUserReference(String userId) {
        if (userId == null || userId.isEmpty()) return null;
        return allUserCollectionReference().document(userId);
    }

    /* ---------------- CHATROOMS ---------------- */

    public static CollectionReference allChatroomCollectionReference() {
        return FirebaseFirestore.getInstance().collection("chatrooms");
    }

    public static DocumentReference getChatroomReference(String chatroomId) {
        if (chatroomId == null || chatroomId.isEmpty()) return null;
        return FirebaseFirestore.getInstance()
                .collection("chatrooms")
                .document(chatroomId);
    }

    public static CollectionReference getChatroomMessageReference(String chatroomId) {
        DocumentReference ref = getChatroomReference(chatroomId);
        return ref != null ? ref.collection("chats") : null;
    }

    public static String getChatroomId(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) return "";
        return userId1.compareTo(userId2) < 0
                ? userId1 + "_" + userId2
                : userId2 + "_" + userId1;
    }

    public static DocumentReference getOtherUserFromChatroom(List<String> userIds) {
        if (userIds == null || userIds.size() < 2) return null;

        String currentUid = currentUserId();
        if (currentUid == null) return null;

        return userIds.get(0).equals(currentUid)
                ? allUserCollectionReference().document(userIds.get(1))
                : allUserCollectionReference().document(userIds.get(0));
    }

    /* ---------------- TIME ---------------- */

    public static String timestampToString(Timestamp timestamp) {
        if (timestamp == null) return "";
        return new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(timestamp.toDate());
    }

    /* ---------------- STORAGE ---------------- */

    public static StorageReference getCurrentProfilePicStorageRef() {
        String uid = currentUserId();
        if (uid == null) return null;

        return FirebaseStorage.getInstance()
                .getReference()
                .child("profile_pic")
                .child(uid);
    }

    public static void uploadProfilePic(
            Context context,
            Uri imageUri
    ) {

        String uid = currentUserId();
        if (uid == null || imageUri == null) return;

        MediaManager.get().upload(imageUri)
                .unsigned("CrewUp")
                .option("folder", "profile_pictures")
                .callback(new UploadCallback() {

                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(context, "Uploading profile picture…", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {

                        String imageUrl = resultData.get("secure_url").toString();

                        Map<String, Object> data = new HashMap<>();
                        data.put("profilePicUrl", imageUrl);

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .set(data, SetOptions.merge());
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                })
                .dispatch();
    }

    public static void uploadCoverProfilePic(
            Context context,
            Uri imageUri
    ) {

        String uid = currentUserId();
        if (uid == null || imageUri == null) return;

        MediaManager.get().upload(imageUri)
                .unsigned("CrewUp")
                .option("folder", "profile_pictures")
                .callback(new UploadCallback() {

                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(context, "Uploading cover picture…", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {

                        String imageUrl = resultData.get("secure_url").toString();

                        Map<String, Object> data = new HashMap<>();
                        data.put("CoverprofilePicUrl", imageUrl);

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .set(data, SetOptions.merge());
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                })
                .dispatch();
    }

    public static void listenToProfilePic(
            Context context,
            ImageView imageView,
            String userId
    ) {
        if (userId == null) return;
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .addSnapshotListener((snapshot, e) -> {

                    if (e != null || snapshot == null || !snapshot.exists()) return;

                    String url = snapshot.getString("profilePicUrl");
                    if (url != null && !url.isEmpty()) {

                        Glide.with(context)
                                .load(url)
                                .placeholder(R.drawable.ic_profile_placeholder_foreground)
                                .circleCrop()
                                .into(imageView);
                    }
                });
    }

    public static void CoverlistenToProfilePic(
            Context context,
            ImageView imageView,
            String userId
    ) {
        if (userId == null) return;
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .addSnapshotListener((snapshot, e) -> {

                    if (e != null || snapshot == null || !snapshot.exists()) return;

                    String url = snapshot.getString("CoverprofilePicUrl");
                    if (url != null && !url.isEmpty()) {

                        Glide.with(context)
                                .load(url)
                                .placeholder(R.color.button_secondary)
                                .centerCrop()
                                .into(imageView);
                    }
                });
    }

    /* ---------------- LOGOUT ---------------- */

    public static void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    /* ---------------- RECENT SEARCH ---------------- */

    public static void addToRecentSearch(UserModel user, String userId) {

        String currentUid = currentUserId();
        if (currentUid == null || user == null || userId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());   // ✅ kept
        data.put("location", user.getLocation());
        data.put("timestamp", Timestamp.now());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUid)
                .collection("recent_searches")
                .document(userId)
                .set(data);
    }

    public static void removeFromRecentSearch(String targetUserId) {

        String currentUid = currentUserId();
        if (currentUid == null || targetUserId == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUid)
                .collection("recent_searches")
                .document(targetUserId)
                .delete();
    }

    public static void clearAllRecentSearches() {

        String uid = currentUserId();
        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("recent_searches")
                .get()
                .addOnSuccessListener(query -> {
                    for (var doc : query.getDocuments()) {
                        doc.getReference().delete();
                    }
                });
    }

    public static String getOtherUserId(List<String> userIds) {
        if (userIds == null || userIds.size() != 2) return null;

        String currentUserId = currentUserId();
        if (currentUserId == null) return null;

        if (userIds.get(0).equals(currentUserId)) {
            return userIds.get(1);
        } else {
            return userIds.get(0);
        }
    }

    public static void UploadServiceType(
            Context context,
            Uri imageUri
    ) {

        String uid = currentUserId();
        if (uid == null || imageUri == null) return;

        MediaManager.get().upload(imageUri)
                .unsigned("CrewUp")
                .option("folder", "profile_pictures")
                .callback(new UploadCallback() {

                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(context, "Uploading picture", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {

                        String imageUrl = resultData.get("secure_url").toString();

                        Map<String, Object> data = new HashMap<>();
                        data.put("Service_info_image", imageUrl);

                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        DocumentReference ref = db.collection("BookingOrder").document();
                        String projectId = ref.getId();

                        FirebaseFirestore.getInstance()
                                .collection("BookingOrder")
                                .document(projectId)
                                .set(data, SetOptions.merge());
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                })
                .dispatch();
    }
    public static void listentoServiceInfo(
            Context context,
            ImageView imageView,
            String userId
    ) {
        if (userId == null) return;
        FirebaseFirestore.getInstance()
                .collection("BookingOrder")
                .document(userId)
                .addSnapshotListener((snapshot, e) -> {

                    if (e != null || snapshot == null || !snapshot.exists()) return;

                    String url = snapshot.getString("Service_info_image");
                    if (url != null && !url.isEmpty()) {

                        Glide.with(context)
                                .load(url)
                                .into(imageView);
                    }
                });
    }


}



