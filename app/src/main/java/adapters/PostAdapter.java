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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ConstructionApp.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

import clients.profile.UserProfile;
import models.Post;
import models.comment;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final Context context;
    private final ArrayList<Post> posts;

    public PostAdapter(Context context, ArrayList<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_post_card, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        Post post = posts.get(position);

        holder.txtName.setText(post.getUserName());
        holder.txtContent.setText(post.getContent());
        holder.txtTime.setText(post.getTimestamp());
        holder.txtLikeCount.setText(String.valueOf(post.getLikeCount()));

        // Post image
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.postImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(post.getImageUrl()).into(holder.postImage);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }

        // Profile image
        if (post.getProfilePicUrl() != null && !post.getProfilePicUrl().isEmpty()) {
            Glide.with(context)
                    .load(post.getProfilePicUrl())
                    .circleCrop()
                    .into(holder.imgProfile);
        } else {
            holder.imgProfile.setImageResource(
                    R.drawable.ic_profile_placeholder_foreground);
        }

        // Profile click
        View.OnClickListener openProfile = v -> {
            Intent i = new Intent(context, UserProfile.class);
            i.putExtra("userId", post.getUserId());
            context.startActivity(i);
        };
        holder.imgProfile.setOnClickListener(openProfile);
        holder.txtName.setOnClickListener(openProfile);

        // Like icon
        holder.buttonLike.setImageResource(
                post.isLikedByMe() ? R.drawable.ic_filled : R.drawable.ic_like
        );

        holder.btnLike.setOnClickListener(v -> toggleLike(post, holder));

        // COMMENTS
        holder.comment.setOnClickListener(v -> openComments(post));
    }

    private void openComments(Post post) {

        BottomSheetDialog dialog =
                new BottomSheetDialog(context, R.style.BottomSheetTheme);

        View sheet = LayoutInflater.from(context)
                .inflate(R.layout.comment_layout, null);

        dialog.setContentView(sheet);
        dialog.show();

        View bottomSheet = dialog.findViewById(
                com.google.android.material.R.id.design_bottom_sheet);

        if (bottomSheet != null) {
            BottomSheetBehavior.from(bottomSheet)
                    .setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        RecyclerView recyclerView = sheet.findViewById(R.id.comment_recycler);
        EditText input = sheet.findViewById(R.id.comment_input);
        ImageButton send = sheet.findViewById(R.id.btnsend);

        ArrayList<comment> comments = new ArrayList<>();
        CommentAdapter adapter = new CommentAdapter(context, comments);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Load comments
        db.collection("posts")
                .document(post.getPostId())
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null) return;

                    comments.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        comment c = d.toObject(comment.class);
                        if (c != null) {
                            c.setCommentId(d.getId());
                            comments.add(c);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });

        // SEND COMMENT (FIXED)
        send.setOnClickListener(v -> {

            String text = input.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) return;

            db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(userDoc -> {

                        String name = userDoc.getString("username");
                        String photo = userDoc.getString("profilePicUrl");

                        comment newComment = new comment(
                                uid,
                                name,
                                photo,
                                text,
                                System.currentTimeMillis()
                        );

                        db.collection("posts")
                                .document(post.getPostId())
                                .collection("comments")
                                .add(newComment)
                                .addOnSuccessListener(ref -> {
                                    input.setText("");
                                });
                    });
        });
    }

    private void toggleLike(Post post, PostViewHolder holder) {

        String postId = post.getPostId();
        String uid = FirebaseAuth.getInstance().getUid();
        if (postId == null || uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference postRef = db.collection("posts").document(postId);
        DocumentReference likeRef = postRef.collection("likes").document(uid);

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
        }).addOnSuccessListener(v ->
                holder.txtLikeCount.setText(String.valueOf(post.getLikeCount()))
        );
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProfile, buttonLike, postImage;
        TextView txtName, txtTime, txtContent, txtLikeCount;
        LinearLayout sharebtn, btnLike, comment;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtName = itemView.findViewById(R.id.workertxtName);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtContent = itemView.findViewById(R.id.txtPostContent);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            btnLike = itemView.findViewById(R.id.btnLike);
            buttonLike = itemView.findViewById(R.id.ButtonLike);
            sharebtn = itemView.findViewById(R.id.sharebtn);
            comment = itemView.findViewById(R.id.comment);
            postImage = itemView.findViewById(R.id.postImage);
        }
    }
}
