package com.example.ConstructionApp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class AndroidUtil {

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    // ✅ Pass ONLY fields that exist in UserModel
    public static void passUserModelAsIntent(Intent intent, UserModel model, String userId) {
        intent.putExtra("username", model.getUsername());
        intent.putExtra("email", model.getEmail());
        intent.putExtra("location", model.getLocation());
        intent.putExtra("userId", userId); // document ID
    }

    // ✅ Rebuild UserModel safely from Intent
    public static UserModel getUserModelFromIntent(Intent intent) {
        UserModel userModel = new UserModel();

        // These setters exist implicitly via reflection (Firestore-style)
        // so we use local variables instead of forcing setters
        try {
            java.lang.reflect.Field username = UserModel.class.getDeclaredField("username");
            username.setAccessible(true);
            username.set(userModel, intent.getStringExtra("username"));

            java.lang.reflect.Field email = UserModel.class.getDeclaredField("email");
            email.setAccessible(true);
            email.set(userModel, intent.getStringExtra("email"));

            java.lang.reflect.Field location = UserModel.class.getDeclaredField("location");
            location.setAccessible(true);
            location.set(userModel, intent.getStringExtra("location"));
        } catch (Exception ignored) {}

        return userModel;
    }

    // ✅ Safe profile image loader
    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView) {
        Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.ic_profile_placeholder_foreground)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView);
    }
}
