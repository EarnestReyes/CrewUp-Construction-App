package com.example.ConstructionApp;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FirebaseUtil {

    /* ---------------- AUTH ---------------- */

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
        return allUserCollectionReference().document(currentUserId());
    }

    // ✅ MISSING METHOD (FIX)
    public static DocumentReference getUserReference(String userId) {
        return allUserCollectionReference().document(userId);
    }

    /* ---------------- CHATROOMS ---------------- */

    public static CollectionReference allChatroomCollectionReference() {
        return FirebaseFirestore.getInstance().collection("chatrooms");
    }

    public static DocumentReference getChatroomReference(String chatroomId) {
        return FirebaseFirestore.getInstance()
                .collection("chatrooms")
                .document(chatroomId);
    }

    public static CollectionReference getChatroomMessageReference(String chatroomId) {
        return getChatroomReference(chatroomId).collection("chats");
    }

    public static String getChatroomId(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) return "";
        return userId1.compareTo(userId2) < 0
                ? userId1 + "_" + userId2
                : userId2 + "_" + userId1;
    }

    public static DocumentReference getOtherUserFromChatroom(List<String> userIds) {
        if (userIds == null || userIds.size() < 2) return null;

        if (userIds.get(0).equals(currentUserId())) {
            return getUserReference(userIds.get(1));
        } else {
            return getUserReference(userIds.get(0));
        }
    }

    /* ---------------- TIME ---------------- */

    // ✅ FIXED FORMAT (mm = minutes)
    public static String timestampToString(Timestamp timestamp) {
        if (timestamp == null) return "";
        return new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(timestamp.toDate());
    }

    /* ---------------- STORAGE ---------------- */

    public static StorageReference getCurrentProfilePicStorageRef() {
        return FirebaseStorage.getInstance()
                .getReference()
                .child("profile_pic")
                .child(currentUserId());
    }

    public static StorageReference getOtherProfilePicStorageRef(String otherUserId) {
        return FirebaseStorage.getInstance()
                .getReference()
                .child("profile_pic")
                .child(otherUserId);
    }

    /* ---------------- LOGOUT ---------------- */

    public static void logout() {
        FirebaseAuth.getInstance().signOut();
    }
}



