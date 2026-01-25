package com.example.ConstructionApp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

public class SearchUserRecyclerAdapter
        extends FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserViewHolder> {

    private final Context context;
    private final boolean isRecent;

    public SearchUserRecyclerAdapter(
            @NonNull FirestoreRecyclerOptions<UserModel> options,
            Context context,
            boolean isRecent
    ) {
        super(options);
        this.context = context;
        this.isRecent = isRecent;
    }

    @Override
    protected void onBindViewHolder(
            @NonNull UserViewHolder holder,
            int position,
            @NonNull UserModel model
    ) {

        String userId = getSnapshots().getSnapshot(position).getId();

        /* ---------- USERNAME ---------- */
        String username = model.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = "Unknown user";
        }
        holder.username.setText(username);

        /* ---------- EMAIL / LOCATION ---------- */
        String email = model.getEmail();
        String location = model.getLocation();

        if (email != null && !email.trim().isEmpty()) {
            holder.subText.setText(email);
            holder.subText.setVisibility(View.VISIBLE);
        } else if (location != null && !location.trim().isEmpty()) {
            holder.subText.setText(location);
            holder.subText.setVisibility(View.VISIBLE);
        } else {
            holder.subText.setVisibility(View.GONE);
        }

        /* ---------- PROFILE PICTURE ---------- */
        holder.profilePic.setImageResource(
                R.drawable.ic_profile_placeholder_foreground
        );

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null && snapshot.exists()) {
                        String url = snapshot.getString("profilePicUrl");
                        if (url != null && !url.isEmpty()) {
                            Glide.with(context)
                                    .load(url)
                                    .placeholder(R.drawable.ic_profile_placeholder_foreground)
                                    .circleCrop()
                                    .into(holder.profilePic);
                        } else {
                            holder.profilePic.setImageResource(
                                    R.drawable.ic_profile_placeholder_foreground
                            );
                        }
                    }
                })
                .addOnFailureListener(e ->
                        holder.profilePic.setImageResource(
                                R.drawable.ic_profile_placeholder_foreground
                        )
                );

        /* ---------- CLICK → OPEN CHAT + SAVE RECENT ---------- */
        holder.itemView.setOnClickListener(v -> {
            FirebaseUtil.addToRecentSearch(model, userId);

            Intent intent = new Intent(context, ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, model, userId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });

        /* ---------- REMOVE BUTTON (ONLY FOR RECENT) ---------- */
        if (isRecent) {
            holder.removeBtn.setVisibility(View.VISIBLE);
            holder.removeBtn.setOnClickListener(v ->
                    FirebaseUtil.removeFromRecentSearch(userId)
            );
        } else {
            holder.removeBtn.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.search_user_recycler_row, parent, false);
        return new UserViewHolder(view);
    }

    static class UserViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {

        TextView username, subText;
        ImageView profilePic;
        ImageButton removeBtn;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.user_name_text);
            subText = itemView.findViewById(R.id.phone_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
            removeBtn = itemView.findViewById(R.id.remove_btn);
        }
    }
}

