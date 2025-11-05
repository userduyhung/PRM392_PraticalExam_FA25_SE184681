package com.example.nguyenduyhung_se184681;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nguyenduyhung_se184681.model.Post;
import com.example.nguyenduyhung_se184681.repository.PostRepository;
import com.example.nguyenduyhung_se184681.viewmodel.PostViewModel;

/**
 * Detail Activity - Shows complete details for a selected book
 * Features:
 * - Receives book ID as intent extra
 * - Displays all book details (title, body, REAL image, category, user ID)
 * - Toggle favorite button with immediate state update
 * - Back button to return to main screen
 */
public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "extra_post_id";

    private PostViewModel viewModel;
    private Post currentPost;

    // UI Components
    private ImageView imageView;
    private TextView titleTextView;
    private TextView bodyTextView;
    private TextView categoryTextView;
    private TextView userIdTextView;
    private ImageButton favoriteButton;
    private ProgressBar progressBar;
    private View contentLayout;
    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(PostViewModel.class);

        // Initialize views
        initViews();

        // Get post ID from intent
        int postId = getIntent().getIntExtra(EXTRA_POST_ID, -1);

        if (postId == -1) {
            showError("Invalid book ID");
            return;
        }

        // Load post data
        loadPostData(postId);

        // Setup back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Book Details");
        }
    }

    private void initViews() {
        imageView = findViewById(R.id.detail_image);
        titleTextView = findViewById(R.id.detail_title);
        bodyTextView = findViewById(R.id.detail_body);
        categoryTextView = findViewById(R.id.detail_category);
        userIdTextView = findViewById(R.id.detail_user_id);
        favoriteButton = findViewById(R.id.favorite_button);
        progressBar = findViewById(R.id.detail_progress_bar);
        contentLayout = findViewById(R.id.detail_content_layout);
        errorTextView = findViewById(R.id.detail_error_text);

        // Setup favorite button click listener
        favoriteButton.setOnClickListener(v -> toggleFavorite());

        // Setup image click listener for full-screen view
        imageView.setOnClickListener(v -> {
            if (currentPost != null && currentPost.getImageUrl() != null) {
                showFullScreenImage(currentPost.getImageUrl());
            }
        });
    }

    private void loadPostData(int postId) {
        showLoading();

        // Observe post data from database
        viewModel.getPostById(postId).observe(this, post -> {
            if (post != null) {
                currentPost = post;
                displayPostData(post);
                hideLoading();
            } else {
                showError("Book not found");
            }
        });
    }

    private void displayPostData(Post post) {
        // Display title
        titleTextView.setText(post.getTitle());

        // Display body
        bodyTextView.setText(post.getBody());

        // Display category
        categoryTextView.setText("Category: " + post.getCategory());

        // Display user ID
        userIdTextView.setText("Author ID: #" + post.getUserId());

        // Update favorite button state
        updateFavoriteButton(post.isFavorite());

        // Load REAL image using Glide with improved configuration
        Glide.with(this)
                .load(post.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .fitCenter() // Better than centerCrop for detail view
                .into(imageView);

        // Show content
        contentLayout.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
    }

    private void toggleFavorite() {
        if (currentPost == null) return;

        // Disable button during operation
        favoriteButton.setEnabled(false);

        // Toggle favorite status
        viewModel.toggleFavorite(currentPost, new PostRepository.FavoriteCallback() {
            @Override
            public void onSuccess(boolean isFavorite) {
                runOnUiThread(() -> {
                    // Update UI immediately
                    updateFavoriteButton(isFavorite);
                    favoriteButton.setEnabled(true);

                    // Show toast
                    String message = isFavorite ?
                            "Added to favorites" : "Removed from favorites";
                    Toast.makeText(DetailActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    favoriteButton.setEnabled(true);
                    Toast.makeText(DetailActivity.this,
                            "Error: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateFavoriteButton(boolean isFavorite) {
        if (isFavorite) {
            // Filled star icon for favorited items
            favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
            favoriteButton.setContentDescription("Remove from favorites");
        } else {
            // Outlined star icon for non-favorited items
            favoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
            favoriteButton.setContentDescription("Add to favorites");
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        errorTextView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        contentLayout.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(message);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle back button press
        finish();
        return true;
    }

    /**
     * Show full-screen image viewer dialog
     * User can close by clicking X button or clicking anywhere on screen
     */
    private void showFullScreenImage(String imageUrl) {
        // Create full-screen dialog
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image_viewer);
        dialog.setCancelable(true); // Allow dismissing with back button
        dialog.setCanceledOnTouchOutside(true); // Allow dismissing by touching outside

        // Make dialog background transparent and full screen
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                           WindowManager.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                          WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        // Get views from dialog
        ImageView fullscreenImage = dialog.findViewById(R.id.fullscreen_image);
        ImageButton closeButton = dialog.findViewById(R.id.close_button);
        ProgressBar loadingProgress = dialog.findViewById(R.id.image_loading_progress);
        View dialogRoot = dialog.findViewById(R.id.dialog_root);

        // Load high-quality image with Glide
        loadingProgress.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter() // Full image, high quality
                .error(R.drawable.ic_placeholder)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e,
                                               Object model,
                                               com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                               boolean isFirstResource) {
                        loadingProgress.setVisibility(View.GONE);
                        Toast.makeText(DetailActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                                  Object model,
                                                  com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                  com.bumptech.glide.load.DataSource dataSource,
                                                  boolean isFirstResource) {
                        loadingProgress.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(fullscreenImage);

        // Simple approach: Click anywhere to dismiss
        View.OnClickListener dismissListener = v -> dialog.dismiss();

        closeButton.setOnClickListener(dismissListener);
        dialogRoot.setOnClickListener(dismissListener);
        fullscreenImage.setOnClickListener(dismissListener);


        // Show dialog
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

