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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import data.FirebaseUtil;
import models.comment;

public class CommentAdapter
        extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final Context context;
    private final ArrayList<comment> comments;

    public CommentAdapter(Context context, ArrayList<comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CommentViewHolder holder, int position) {

        comment c = comments.get(position);

        holder.txtName.setText(c.getUserName());
        holder.txtComment.setText(c.getText());

        if (c.getProfilePicUrl() != null &&
                !c.getProfilePicUrl().isEmpty()) {

            Glide.with(context)
                    .load(c.getProfilePicUrl())
                    .circleCrop()
                    .into(holder.imgProfile);
        } else {
            holder.imgProfile.setImageResource(
                    R.drawable.ic_profile_placeholder_foreground
            );
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProfile;
        TextView txtName, txtComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtName = itemView.findViewById(R.id.txtName);
            txtComment = itemView.findViewById(R.id.txtComment);
        }
    }
}
