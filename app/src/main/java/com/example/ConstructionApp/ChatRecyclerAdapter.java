package com.example.ConstructionApp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ChatRecyclerAdapter
        extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {

    private final Context context;

    public ChatRecyclerAdapter(
            @NonNull FirestoreRecyclerOptions<ChatMessageModel> options,
            Context context
    ) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(
            @NonNull ChatModelViewHolder holder,
            int position,
            @NonNull ChatMessageModel model
    ) {

        // 🔄 RESET recycled views (CRITICAL)
        holder.leftChatLayout.setVisibility(View.GONE);
        holder.rightChatLayout.setVisibility(View.GONE);

        String senderId = model.getSenderId();

        if (senderId != null && senderId.equals(FirebaseUtil.currentUserId())) {
            // 👉 Message sent by ME
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatTextview.setText(model.getMessage());
        } else {
            // 👉 Message sent by OTHER USER
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatTextview.setText(model.getMessage());
        }
    }

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ChatModelViewHolder(view);
    }

    static class ChatModelViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftChatLayout, rightChatLayout;
        TextView leftChatTextview, rightChatTextview;

        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);

            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
        }
    }
}
