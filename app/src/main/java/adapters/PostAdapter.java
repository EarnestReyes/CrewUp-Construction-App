package adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ConstructionApp.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import clients.profile.UserProfile;
import models.Post;
import models.comment;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final Context context;
    private final ArrayList<Post> posts;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String currentUserName;
    private String currentUserProfilePic;

    public PostAdapter(Context context, ArrayList<Post> posts) {
        this.context = context;
        this.posts = posts;
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    currentUserName = doc.getString("username");
                    currentUserProfilePic = doc.getString("profilePicUrl");
                });
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_post_card, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder h, int position) {

        Post post = posts.get(position);

        // =========================
        // SHARED POST
        // =========================
        if (post.isShared()) {

            h.sharerContainer.setVisibility(View.VISIBLE);
            h.normalHeader.setVisibility(View.VISIBLE);

            // ===== SHARER INFO =====
            h.txtSharerName.setText(post.getUserName());

            if (post.getProfilePicUrl() != null &&
                    !post.getProfilePicUrl().isEmpty()) {
                Glide.with(context)
                        .load(post.getProfilePicUrl())
                        .circleCrop()
                        .into(h.imgSharerProfile);
            } else {
                h.imgSharerProfile.setImageResource(
                        R.drawable.ic_profile_placeholder_foreground);
            }

            // ===== ORIGINAL USER INFO =====
            h.workertxtName.setText(post.getOriginalUserName());

            if (post.getOriginalProfilePicUrl() != null &&
                    !post.getOriginalProfilePicUrl().isEmpty()) {
                Glide.with(context)
                        .load(post.getOriginalProfilePicUrl())
                        .circleCrop()
                        .into(h.imgProfile);
            } else {
                h.imgProfile.setImageResource(
                        R.drawable.ic_profile_placeholder_foreground);
            }

            // ===== ORIGINAL CONTENT =====
            h.txtPostContent.setText(
                    post.getOriginalContent() != null ?
                            post.getOriginalContent() : ""
            );

            // ===== ORIGINAL IMAGE =====
            if (post.getOriginalImageUrl() != null &&
                    !post.getOriginalImageUrl().isEmpty()) {

                h.postImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(post.getOriginalImageUrl())
                        .into(h.postImage);

            } else {
                h.postImage.setVisibility(View.GONE);
            }

        } else {
            // =========================
            // NORMAL POST
            // =========================

            h.sharerContainer.setVisibility(View.GONE);
            h.normalHeader.setVisibility(View.VISIBLE);

            h.workertxtName.setText(post.getUserName());
            h.txtPostContent.setText(post.getContent());

            if (post.getProfilePicUrl() != null &&
                    !post.getProfilePicUrl().isEmpty()) {
                Glide.with(context)
                        .load(post.getProfilePicUrl())
                        .circleCrop()
                        .into(h.imgProfile);
            } else {
                h.imgProfile.setImageResource(
                        R.drawable.ic_profile_placeholder_foreground);
            }

            if (post.getImageUrl() != null &&
                    !post.getImageUrl().isEmpty()) {

                h.postImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(post.getImageUrl())
                        .into(h.postImage);

            } else {
                h.postImage.setVisibility(View.GONE);
            }
        }

        // =========================
        // COMMON DATA
        // =========================
        h.txtTime.setText(
                android.text.format.DateFormat.format(
                        "MMM dd • hh:mm a",
                        post.getTimestamp()
                )
        );

        h.txtLikeCount.setText(String.valueOf(post.getLikeCount()));

        h.buttonLike.setImageResource(
                post.isLikedByMe()
                        ? R.drawable.ic_filled
                        : R.drawable.ic_like
        );

        h.btnLike.setOnClickListener(v -> toggleLike(post, h));

        // =========================
        // PROFILE CLICK HANDLING
        // =========================

        // Sharer profile
        h.imgSharerProfile.setOnClickListener(v -> {
            if (post.isShared()) {
                openUserProfile(post.getUserId());
            }
        });

        h.txtSharerName.setOnClickListener(v -> {
            if (post.isShared()) {
                openUserProfile(post.getUserId());
            }
        });

        // Original / Normal profile
        h.imgProfile.setOnClickListener(v -> {
            if (post.isShared()) {
                openUserProfile(post.getOriginalUserId());
            } else {
                openUserProfile(post.getUserId());
            }
        });

        h.workertxtName.setOnClickListener(v -> {
            if (post.isShared()) {
                openUserProfile(post.getOriginalUserId());
            } else {
                openUserProfile(post.getUserId());
            }
        });

        h.comment.setOnClickListener(v -> openComments(post));
        h.sharebtn.setOnClickListener(v -> sharePost(post));
    }

    private void openUserProfile(String userId) {
        if (userId == null) return;

        Intent i = new Intent(context, UserProfile.class);
        i.putExtra("userId", userId);
        context.startActivity(i);
    }

    // =========================
    // LIKE FUNCTION
    // =========================
    private void toggleLike(Post post, PostViewHolder h) {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DocumentReference postRef =
                db.collection("posts").document(post.getPostId());

        DocumentReference likeRef =
                postRef.collection("likes").document(uid);

        db.runTransaction(t -> {
            if (t.get(likeRef).exists()) {
                t.delete(likeRef);
                t.update(postRef, "likeCount", FieldValue.increment(-1));
                post.setLikedByMe(false);
                post.setLikeCount(post.getLikeCount() - 1);
            } else {
                t.set(likeRef, new HashMap<>());
                t.update(postRef, "likeCount", FieldValue.increment(1));
                post.setLikedByMe(true);
                post.setLikeCount(post.getLikeCount() + 1);
            }
            return null;
        }).addOnSuccessListener(v -> {
            h.txtLikeCount.setText(String.valueOf(post.getLikeCount()));
            h.buttonLike.setImageResource(
                    post.isLikedByMe()
                            ? R.drawable.ic_filled
                            : R.drawable.ic_like
            );
        });
    }

    // =========================
    // SHARE FUNCTION
    // =========================
    private void sharePost(Post originalPost) {

        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        db.collection("posts")
                .document(originalPost.getPostId())
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) return;

                    String originalUserId = snapshot.getString("userId");
                    String originalUserName = snapshot.getString("userName");

                    String originalContent = snapshot.getString("content");
                    if (originalContent == null) {
                        originalContent = snapshot.getString("originalContent");
                    }

                    String originalImageUrl = snapshot.getString("imageUrl");
                    if (originalImageUrl == null) {
                        originalImageUrl = snapshot.getString("originalImageUrl");
                    }

                    String originalProfilePic = snapshot.getString("profilePicUrl");

                    Map<String, Object> sharedPost = new HashMap<>();

                    sharedPost.put("userId", currentUserId);
                    sharedPost.put("userName", currentUserName);
                    sharedPost.put("profilePicUrl", currentUserProfilePic);

                    sharedPost.put("isShared", true);
                    sharedPost.put("originalPostId", originalPost.getPostId());

                    sharedPost.put("originalUserId", originalUserId);
                    sharedPost.put("originalUserName", originalUserName);
                    sharedPost.put("originalProfilePicUrl", originalProfilePic);
                    sharedPost.put("originalContent", originalContent);
                    sharedPost.put("originalImageUrl", originalImageUrl);

                    sharedPost.put("likeCount", 0);
                    sharedPost.put("timestamp", System.currentTimeMillis());

                    db.collection("posts").add(sharedPost);
                });
    }

    // =========================
    // COMMENTS
    // =========================
    private void openComments(Post post) {

        BottomSheetDialog dialog =
                new BottomSheetDialog(context, R.style.BottomSheetTheme);

        View sheet = LayoutInflater.from(context)
                .inflate(R.layout.comment_layout, null);

        dialog.setContentView(sheet);
        dialog.show();

        RecyclerView rv = sheet.findViewById(R.id.comment_recycler);
        EditText input = sheet.findViewById(R.id.comment_input);
        ImageButton send = sheet.findViewById(R.id.btnsend);

        ArrayList<comment> list = new ArrayList<>();
        CommentAdapter adapter = new CommentAdapter(context, list);

        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(adapter);

        String commentPostId =
                post.isShared() ? post.getOriginalPostId() : post.getPostId();

        db.collection("posts")
                .document(commentPostId)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener((snap, e) -> {

                    if (snap == null) return;

                    list.clear();

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        comment c = d.toObject(comment.class);
                        if (c != null) {
                            c.setCommentId(d.getId());
                            list.add(c);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });

        send.setOnClickListener(v -> {

            String txt = input.getText().toString().trim();
            if (txt.isEmpty()) return;

            comment c = new comment(
                    FirebaseAuth.getInstance().getUid(),
                    currentUserName,
                    currentUserProfilePic,
                    txt,
                    System.currentTimeMillis()
            );

            db.collection("posts")
                    .document(commentPostId)
                    .collection("comments")
                    .add(c)
                    .addOnSuccessListener(r -> input.setText(""));
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        LinearLayout sharerContainer;
        ImageView imgSharerProfile;
        TextView txtSharerName;

        LinearLayout normalHeader;
        ImageView imgProfile;
        TextView workertxtName;

        TextView txtTime;
        TextView txtPostContent;
        TextView txtLikeCount;

        ImageView buttonLike;
        ImageView postImage;

        LinearLayout btnLike;
        LinearLayout comment;
        LinearLayout sharebtn;

        PostViewHolder(View v) {
            super(v);

            sharerContainer = v.findViewById(R.id.sharerContainer);
            imgSharerProfile = v.findViewById(R.id.imgSharerProfile);
            txtSharerName = v.findViewById(R.id.txtSharerName);

            normalHeader = v.findViewById(R.id.normalHeader);
            imgProfile = v.findViewById(R.id.imgProfile);
            workertxtName = v.findViewById(R.id.workertxtName);

            txtTime = v.findViewById(R.id.txtTime);
            txtPostContent = v.findViewById(R.id.txtPostContent);
            txtLikeCount = v.findViewById(R.id.txtLikeCount);

            buttonLike = v.findViewById(R.id.ButtonLike);
            postImage = v.findViewById(R.id.postImage);

            btnLike = v.findViewById(R.id.btnLike);
            comment = v.findViewById(R.id.comment);
            sharebtn = v.findViewById(R.id.sharebtn);
        }
    }
}
