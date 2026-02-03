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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
    public PostViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_post_card, parent, false);

        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PostViewHolder holder, int position) {

        Post post = posts.get(position);

        holder.txtName.setText(post.getUserName());
        holder.txtContent.setText(post.getContent());
        holder.txtTime.setText(post.getTimestamp());

        holder.txtLikeCount.setText(
                String.valueOf(post.getLikeCount())
        );

        holder.imgProfile.setOnClickListener(v -> {
            Intent profile = new Intent(context, UserProfile.class);
            profile.putExtra("userId", post.getUserId());
            context.startActivity(profile);
        });

        holder.txtName.setOnClickListener(v -> {
            Intent profile = new Intent(context, UserProfile.class);
            profile.putExtra("userId", post.getUserId());
            context.startActivity(profile);
        });

        holder.buttonLike.setImageResource(
                post.isLikedByMe()
                        ? R.drawable.ic_filled
                        : R.drawable.ic_like
        );

        String url = post.getProfilePicUrl();
        if (url != null && !url.isEmpty()) {
            Glide.with(context)
                    .load(url)
                    .placeholder(R.drawable.ic_profile_placeholder_foreground)
                    .circleCrop()
                    .into(holder.imgProfile);
        } else {
            holder.imgProfile.setImageResource(
                    R.drawable.ic_profile_placeholder_foreground
            );
        }

        holder.btnLike.setOnClickListener(v -> {
            toggleLike(post, holder);
        });

        holder.comment.setOnClickListener(v -> {

            BottomSheetDialog bottomSheetDialog =
                    new BottomSheetDialog(context, R.style.BottomSheetTheme);

            View sheetView = LayoutInflater.from(context)
                    .inflate(R.layout.comment_layout, null);

            bottomSheetDialog.setContentView(sheetView);
            bottomSheetDialog.show();

            bottomSheetDialog.setOnShowListener(dialog -> {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                View bottomSheet =
                        d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

                if (bottomSheet != null) {
                    com.google.android.material.bottomsheet.BottomSheetBehavior
                            .from(bottomSheet)
                            .setState(
                                    com.google.android.material.bottomsheet
                                            .BottomSheetBehavior.STATE_EXPANDED
                            );
                }
            });

            RecyclerView recyclerView =
                    sheetView.findViewById(R.id.comment_recycler);

            ArrayList<comment> commentList = new ArrayList<>();
            CommentAdapter adapter =
                    new CommentAdapter(context, commentList);

            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(adapter);

            //LOAD COMMENTS (THIS WAS MISSING EFFECTIVELY)
            FirebaseFirestore.getInstance()
                    .collection("posts")
                    .document(post.getPostId())
                    .collection("comments")
                    .orderBy("timestamp")
                    .addSnapshotListener((value, error) -> {
                        if (error != null || value == null) return;

                        commentList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            comment c = doc.toObject(comment.class);
                            if (c != null) {
                                c.setCommentId(doc.getId());
                                commentList.add(c);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    });

            //INPUT + SEND BUTTON
            EditText commentInput =
                    sheetView.findViewById(R.id.comment_input);
            ImageButton sendBtn =
                    sheetView.findViewById(R.id.btnsend);

            sendBtn.setOnClickListener(btn -> {

                String text = commentInput.getText().toString().trim();
                if (text.isEmpty()) {
                    Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                String postId = post.getPostId();
                String userId = FirebaseAuth.getInstance().getUid();
                if (postId == null || userId == null) return;

                comment newComment = new comment(
                        userId,
                        post.getUserName(),
                        post.getProfilePicUrl(),
                        text,
                        System.currentTimeMillis()
                );

                FirebaseFirestore.getInstance()
                        .collection("posts")
                        .document(postId)
                        .collection("comments")
                        .add(newComment)
                        .addOnSuccessListener(docRef -> {

                            String generatedId = docRef.getId();
                            newComment.setCommentId(generatedId);
                            docRef.update("commentId", generatedId);

                            commentInput.setText("");
                        });
            });
        });

        holder.sharebtn.setOnClickListener(v -> {
            Intent in = new Intent(Intent.ACTION_SEND);
            in.setType("text/plain");
            String body = post.getUserName();
            String sub = "http://google.com";
            in.putExtra(Intent.EXTRA_TEXT, body);
            in.putExtra(Intent.EXTRA_TEXT, sub);
            context.startActivity(Intent.createChooser(in, "Share using"));
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
    private void toggleLike(Post post, PostViewHolder holder) {

        String postId = post.getPostId();
        String userId = FirebaseAuth.getInstance().getUid();
        if (postId == null || userId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference postRef =
                db.collection("posts").document(postId);

        DocumentReference likeRef =
                postRef.collection("likes").document(userId);

        db.runTransaction(transaction -> {

            DocumentSnapshot likeSnap = transaction.get(likeRef);

            if (likeSnap.exists()) {
                transaction.delete(likeRef);
                transaction.update(postRef,
                        "likeCount", FieldValue.increment(-1));

                post.setLikedByMe(false);
                post.setLikeCount(post.getLikeCount() - 1);

            } else {
                transaction.set(likeRef, new HashMap<>());
                transaction.update(postRef,
                        "likeCount", FieldValue.increment(1));

                post.setLikedByMe(true);
                post.setLikeCount(post.getLikeCount() + 1);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {

            holder.txtLikeCount.setText(
                    String.valueOf(post.getLikeCount())
            );

            holder.buttonLike.setImageResource(
                    post.isLikedByMe()
                            ? R.drawable.ic_filled
                            : R.drawable.ic_like
            );
        });
    }
    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile, buttonLike;
        TextView txtName, txtTime, txtContent, txtLikeCount;
        LinearLayout sharebtn, btnLike, comment;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtName = itemView.findViewById(R.id.workertxtName);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtContent = itemView.findViewById(R.id.txtPostContent);
            btnLike = itemView.findViewById(R.id.btnLike);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            sharebtn = itemView.findViewById(R.id.sharebtn);
            buttonLike = itemView.findViewById(R.id.ButtonLike);
            comment = itemView.findViewById(R.id.comment);
        }
    }
}
