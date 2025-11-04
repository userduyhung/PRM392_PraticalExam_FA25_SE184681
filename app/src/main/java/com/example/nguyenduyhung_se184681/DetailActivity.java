package com.example.nguyenduyhung_se184681;

import android.os.Bundle;
import android.view.View;
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
 * Detail Activity - Shows complete details for a selected post
 * Features:
 * - Receives post ID as intent extra
 * - Displays all post details (title, body, REAL image, category, user ID)
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
            showError("Invalid post ID");
            return;
        }

        // Load post data
        loadPostData(postId);

        // Setup back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Post Details");
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
                showError("Post not found");
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
        userIdTextView.setText("Posted by User #" + post.getUserId());

        // Update favorite button state
        updateFavoriteButton(post.isFavorite());

        // Load REAL image using Glide
        Glide.with(this)
                .load(post.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

