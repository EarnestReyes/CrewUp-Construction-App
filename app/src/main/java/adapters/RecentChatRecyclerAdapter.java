package adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ConstructionApp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import clients.chat.ChatActivity;
import data.AndroidUtil;
import data.FirebaseUtil;
import models.ChatroomModel;
import models.UserModel;

public class RecentChatRecyclerAdapter
        extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatVH> {

    private final Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public RecentChatRecyclerAdapter(
            @NonNull FirestoreRecyclerOptions<ChatroomModel> options,
            Context context
    ) {
        super(options);
        this.context = context;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return getSnapshots().getSnapshot(position).getId().hashCode();
    }

    @Override
    protected void onBindViewHolder(
            @NonNull ChatVH holder,
            int position,
            @NonNull ChatroomModel model
    ) {

        // ===== ALWAYS bind chat data first (NO async) =====
        boolean sentByMe =
                model.getLastMessageSenderId() != null &&
                        model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());

        holder.lastMessageText.setText(
                sentByMe
                        ? "You: " + model.getLastMessage()
                        : model.getLastMessage()
        );

        holder.lastMessageTime.setText(
                FirebaseUtil.timestampToString(model.getLastMessageTimestamp())
        );

        holder.usernameText.setText("Unknown User");
        holder.profilePic.setImageResource(R.drawable.ic_profile_placeholder_foreground);

        String otherUserId = FirebaseUtil.getOtherUserId(model.getUserIds());
        if (otherUserId == null) {
            holder.usernameText.setText("Unknown user");
            return;
        }

        // Tag holder to prevent recycle bug
        holder.itemView.setTag(otherUserId);

        // ===== Load USER =====
        db.collection("users")
                .document(otherUserId)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!otherUserId.equals(holder.itemView.getTag())) return;

                    if (snap.exists()) {
                        bindUser(holder, snap, otherUserId);
                    } else {
                        // ===== Fallback WORKER =====
                        db.collection("workers")
                                .document(otherUserId)
                                .get()
                                .addOnSuccessListener(workerSnap -> {
                                    if (!otherUserId.equals(holder.itemView.getTag())) return;
                                    if (workerSnap.exists()) {
                                        bindUser(holder, workerSnap, otherUserId);
                                    }
                                });
                    }
                });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    private void bindUser(
            ChatVH holder,
            com.google.firebase.firestore.DocumentSnapshot snap,
            String otherUserId
    ) {
        UserModel user = snap.toObject(UserModel.class);
        if (user == null) return;

        user.setUserId(otherUserId);

        holder.usernameText.setText(user.getUsername());

        String profilePicUrl = snap.getString("profilePicUrl");
        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            Glide.with(context)
                    .load(profilePicUrl)
                    .placeholder(R.drawable.ic_profile_placeholder_foreground)
                    .circleCrop()
                    .into(holder.profilePic);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, user, otherUserId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public ChatVH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_message, parent, false);
        return new ChatVH(view);
    }

    static class ChatVH extends RecyclerView.ViewHolder {

        TextView usernameText, lastMessageText, lastMessageTime;
        ImageView profilePic;

        ChatVH(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.workertxtName);
            lastMessageText = itemView.findViewById(R.id.txtLastMessage);
            lastMessageTime = itemView.findViewById(R.id.txtTime);
            profilePic = itemView.findViewById(R.id.imgProfile);
        }
    }
}
