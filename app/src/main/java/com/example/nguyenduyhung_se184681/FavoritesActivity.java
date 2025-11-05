package com.example.nguyenduyhung_se184681;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nguyenduyhung_se184681.adapter.PostAdapter;
import com.example.nguyenduyhung_se184681.model.Post;
import com.example.nguyenduyhung_se184681.viewmodel.PostViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Favorites Activity - Favorites Screen (Screen 3)
 * Features:
 * - Displays only favorited posts (from database only, no network calls)
 * - Real-time updates when favorites are added/removed from Detail Screen
 * - Search bar for filtering favorites
 * - Empty state message when no favorites exist
 * - Navigation to detail screen on item click
 */
public class FavoritesActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {

    private PostViewModel viewModel;
    private PostAdapter adapter;

    // UI Components
    private RecyclerView recyclerView;
    private TextView emptyStateTextView;
    private EditText searchEditText;
    private View searchCardView; // Card containing search bar

    // State
    private List<Post> allFavorites = new ArrayList<>();
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Setup action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("⭐ Favorites");
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(PostViewModel.class);

        // Initialize views
        initViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup search functionality
        setupSearch();

        // Load favorite posts (with real-time updates)
        loadFavoritePosts();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.favorites_recycler_view);
        emptyStateTextView = findViewById(R.id.favorites_empty_state);
        searchEditText = findViewById(R.id.favorites_search_edit_text);
        searchCardView = findViewById(R.id.favorites_search_card);
    }

    private void setupRecyclerView() {
        adapter = new PostAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }

    private void setupSearch() {
        // Real-time search as user types
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Add Enter key action to perform search and hide keyboard
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {

                // Hide keyboard
                android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                }

                // Perform search
                currentSearchQuery = searchEditText.getText().toString().trim();
                applyFilters();

                return true;
            }
            return false;
        });
    }

    /**
     * Load favorite posts from database with real-time updates
     * Uses LiveData to automatically update when favorites change
     */
    private void loadFavoritePosts() {
        // Always hide category filters on Favorites screen

        // Observe favorite posts - this will automatically update in real-time
        viewModel.getFavoritePosts().observe(this, favorites -> {
            if (favorites != null) {
                // Defensive: ensure only truly favorited items are used (guard against DB issues)
                List<Post> realFavorites = favorites.stream()
                        .filter(Post::isFavorite)
                        .collect(java.util.stream.Collectors.toList());

                allFavorites = new ArrayList<>(realFavorites);

                // Update search bar visibility based on favorites count
                updateSearchBarVisibility(realFavorites.size());

                if (realFavorites.isEmpty()) {
                    // Clear adapter to avoid showing stale data
                    adapter.setPosts(new ArrayList<>());
                    showEmptyState();
                } else {
                    hideEmptyState();
                    applyFilters();
                }
            } else {
                updateSearchBarVisibility(0);
                showEmptyState();
            }
        });
    }

    /**
     * Show or hide search bar based on number of favorites
     * Only show search when there are 3 or more favorites
     */
    private void updateSearchBarVisibility(int favoritesCount) {
        if (searchCardView != null) {
            if (favoritesCount >= 3) {
                searchCardView.setVisibility(View.VISIBLE);
            } else {
                searchCardView.setVisibility(View.GONE);
                // Clear search query when hiding search bar
                if (!currentSearchQuery.isEmpty()) {
                    currentSearchQuery = "";
                    searchEditText.setText("");
                }
            }
        }
    }

    /**
     * Apply search filter to the favorite posts
     * Filters are applied client-side on the in-memory list
     */
    private void applyFilters() {
        if (allFavorites.isEmpty()) {
            showEmptyState();
            return;
        }

        List<Post> filteredPosts = new ArrayList<>(allFavorites);

        // Apply search filter (search by book title only - null-safe)
        if (!currentSearchQuery.isEmpty()) {
            String query = currentSearchQuery.toLowerCase();
            filteredPosts = filteredPosts.stream()
                    .filter(post -> {
                        String title = post.getTitle() != null ? post.getTitle().toLowerCase() : "";
                        return title.contains(query);
                    })
                    .collect(Collectors.toList());
        }

        // Update adapter
        if (filteredPosts.isEmpty()) {
            if (!currentSearchQuery.isEmpty()) {
                showEmptyState("No favorites found for \"" + currentSearchQuery + "\"");
            } else {
                showEmptyState();
            }
        } else {
            hideEmptyState();
            adapter.setPosts(filteredPosts);
        }
    }

    @Override
    public void onPostClick(Post post) {
        // Navigate to detail screen
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_POST_ID, post.getId());
        startActivity(intent);
    }

    private void showEmptyState() {
        showEmptyState("You have no favorites yet.\n\n" +
                "Tap the star on any book to add it to your favorites!");
    }

    private void showEmptyState(String message) {
        recyclerView.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.VISIBLE);
        emptyStateTextView.setText(message);
    }

    private void hideEmptyState() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateTextView.setVisibility(View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle back button press
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When returning from detail screen, favorites will automatically update
        // due to LiveData observation - no manual refresh needed!
    }
}
