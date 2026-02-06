package adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ConstructionApp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import clients.chat.ChatActivity;
import data.AndroidUtil;
import data.FirebaseUtil;
import models.ChatroomModel;
import models.UserModel;

public class RecentChatRecyclerAdapter
        extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    private final Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        // Reset UI (important for RecyclerView reuse)
        holder.usernameText.setText("");
        holder.lastMessageText.setText("");
        holder.lastMessageTime.setText("");
        holder.profilePic.setImageResource(R.drawable.ic_profile_placeholder_foreground);
        holder.itemView.setOnClickListener(null);

        Log.e("CHAT_DEBUG", "currentUserId = " + FirebaseUtil.currentUserId());

        String otherUserId = FirebaseUtil.getOtherUserId(model.getUserIds());
        if (otherUserId == null) return;

        // Try USERS first
        db.collection("users")
                .document(otherUserId)
                .get()
                .addOnSuccessListener(userSnap -> {
                    if (userSnap.exists()) {
                        bindUser(holder, model, userSnap, otherUserId);
                    } else {
                        // Fallback to WORKERS
                        db.collection("workers")
                                .document(otherUserId)
                                .get()
                                .addOnSuccessListener(workerSnap -> {
                                    if (workerSnap.exists()) {
                                        bindUser(holder, model, workerSnap, otherUserId);
                                    }
                                });
                    }
                });
    }

    /**
     * Binds user or worker data to the chat row
     */
    private void bindUser(
            ChatroomModelViewHolder holder,
            ChatroomModel model,
            DocumentSnapshot snap,
            String otherUserId
    ) {
        UserModel user = snap.toObject(UserModel.class);
        if (user == null) return;

        user.setUserId(otherUserId);

        // Username
        holder.usernameText.setText(user.getUsername());

        boolean sentByMe =
                model.getLastMessageSenderId() != null &&
                        model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());

        holder.lastMessageText.setText(
                sentByMe
                        ? "You: " + model.getLastMessage()
                        : model.getLastMessage()
        );

        // Time
        holder.lastMessageTime.setText(
                FirebaseUtil.timestampToString(model.getLastMessageTimestamp())
        );

        // Profile picture
        String profilePicUrl = snap.getString("profilePicUrl");
        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            Glide.with(context)
                    .load(profilePicUrl)
                    .placeholder(R.drawable.ic_profile_placeholder_foreground)
                    .circleCrop()
                    .into(holder.profilePic);
        }

        // Click → open chat
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, user, otherUserId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
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
            usernameText = itemView.findViewById(R.id.workertxtName);
            lastMessageText = itemView.findViewById(R.id.txtLastMessage);
            lastMessageTime = itemView.findViewById(R.id.txtTime);
            profilePic = itemView.findViewById(R.id.imgProfile);
        }
    }
}
