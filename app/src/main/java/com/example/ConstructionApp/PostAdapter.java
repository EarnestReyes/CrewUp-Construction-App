package com.example.ConstructionApp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final ArrayList<Post> posts;

    public PostAdapter(ArrayList<Post> posts) {
        // make sure the list is never null
        if (posts != null) {
            this.posts = posts;
        } else {
            this.posts = new ArrayList<>();
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_card, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.txtName.setText(post.getUserName());
        holder.txtContent.setText(post.getContent());
        holder.txtTime.setText(post.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return posts.size(); // safe now
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile;
        TextView txtName, txtTime, txtContent;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtName = itemView.findViewById(R.id.txtName);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtContent = itemView.findViewById(R.id.txtPostContent);
        }
    }
}

