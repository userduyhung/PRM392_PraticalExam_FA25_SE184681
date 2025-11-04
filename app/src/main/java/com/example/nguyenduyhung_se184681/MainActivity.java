package com.example.nguyenduyhung_se184681;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.nguyenduyhung_se184681.adapter.PostAdapter;
import com.example.nguyenduyhung_se184681.model.Post;
import com.example.nguyenduyhung_se184681.repository.PostRepository;
import com.example.nguyenduyhung_se184681.viewmodel.PostViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main Activity - List Screen
 * Features:
 * - Displays posts in RecyclerView with LazyColumn-like behavior
 * - Search bar for filtering posts
 * - Category filters (dynamically generated from data)
 * - Pull-to-refresh to fetch latest data
 * - Offline-first: Shows cached data when network unavailable
 * - Navigation to detail screen on item click
 */
public class MainActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {

    private PostViewModel viewModel;
    private PostAdapter adapter;

    // UI Components
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText searchEditText;
    private ChipGroup categoryChipGroup;
    private ChipGroup filterChipGroup;

    // State
    private String currentSearchQuery = "";
    private Set<String> selectedCategories = new HashSet<>();
    private boolean showFavoritesOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(PostViewModel.class);

        // Initialize views
        initViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup search functionality
        setupSearch();

        // Setup filters
        setupFilters();

        // Load initial data
        loadData();

        // Setup pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        errorTextView = findViewById(R.id.error_text);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        searchEditText = findViewById(R.id.search_edit_text);
        categoryChipGroup = findViewById(R.id.category_chip_group);
        filterChipGroup = findViewById(R.id.filter_chip_group);

        // Setup FAB for Favorites navigation
        findViewById(R.id.fab_favorites).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });
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

    private void setupFilters() {
        // Setup All/Favorites filter
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            showFavoritesOnly = checkedId == R.id.chip_favorites;
            applyFilters();
        });

        // Load and setup category filters
        loadCategories();
    }

    private void loadCategories() {
        viewModel.getAllCategories().observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                categoryChipGroup.removeAllViews();

                // Add "All" category chip
                Chip allChip = createCategoryChip("All", true);
                categoryChipGroup.addView(allChip);

                // Add category chips from data
                for (String category : categories) {
                    Chip chip = createCategoryChip(category, false);
                    categoryChipGroup.addView(chip);
                }
            }
        });
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

    private void loadData() {
        // Check if database is empty
        viewModel.isDatabaseEmpty(isEmpty -> {
            if (isEmpty) {
                // Fetch from API
                runOnUiThread(() -> {
                    showLoading();
                    fetchFromApi();
                });
            } else {
                // Load from database
                runOnUiThread(this::applyFilters);
            }
        });
    }

    private void fetchFromApi() {
        viewModel.fetchPostsFromApi(new PostRepository.FetchCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    hideLoading();
                    applyFilters();
                    Toast.makeText(MainActivity.this,
                            "Data loaded successfully", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    hideLoading();
                    // Try to load from cache anyway
                    viewModel.isDatabaseEmpty(isEmpty -> {
                        if (isEmpty) {
                            showError("Could not load data. Please check your connection.");
                        } else {
                            applyFilters();
                            Toast.makeText(MainActivity.this,
                                    "Showing cached data", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });
    }

    private void refreshData() {
        viewModel.fetchPostsFromApi(new PostRepository.FetchCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this,
                            "Data refreshed", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this,
                            "Failed to refresh: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void applyFilters() {
        LiveData<List<Post>> postsLiveData;

        // Choose the base data source
        if (showFavoritesOnly) {
            postsLiveData = viewModel.getFavoritePosts();
        } else {
            postsLiveData = viewModel.getAllPosts();
        }

        // Remove previous observers to prevent multiple updates
        postsLiveData.removeObservers(this);

        // Observe and update UI
        postsLiveData.observe(this, posts -> {
            if (posts == null) {
                showError("No posts available");
                return;
            }

            List<Post> filteredPosts = new ArrayList<>(posts);

            // Apply category filter (client-side for multiple categories)
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

            // Update UI based on results
            if (!filteredPosts.isEmpty()) {
                adapter.setPosts(filteredPosts);
                showContent();
            } else {
                if (showFavoritesOnly) {
                    showError("No favorites yet.\nTap the star on any post to add it to favorites!");
                } else if (!currentSearchQuery.isEmpty()) {
                    showError("No posts found for \"" + currentSearchQuery + "\"");
                } else if (!selectedCategories.isEmpty()) {
                    showError("No posts found in selected categories");
                } else {
                    showError("No posts available");
                }
            }
        });
    }

    @Override
    public void onPostClick(Post post) {
        // Navigate to detail screen
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_POST_ID, post.getId());
        startActivity(intent);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        errorTextView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(message);
    }
}

