package com.example.nguyenduyhung_se184681.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nguyenduyhung_se184681.R;
import com.example.nguyenduyhung_se184681.model.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying posts with real images from API
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private static final String TAG = "PostAdapter";
    private List<Post> posts = new ArrayList<>();
    private final OnPostClickListener listener;

    public PostAdapter(OnPostClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post, listener);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView titleTextView;
        private final TextView bodyTextView;
        private final TextView categoryTextView;
        private final ImageView favoriteIcon;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            titleTextView = itemView.findViewById(R.id.item_title);
            bodyTextView = itemView.findViewById(R.id.item_body);
            categoryTextView = itemView.findViewById(R.id.item_category);
            favoriteIcon = itemView.findViewById(R.id.item_favorite_icon);
        }

        public void bind(Post post, OnPostClickListener listener) {
            // Set title (capitalize first letter)
            String title = post.getTitle();
            if (!title.isEmpty()) {
                title = title.substring(0, 1).toUpperCase() + title.substring(1);
            }
            titleTextView.setText(title);

            // Set body (truncate if too long)
            String body = post.getBody();
            if (body.length() > 100) {
                body = body.substring(0, 100) + "...";
            }
            bodyTextView.setText(body);

            // Set category
            categoryTextView.setText(post.getCategory());

            // Set favorite icon visibility
            favoriteIcon.setVisibility(post.isFavorite() ? View.VISIBLE : View.GONE);

            // Load REAL image from URL using Glide
            String imageUrl = post.getImageUrl();
            Log.d(TAG, "Loading image for post " + post.getId() + ": " + imageUrl);

            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(imageView);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post);
                }
            });
        }
    }

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }
}
