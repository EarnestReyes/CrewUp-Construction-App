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
import com.google.firebase.Timestamp;
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

        db.collection("users")
                .document(uid)
                .get()
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
    public void onBindViewHolder(@NonNull PostViewHolder h, int pos) {
        Post post = posts.get(pos);

        h.txtName.setText(post.getUserName());
        h.txtContent.setText(post.getContent());
        h.txtTime.setText(
                android.text.format.DateFormat.format(
                        "MMM dd • hh:mm a",
                        post.getTimestamp()
                )
        );
        h.txtLikeCount.setText(String.valueOf(post.getLikeCount()));

        // Profile image
        if (post.getProfilePicUrl() != null && !post.getProfilePicUrl().isEmpty()) {
            Glide.with(context)
                    .load(post.getProfilePicUrl())
                    .circleCrop()
                    .into(h.imgProfile);
        } else {
            h.imgProfile.setImageResource(
                    R.drawable.ic_profile_placeholder_foreground
            );
        }

        // Post image
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            h.postImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(post.getImageUrl()).into(h.postImage);
        } else {
            h.postImage.setVisibility(View.GONE);
        }

        h.buttonLike.setImageResource(
                post.isLikedByMe()
                        ? R.drawable.ic_filled
                        : R.drawable.ic_like
        );

        h.btnLike.setOnClickListener(v -> toggleLike(post, h));

        View.OnClickListener openProfile = v -> {
            Intent i = new Intent(context, UserProfile.class);
            i.putExtra("userId", post.getUserId());
            context.startActivity(i);
        };

        h.imgProfile.setOnClickListener(openProfile);
        h.txtName.setOnClickListener(openProfile);

        h.comment.setOnClickListener(v -> openComments(post));
    }

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
            pushNotification(post);
        });
    }

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

        db.collection("posts")
                .document(post.getPostId())
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;
                    list.clear();
                    for (DocumentSnapshot d : snap) {
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
                    .document(post.getPostId())
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

        ImageView imgProfile, buttonLike, postImage;
        TextView txtName, txtTime, txtContent, txtLikeCount;
        LinearLayout btnLike, comment, sharebtn;

        PostViewHolder(View v) {
            super(v);
            imgProfile = v.findViewById(R.id.imgProfile);
            txtName = v.findViewById(R.id.workertxtName);
            txtTime = v.findViewById(R.id.txtTime);
            txtContent = v.findViewById(R.id.txtPostContent);
            txtLikeCount = v.findViewById(R.id.txtLikeCount);
            btnLike = v.findViewById(R.id.btnLike);
            buttonLike = v.findViewById(R.id.ButtonLike);
            comment = v.findViewById(R.id.comment);
            sharebtn = v.findViewById(R.id.sharebtn);
            postImage = v.findViewById(R.id.postImage);
        }
    }
    public static void pushNotification(Post post) {
        String likerId = FirebaseAuth.getInstance().getUid();
        if (likerId == null) return;

        if (likerId.equals(post.getUserId())) return;

        Map<String, Object> data = new HashMap<>();
        data.put("toUserId", post.getUserId());   // ✅ post owner
        data.put("fromUserId", likerId);          // ✅ who liked
        data.put("postId", post.getPostId());     // ✅ which post
        data.put("title", "CrewUp");
        data.put("message", "Someone liked your post");
        data.put("type", "LIKE");
        data.put("timestamp", Timestamp.now());
        data.put("read", false);

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .add(data);
    }
}
