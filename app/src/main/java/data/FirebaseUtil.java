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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import models.UserModel;

public class FirebaseUtil {

    /* ================= AUTH ================= */

    public static String currentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    public static boolean isLoggedIn() {
        return currentUserId() != null;
    }

    /* ================= USERS ================= */

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

    /* ================= CHATROOMS ================= */

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

    /* ================= TIME ================= */

    public static String timestampToString(Timestamp timestamp) {
        if (timestamp == null) return "";
        return new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(timestamp.toDate());
    }

    /* ================= PROFILE UPLOADS ================= */

    public static void uploadProfilePic(Context context, Uri imageUri) {
        String uid = currentUserId();
        if (uid == null || imageUri == null) return;

        Context appCtx = context.getApplicationContext();

        MediaManager.get().upload(imageUri)
                .unsigned("CrewUp")
                .option("folder", "profile_pictures")
                .callback(new UploadCallback() {

                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(appCtx,
                                "Uploading profile picture…",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {

                        String imageUrl = resultData.get("secure_url").toString();

                        Map<String, Object> data = new HashMap<>();
                        data.put("profilePicUrl", imageUrl);

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .set(data, SetOptions.merge())
                                .addOnFailureListener(e ->
                                        Toast.makeText(appCtx,
                                                "Failed to save profile picture",
                                                Toast.LENGTH_SHORT).show()
                                );
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(appCtx,
                                "Profile upload failed",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    public static void uploadCoverProfilePic(Context context, Uri imageUri) {
        String uid = currentUserId();
        if (uid == null || imageUri == null) return;

        Context appCtx = context.getApplicationContext();

        MediaManager.get().upload(imageUri)
                .unsigned("CrewUp")
                .option("folder", "profile_pictures")
                .callback(new UploadCallback() {

                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(appCtx,
                                "Uploading cover picture…",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

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
                        Toast.makeText(appCtx,
                                "Cover upload failed",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    /* ================= PROFILE LISTENERS ================= */

    public static void listenToProfilePic(Context context, ImageView imageView, String userId) {
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

    public static void coverListenToProfilePic(Context context, ImageView imageView, String userId) {
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

    /* ================= LOGOUT ================= */

    public static void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    /* ================= RECENT SEARCH ================= */

    public static void addToRecentSearch(UserModel user, String userId) {
        String currentUid = currentUserId();
        if (currentUid == null || user == null || userId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
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
                .addOnSuccessListener(query ->
                        query.getDocuments().forEach(doc -> doc.getReference().delete())
                );
    }

    public static String getOtherUserId(List<String> userIds) {
        if (userIds == null || userIds.size() != 2) return null;

        String currentUid = currentUserId();
        if (currentUid == null) return null;

        return userIds.get(0).equals(currentUid)
                ? userIds.get(1)
                : userIds.get(0);
    }

    /* ================= GENERIC IMAGE UPLOAD ================= */

    public interface ImageUploadCallback {
        void onUploaded(String imageUrl);
    }

    public static void ImagePost(Context context, Uri imageUri, ImageUploadCallback callback) {
        if (imageUri == null) return;

        Context appCtx = context.getApplicationContext();

        MediaManager.get().upload(imageUri)
                .unsigned("CrewUp")
                .option("folder", "post_images")
                .callback(new UploadCallback() {

                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(appCtx,
                                "Uploading image…",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        if (callback != null) {
                            callback.onUploaded(resultData.get("secure_url").toString());
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(appCtx,
                                "Image upload failed",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }
    /* ================= SERVICE IMAGE UPLOAD ================= */

    public static void UploadServiceType(Context context,
                                         String filePath,
                                         OnImageUploaded callback) {

        String uid = currentUserId();
        if (uid == null || filePath == null) return;

        Context appCtx = context.getApplicationContext();

        MediaManager.get().upload(filePath)   // ✅ USE FILE PATH
                .unsigned("CrewUp")
                .option("folder", "service_photos")
                .callback(new UploadCallback() {

                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(appCtx,
                                "Uploading picture…",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId,
                                           long bytes,
                                           long totalBytes) {
                        // Optional: add progress bar later
                    }

                    @Override
                    public void onSuccess(String requestId,
                                          Map resultData) {

                        if (resultData == null ||
                                resultData.get("secure_url") == null) {
                            Toast.makeText(appCtx,
                                    "Upload failed",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String imageUrl =
                                resultData.get("secure_url").toString();

                        // 🔥 Delete temp file after upload
                        new File(filePath).delete();

                        // Return URL to caller
                        if (callback != null) {
                            callback.onUploaded(imageUrl);
                        }

                        // Save to Firestore
                        FirebaseFirestore db =
                                FirebaseFirestore.getInstance();

                        db.collection("BookingOrder")
                                .whereEqualTo("userId", uid)
                                .whereEqualTo("status", "pending")
                                .limit(1)
                                .get()
                                .addOnSuccessListener(q -> {

                                    if (q.isEmpty()) return;

                                    String projectId =
                                            q.getDocuments()
                                                    .get(0)
                                                    .getId();

                                    Map<String, Object> data =
                                            new HashMap<>();
                                    data.put("photos",
                                            FieldValue.arrayUnion(imageUrl));

                                    db.collection("BookingOrder")
                                            .document(projectId)
                                            .set(data, SetOptions.merge());
                                });
                    }

                    @Override
                    public void onError(String requestId,
                                        ErrorInfo error) {

                        Toast.makeText(appCtx,
                                "Upload failed",
                                Toast.LENGTH_SHORT).show();

                        // Cleanup temp file
                        new File(filePath).delete();
                    }

                    @Override
                    public void onReschedule(String requestId,
                                             ErrorInfo error) {
                    }
                })
                .dispatch();
    }
    // 👇 ADD THIS INTERFACE
    public interface OnImageUploaded {
        void onUploaded(String imageUrl);
    }

}
