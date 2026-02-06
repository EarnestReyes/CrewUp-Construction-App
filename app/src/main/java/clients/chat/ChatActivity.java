package clients.chat;

import static android.view.View.GONE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import models.ChatMessageModel;
import models.ChatroomModel;
import data.FirebaseUtil;

import adapters.ChatRecyclerAdapter;
import com.example.ConstructionApp.R;
import models.UserModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String otherUserId, projectId;
    private UserModel otherUser;
    private String chatroomId;
    private ChatRecyclerAdapter adapter;
    private EditText messageInput;
    private ImageButton sendMessageBtn, backBtn, hire_btn;
    private TextView otherUsername;
    private RecyclerView recyclerView;
    private ImageView profilePic;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Edge-to-edge (required for IME)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_chat);

        otherUserId = getIntent().getStringExtra("userId");

        if (otherUserId == null || otherUserId.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Views
        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        profilePic = findViewById(R.id.profile_pic_image_view);
        hire_btn = findViewById(R.id.hire_btn);

        View root = findViewById(R.id.main);
        View bottomLayout = findViewById(R.id.bottom_layout);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {

            if (isFinishing() || isDestroyed()) {
                return insets;
            }

            if (bottomLayout == null || recyclerView == null) {
                return insets;
            }

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            boolean isKeyboardVisible =
                    insets.isVisible(WindowInsetsCompat.Type.ime());

            int keyboardHeight = isKeyboardVisible ? imeInsets.bottom : 0;

            // Status bar padding
            v.setPadding(
                    v.getPaddingLeft(),
                    systemBars.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );

            // Move input above keyboard
            bottomLayout.setTranslationY(-keyboardHeight);

            // Nav bar padding ONLY when keyboard hidden
            bottomLayout.setPadding(
                    bottomLayout.getPaddingLeft(),
                    bottomLayout.getPaddingTop(),
                    bottomLayout.getPaddingRight(),
                    isKeyboardVisible ? 0 : systemBars.bottom
            );

            // RecyclerView space = keyboard + nav bar
            recyclerView.setPadding(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getPaddingRight(),
                    keyboardHeight + systemBars.bottom
            );

            return insets;
        });

        backBtn.setOnClickListener((v) -> finish());

        hire_btn.setOnClickListener(view -> {

            checkIfAlreadyHired();

            permission("Clarification", "Are you sure you want to hire " + otherUser.getUsername() + "?", "Action cancelled");
        });

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

                    String otherUserRole = otherUser.getRole();

                    if ("worker".equalsIgnoreCase(otherUserRole)) {
                        checkIfAlreadyHired();
                    } else {
                        hire_btn.setVisibility(GONE);
                    }

                    String url = snapshot.getString("profilePicUrl");
                    if (url != null && !url.isEmpty()) {
                        Glide.with(this)
                                .load(url)
                                .circleCrop()
                                .into(profilePic);
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
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
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
       sendNotification(message);
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

    @Override
    protected void onPostResume() {
        super.onPostResume();
            if(adapter!=null){
                adapter.notifyDataSetChanged();
        }
    }

    void sendNotification(String message) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> notif = new HashMap<>();
        notif.put("fromUserId", user.getUid());
        notif.put("toUserId", otherUser.getUserId());
        notif.put("message", message);
        notif.put("timestamp", System.currentTimeMillis());

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .add(notif);
    }

    private void permission(String title, String message, String toastText) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    hire_btn.setVisibility(GONE);
                    Toast.makeText(this, "Hiring worker, please wait...", Toast.LENGTH_SHORT).show();
                    Intent in = new Intent(this, WelcomeBookingPage.class);
                    in.putExtra("otherId", otherUserId);
                    startActivity(in);
                    try{
                        Thread.sleep(2000);
                    } catch (Exception e){

                    }
                    Toast.makeText(this, "User " +  otherUser.getUsername() + " is hired!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
                })
                .show();
    }
    private void checkIfAlreadyHired() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            hire_btn.setVisibility(GONE);
            return;
        }

        db.collection("BookingOrder")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("workerId", otherUserId)
                .whereIn("status", Arrays.asList("pending", "active"))
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (!querySnapshot.isEmpty()) {
                        hire_btn.setVisibility(View.GONE); 
                    } else {
                        hire_btn.setVisibility(View.VISIBLE);
                    }

                })
                .addOnFailureListener(e ->
                        hire_btn.setVisibility(GONE)
                );
    }
}
