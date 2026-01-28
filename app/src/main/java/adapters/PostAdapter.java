package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ConstructionApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

import models.Post;

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

        holder.btnLike.setImageResource(
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

            holder.btnLike.setImageResource(
                    post.isLikedByMe()
                            ? R.drawable.ic_filled
                            : R.drawable.ic_like
            );
        });
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProfile;
        ImageView btnLike;
        TextView txtName, txtTime, txtContent, txtLikeCount;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtName = itemView.findViewById(R.id.workertxtName);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtContent = itemView.findViewById(R.id.txtPostContent);
            btnLike = itemView.findViewById(R.id.btnLike);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
        }
    }
}
