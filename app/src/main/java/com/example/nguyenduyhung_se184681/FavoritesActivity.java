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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Favorites Activity - Favorites Screen (Screen 3)
 * Features:
 * - Displays only favorited posts (from database only, no network calls)
 * - Real-time updates when favorites are added/removed from Detail Screen
 * - Search bar for filtering favorites
 * - Category filters (dynamically generated from favorite posts)
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
    private ChipGroup categoryChipGroup;

    // State
    private List<Post> allFavorites = new ArrayList<>();
    private String currentSearchQuery = "";
    private Set<String> selectedCategories = new HashSet<>();

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
        categoryChipGroup = findViewById(R.id.favorites_category_chip_group);
    }

    private void setupRecyclerView() {
        adapter = new PostAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }

    private void setupSearch() {
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
    }

    /**
     * Load favorite posts from database with real-time updates
     * Uses LiveData to automatically update when favorites change
     */
    private void loadFavoritePosts() {
        // Observe favorite posts - this will automatically update in real-time
        viewModel.getFavoritePosts().observe(this, favorites -> {
            if (favorites != null) {
                allFavorites = new ArrayList<>(favorites);

                if (favorites.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                    updateCategoryFilters(favorites);
                    applyFilters();
                }
            } else {
                showEmptyState();
            }
        });
    }

    /**
     * Update category chips based on available favorite posts
     * Categories are automatically derived from the favorited posts
     */
    private void updateCategoryFilters(List<Post> favorites) {
        // Extract unique categories from favorites
        Set<String> categories = new HashSet<>();
        for (Post post : favorites) {
            if (post.getCategory() != null && !post.getCategory().isEmpty()) {
                categories.add(post.getCategory());
            }
        }

        // Clear existing chips
        categoryChipGroup.removeAllViews();

        // Add "All" chip
        Chip allChip = createCategoryChip("All", true);
        categoryChipGroup.addView(allChip);

        // Add category chips (sorted)
        List<String> sortedCategories = new ArrayList<>(categories);
        sortedCategories.sort(String::compareTo);

        for (String category : sortedCategories) {
            Chip chip = createCategoryChip(category, false);
            categoryChipGroup.addView(chip);
        }

        // If only one category exists besides "All", hide the chip group
        if (categories.size() <= 1) {
            categoryChipGroup.setVisibility(View.GONE);
        } else {
            categoryChipGroup.setVisibility(View.VISIBLE);
        }
    }

    private Chip createCategoryChip(String category, boolean isChecked) {
        Chip chip = new Chip(this);
        chip.setText(category);
        chip.setCheckable(true);
        chip.setChecked(isChecked);
        chip.setChipBackgroundColorResource(R.color.chip_background);
        chip.setTextColor(getResources().getColor(R.color.chip_text, null));

        chip.setOnCheckedChangeListener((buttonView, isCheckedNow) -> {
            // Prevent recursive calls
            if (!buttonView.isPressed()) return;

            if (category.equals("All")) {
                // "All" chip behavior: clear all selections and reset to all
                if (isCheckedNow) {
                    // Uncheck all other chips
                    for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                        Chip otherChip = (Chip) categoryChipGroup.getChildAt(i);
                        if (otherChip != chip && otherChip.isChecked()) {
                            otherChip.setChecked(false);
                        }
                    }
                    selectedCategories.clear();
                } else {
                    // "All" cannot be unchecked manually - keep it checked if nothing else is selected
                    if (selectedCategories.isEmpty()) {
                        chip.setChecked(true);
                    }
                }
            } else {
                // Regular category chip behavior
                if (isCheckedNow) {
                    // Uncheck "All" when selecting a specific category
                    Chip allChip = (Chip) categoryChipGroup.getChildAt(0);
                    if (allChip.isChecked()) {
                        allChip.setChecked(false);
                    }
                    // Add to selected categories
                    selectedCategories.add(category);
                } else {
                    // Remove from selected categories
                    selectedCategories.remove(category);

                    // If no categories selected, check "All"
                    if (selectedCategories.isEmpty()) {
                        Chip allChip = (Chip) categoryChipGroup.getChildAt(0);
                        allChip.setChecked(true);
                    }
                }
            }

            applyFilters();
        });

        return chip;
    }

    /**
     * Apply search and category filters to the favorite posts
     * Filters are applied client-side on the in-memory list
     */
    private void applyFilters() {
        if (allFavorites.isEmpty()) {
            showEmptyState();
            return;
        }

        List<Post> filteredPosts = new ArrayList<>(allFavorites);

        // Apply category filter (for multiple selected categories)
        if (!selectedCategories.isEmpty()) {
            filteredPosts = filteredPosts.stream()
                    .filter(post -> selectedCategories.contains(post.getCategory()))
                    .collect(Collectors.toList());
        }

        // Apply search filter
        if (!currentSearchQuery.isEmpty()) {
            String query = currentSearchQuery.toLowerCase();
            filteredPosts = filteredPosts.stream()
                    .filter(post ->
                            post.getTitle().toLowerCase().contains(query) ||
                            post.getBody().toLowerCase().contains(query))
                    .collect(Collectors.toList());
        }

        // Update adapter
        if (filteredPosts.isEmpty()) {
            if (!currentSearchQuery.isEmpty()) {
                showEmptyState("No favorites found for \"" + currentSearchQuery + "\"");
            } else if (!selectedCategories.isEmpty()) {
                showEmptyState("No favorites in selected categories");
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
                "Tap the star on any post to add it to your favorites!");
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

