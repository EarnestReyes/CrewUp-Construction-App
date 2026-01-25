package com.example.ConstructionApp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;

import java.util.Arrays;

public class ChatActivity extends AppCompatActivity {

    private String otherUserId;
    private UserModel otherUser;

    private String chatroomId;
    private ChatRecyclerAdapter adapter;

    private EditText messageInput;
    private ImageButton sendMessageBtn, backBtn;
    private TextView otherUsername;
    private RecyclerView recyclerView;
    private ImageView profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        otherUserId = getIntent().getStringExtra("userId");

        Log.d("CHAT_DEBUG", "ChatActivity started with userId=" + otherUserId);

        if (otherUserId == null || otherUserId.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        profilePic = findViewById(R.id.profile_pic_image_view);

        backBtn.setOnClickListener(v -> finish());

        chatroomId = FirebaseUtil.getChatroomId(
                FirebaseUtil.currentUserId(),
                otherUserId
        );

        loadOtherUser();
        setupChatRecyclerView();
        getOrCreateChatroomModel();

        sendMessageBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessageToUser(message);
            }
        });
    }

    /* ---------------- LOAD USER ---------------- */

    private void loadOtherUser() {
        FirebaseUtil.getUserReference(otherUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    otherUser = snapshot.toObject(UserModel.class);
                    if (otherUser == null) return;

                    otherUser.setUserId(snapshot.getId());
                    otherUsername.setText(otherUser.getUsername());

                    String url = snapshot.getString("profilePicUrl");
                    if (url != null && !url.isEmpty()) {
                        Glide.with(this)
                                .load(url)
                                .placeholder(R.drawable.ic_profile_placeholder_foreground)
                                .circleCrop()
                                .into(profilePic);
                    } else {
                        profilePic.setImageResource(
                                R.drawable.ic_profile_placeholder_foreground
                        );
                    }
                });
    }

    /* ---------------- CHAT LIST ---------------- */

    private void setupChatRecyclerView() {
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options =
                new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                        .setQuery(query, ChatMessageModel.class)
                        .build();

        adapter = new ChatRecyclerAdapter(options, this);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    /* ---------------- SEND MESSAGE ---------------- */

    private void sendMessageToUser(String message) {
        Timestamp now = Timestamp.now();

        FirebaseUtil.getChatroomReference(chatroomId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    ChatroomModel model = snapshot.toObject(ChatroomModel.class);

                    if (model == null) {
                        model = new ChatroomModel(
                                chatroomId,
                                Arrays.asList(
                                        FirebaseUtil.currentUserId(),
                                        otherUserId
                                ),
                                message,
                                FirebaseUtil.currentUserId(),
                                now
                        );
                    } else {
                        model.setLastMessage(message);
                        model.setLastMessageSenderId(FirebaseUtil.currentUserId());
                        model.setLastMessageTimestamp(now);
                    }

                    FirebaseUtil.getChatroomReference(chatroomId)
                            .set(model)
                            .addOnSuccessListener(unused -> {

                                ChatMessageModel chatMessage =
                                        new ChatMessageModel(
                                                message,
                                                FirebaseUtil.currentUserId(),
                                                now
                                        );

                                FirebaseUtil.getChatroomMessageReference(chatroomId)
                                        .add(chatMessage)
                                        .addOnSuccessListener(ref ->
                                                messageInput.setText("")
                                        );
                            });
                });
    }

    /* ---------------- CREATE CHATROOM ---------------- */

    private void getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        ChatroomModel chatroomModel = new ChatroomModel(
                                chatroomId,
                                Arrays.asList(
                                        FirebaseUtil.currentUserId(),
                                        otherUserId
                                ),
                                "",
                                "",
                                Timestamp.now()
                        );

                        FirebaseUtil.getChatroomReference(chatroomId)
                                .set(chatroomModel);
                    }
                });
    }

    /* ---------------- NOTIFICATION ---------------- */

    public void notifications(String user, String message) {
        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            "CrewUp",
                            "CrewUp Notifications",
                            NotificationManager.IMPORTANCE_DEFAULT
                    );
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "CrewUp")
                        .setSmallIcon(R.drawable.crewup_logo)
                        .setContentTitle(user)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        manager.notify(1, builder.build());
    }

    /* ---------------- LIFECYCLE ---------------- */

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}

