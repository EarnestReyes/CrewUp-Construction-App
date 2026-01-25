package com.example.ConstructionApp;

import android.accounts.AccountManagerFuture;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RecentChatRecyclerAdapter
        extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    private final Context context;

    public RecentChatRecyclerAdapter(
            @NonNull FirestoreRecyclerOptions<ChatroomModel> options,
            Context context
    ) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(
            @NonNull ChatroomModelViewHolder holder,
            int position,
            @NonNull ChatroomModel model
    ) {

        holder.itemView.setOnClickListener(null);
        holder.profilePic.setImageResource(
                R.drawable.ic_profile_placeholder_foreground
        );

        DocumentReference otherUserRef =
                FirebaseUtil.getOtherUserFromChatroom(model.getUserIds());

        if (otherUserRef == null) return;

        otherUserRef.get().addOnSuccessListener(snapshot -> {

            if (!snapshot.exists()) return;

            UserModel otherUserModel = snapshot.toObject(UserModel.class);
            if (otherUserModel == null) return;

            String otherUserId = snapshot.getId();
            otherUserModel.setUserId(otherUserId);

            holder.usernameText.setText(otherUserModel.getUsername());

            boolean lastMessageSentByMe =
                    model.getLastMessageSenderId() != null &&
                            model.getLastMessageSenderId()
                                    .equals(FirebaseUtil.currentUserId());

            holder.lastMessageText.setText(
                    lastMessageSentByMe
                            ? "You: " + model.getLastMessage()
                            : model.getLastMessage()
            );

            holder.lastMessageTime.setText(
                    FirebaseUtil.timestampToString(
                            model.getLastMessageTimestamp()
                    )
            );

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(otherUserId)
                    .get()
                    .addOnSuccessListener(userSnap -> {
                        if (userSnap.exists()) {
                            String url = userSnap.getString("profilePicUrl");
                            if (url != null && !url.isEmpty()) {
                                Glide.with(context)
                                        .load(url)
                                        .placeholder(R.drawable.ic_profile_placeholder_foreground)
                                        .circleCrop()
                                        .into(holder.profilePic);
                            }
                        }
                    });

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                AndroidUtil.passUserModelAsIntent(
                        intent,
                        otherUserModel,
                        otherUserId
                );
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        });
    }


    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_message, parent, false);
        return new ChatroomModelViewHolder(view);
    }

    static class ChatroomModelViewHolder extends RecyclerView.ViewHolder {

        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilePic;

        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.txtName);
            lastMessageText = itemView.findViewById(R.id.txtLastMessage);
            lastMessageTime = itemView.findViewById(R.id.txtTime);
            profilePic = itemView.findViewById(R.id.imgProfile);
        }
    }
}
