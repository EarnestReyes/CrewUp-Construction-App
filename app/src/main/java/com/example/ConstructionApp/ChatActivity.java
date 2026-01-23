package com.example.ConstructionApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.Arrays;

public class ChatActivity extends AppCompatActivity {

    private String otherUserId;
    private UserModel otherUser;

    private String chatroomId;
    private ChatroomModel chatroomModel;
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

        // ✅ Get userId from intent
        otherUserId = getIntent().getStringExtra("userId");

        if (otherUserId == null) {
            finish();
            return;
        }

        chatroomId = FirebaseUtil.getChatroomId(
                FirebaseUtil.currentUserId(),
                otherUserId
        );

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        profilePic = findViewById(R.id.profile_pic_image_view);

        backBtn.setOnClickListener(v -> onBackPressed());

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

    private void loadOtherUser() {
        FirebaseUtil.getUserReference(otherUserId).get()
                .addOnSuccessListener(snapshot -> {
                    otherUser = snapshot.toObject(UserModel.class);
                    if (otherUser == null) return;

                    otherUsername.setText(otherUser.getUsername());

                    FirebaseUtil.getOtherProfilePicStorageRef(otherUserId)
                            .getDownloadUrl()
                            .addOnSuccessListener(uri ->
                                    AndroidUtil.setProfilePic(this, uri, profilePic)
                            );
                });
    }

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
        adapter.startListening();
    }

    private void sendMessageToUser(String message) {

        if (chatroomId == null || chatroomId.isEmpty()) return;
        if (message.trim().isEmpty()) return;

        FirebaseUtil.getChatroomReference(chatroomId).get()
                .addOnSuccessListener(snapshot -> {

                    ChatroomModel model = snapshot.toObject(ChatroomModel.class);
                    Timestamp now = Timestamp.now();

                    if (model == null) {
                        // ✅ create chatroom correctly
                        model = new ChatroomModel(
                                chatroomId,
                                Arrays.asList(
                                        FirebaseUtil.currentUserId(),
                                        otherUserId
                                ),
                                message,                       // lastMessage
                                FirebaseUtil.currentUserId(),  // senderId
                                now                            // timestamp
                        );
                    } else {
                        model.setLastMessage(message);
                        model.setLastMessageSenderId(FirebaseUtil.currentUserId());
                        model.setLastMessageTimestamp(now);
                    }

                    chatroomModel = model;
                    FirebaseUtil.getChatroomReference(chatroomId).set(model);

                    // ✅ Send message
                    ChatMessageModel chatMessage =
                            new ChatMessageModel(
                                    message,
                                    FirebaseUtil.currentUserId(),
                                    now
                            );

                    FirebaseUtil.getChatroomMessageReference(chatroomId)
                            .add(chatMessage)
                            .addOnSuccessListener(ref -> messageInput.setText(""));
                });
    }



    private void getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId).get()
                .addOnSuccessListener(snapshot -> {

                    chatroomModel = snapshot.toObject(ChatroomModel.class);

                    if (chatroomModel == null) {
                        chatroomModel = new ChatroomModel(
                                chatroomId,
                                Arrays.asList(
                                        FirebaseUtil.currentUserId(),
                                        otherUserId
                                ),
                                "",                         // lastMessage
                                "",                         // senderId
                                Timestamp.now()             // timestamp
                        );

                        FirebaseUtil.getChatroomReference(chatroomId)
                                .set(chatroomModel);
                    }
                });
    }

}