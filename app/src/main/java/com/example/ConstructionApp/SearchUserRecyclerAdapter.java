package com.example.ConstructionApp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class SearchUserRecyclerAdapter
        extends FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder> {

    private final Context context;

    public SearchUserRecyclerAdapter(
            @NonNull FirestoreRecyclerOptions<UserModel> options,
            Context context
    ) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(
            @NonNull UserModelViewHolder holder,
            int position,
            @NonNull UserModel model
    ) {

        // 🔑 Get Firestore document ID (userId)
        String userId = getSnapshots().getSnapshot(position).getId();

        // Username (null-safe)
        if (model.getUsername() != null) {
            holder.usernameText.setText(model.getUsername());
        } else {
            holder.usernameText.setText("Unknown user");
        }

        // Mark current user
        if (userId.equals(FirebaseUtil.currentUserId())) {
            holder.usernameText.setText(holder.usernameText.getText() + " (Me)");
        }

        // Optional: show email or location instead of phone
        if (model.getEmail() != null) {
            holder.subText.setText(model.getEmail());
        } else if (model.getLocation() != null) {
            holder.subText.setText(model.getLocation());
        } else {
            holder.subText.setText("");
        }

        // Profile picture (safe)
        FirebaseUtil.getOtherProfilePicStorageRef(userId)
                .getDownloadUrl()
                .addOnSuccessListener(uri ->
                        AndroidUtil.setProfilePic(context, uri, holder.profilePic)
                )
                .addOnFailureListener(e ->
                        holder.profilePic.setImageResource(R.drawable.ic_profile_placeholder_foreground)
                );

        // Click → open chat
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, model, userId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.search_user_recycler_row, parent, false);
        return new UserModelViewHolder(view);
    }

    static class UserModelViewHolder extends RecyclerView.ViewHolder {

        TextView usernameText;
        TextView subText;
        ImageView profilePic;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            subText = itemView.findViewById(R.id.phone_text); // reuse TextView
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}

