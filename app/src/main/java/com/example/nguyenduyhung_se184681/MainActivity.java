package com.example.nguyenduyhung_se184681;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.nguyenduyhung_se184681.util.NetworkUtils;
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
    private LinearLayout errorContainer;
    private TextView errorTextView;
    private TextView networkStatusText;
    private ImageView errorIcon;
    private Button retryButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText searchEditText;
    private ChipGroup categoryChipGroup;
    private ChipGroup filterChipGroup;

    // State
    private String currentSearchQuery = "";
    private Set<String> selectedCategories = new HashSet<>();
    private boolean showFavoritesOnly = false;
    // Keep reference to currently observed LiveData so we can remove observers cleanly
    private androidx.lifecycle.LiveData<java.util.List<Post>> activePostsLiveData = null;
    // Keep reference to category "All" chip to avoid ID conflict with filter "All" chip
    private Chip categoryAllChip = null;
    // Cache all posts to avoid re-observing LiveData
    private List<Post> allPostsCache = new ArrayList<>();
    private boolean isLiveDataObserved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Restore state if available
        if (savedInstanceState != null) {
            ArrayList<String> savedCategories = savedInstanceState.getStringArrayList("selectedCategories");
            if (savedCategories != null) {
                selectedCategories = new HashSet<>(savedCategories);
                android.util.Log.d("MainActivity", "Restored selectedCategories from savedInstanceState: " + selectedCategories);
            }
            showFavoritesOnly = savedInstanceState.getBoolean("showFavoritesOnly", false);
            currentSearchQuery = savedInstanceState.getString("currentSearchQuery", "");
        }

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
        errorContainer = findViewById(R.id.error_container);
        errorTextView = findViewById(R.id.error_text);
        networkStatusText = findViewById(R.id.network_status_text);
        errorIcon = findViewById(R.id.error_icon);
        retryButton = findViewById(R.id.retry_button);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        searchEditText = findViewById(R.id.search_edit_text);
        categoryChipGroup = findViewById(R.id.category_chip_group);
        filterChipGroup = findViewById(R.id.filter_chip_group);

        // Setup FAB for Favorites navigation
        findViewById(R.id.fab_favorites).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        // Setup retry button
        retryButton.setOnClickListener(v -> {
            loadData();
        });
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

    private void setupFilters() {
        // Setup All/Favorites filter
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // If no chips are checked, keep current state (don't change anything)
            if (checkedIds.isEmpty()) {
                // Restore the previously selected chip
                if (showFavoritesOnly) {
                    filterChipGroup.check(R.id.chip_favorites);
                } else {
                    filterChipGroup.check(R.id.chip_all);
                }
                return;
            }

            int checkedId = checkedIds.get(0);
            boolean newShowFavoritesOnly = checkedId == R.id.chip_favorites;

            // Only update if state actually changed
            if (newShowFavoritesOnly != showFavoritesOnly) {
                showFavoritesOnly = newShowFavoritesOnly;

                // Reset observation flag when switching data source
                isLiveDataObserved = false;

                // Hide category filters when Favorites is selected
                View categoryScrollView = findViewById(R.id.category_scroll_view);
                if (categoryScrollView != null) {
                    if (showFavoritesOnly) {
                        categoryScrollView.setVisibility(View.GONE);
                    } else {
                        categoryScrollView.setVisibility(View.VISIBLE);
                    }
                }

                applyFilters();
            }
        });

        // Load and setup category filters
        loadCategories();
    }

    private void loadCategories() {
        viewModel.getAllCategories().observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                categoryChipGroup.removeAllViews();

                // Add "All" category chip and store reference to avoid ID conflict
                categoryAllChip = createCategoryChip("All", true);
                categoryChipGroup.addView(categoryAllChip);

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

        // Remove the default checkmark icon to avoid showing 2 checkmarks
        chip.setCheckedIcon(null);
        chip.setChipIconVisible(false);

        // Set colors based on checked state
        updateChipColors(chip, isChecked);

        chip.setOnCheckedChangeListener((buttonView, isCheckedNow) -> {
            // Prevent recursive calls
            if (!buttonView.isPressed()) return;

            // Update chip colors when state changes
            updateChipColors(chip, isCheckedNow);

            // Use safe equals to avoid NPE
            boolean isAll = "All".equals(category);

            if (isAll) {
                // "All" chip behavior: clear all selections and reset to all
                if (isCheckedNow) {
                    // Uncheck all other chips
                    for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                        View child = categoryChipGroup.getChildAt(i);
                        if (child instanceof Chip) {
                            Chip otherChip = (Chip) child;
                            if (otherChip != chip && otherChip.isChecked()) {
                                otherChip.setChecked(false);
                                // Manually update colors for programmatically unchecked chips
                                updateChipColors(otherChip, false);
                            }
                        }
                    }
                    selectedCategories.clear();
                } else {
                    // "All" cannot be unchecked manually - keep it checked if nothing else is selected
                    if (selectedCategories.isEmpty()) {
                        chip.setChecked(true);
                        // Manually update colors
                        updateChipColors(chip, true);
                    }
                }
            } else {
                // Regular category chip behavior
                if (isCheckedNow) {
                    // Uncheck "All" when selecting a specific category - use stored reference
                    if (categoryAllChip != null && categoryAllChip.isChecked()) {
                        categoryAllChip.setChecked(false);
                        // Manually update colors since programmatic setChecked might not trigger listener
                        updateChipColors(categoryAllChip, false);
                    }
                    // Add to selected categories
                    selectedCategories.add(category);
                } else {
                    // Remove from selected categories
                    selectedCategories.remove(category);

                    // If no categories selected, check "All"
                    if (selectedCategories.isEmpty()) {
                        if (categoryAllChip != null) {
                            categoryAllChip.setChecked(true);
                            // Manually update colors
                            updateChipColors(categoryAllChip, true);
                        }
                    }
                }
            }

            applyFilters();
        });

        return chip;
    }

    /**
     * Update chip colors based on checked state
     * Checked: blue background, white text
     * Unchecked: light blue background, dark blue text
     */
    private void updateChipColors(Chip chip, boolean isChecked) {
        if (isChecked) {
            chip.setChipBackgroundColorResource(R.color.chip_background_checked);
            chip.setTextColor(getResources().getColor(R.color.chip_text_checked, null));
        } else {
            chip.setChipBackgroundColorResource(R.color.chip_background);
            chip.setTextColor(getResources().getColor(R.color.chip_text, null));
        }
    }

    /**
     * Restore category chip visual states based on selectedCategories
     * Called when returning from Detail screen to ensure chips show correct colors
     * Temporarily removes listener to avoid triggering logic that checks isPressed()
     */
    private void restoreCategoryChipStates() {
        if (categoryChipGroup == null) {
            android.util.Log.d("MainActivity", "restoreCategoryChipStates: categoryChipGroup is null");
            return;
        }

        int chipCount = categoryChipGroup.getChildCount();
        android.util.Log.d("MainActivity", "restoreCategoryChipStates: chipCount=" + chipCount + ", selectedCategories=" + selectedCategories);

        if (chipCount == 0) {
            // No chips created yet, skip restore (will be handled by loadCategories observer)
            android.util.Log.d("MainActivity", "restoreCategoryChipStates: No chips yet, skipping");
            return;
        }

        for (int i = 0; i < chipCount; i++) {
            View child = categoryChipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                String chipText = chip.getText().toString();

                if ("All".equals(chipText)) {
                    // "All" should be checked only if no categories selected
                    boolean shouldBeChecked = selectedCategories.isEmpty();
                    android.util.Log.d("MainActivity", "Chip 'All': currentChecked=" + chip.isChecked() + ", shouldBe=" + shouldBeChecked);
                    if (chip.isChecked() != shouldBeChecked) {
                        // Remove listener temporarily to avoid triggering OnCheckedChangeListener
                        chip.setOnCheckedChangeListener(null);
                        chip.setChecked(shouldBeChecked);
                        updateChipColors(chip, shouldBeChecked);
                        // Restore listener
                        restoreChipListener(chip, chipText, shouldBeChecked);
                        android.util.Log.d("MainActivity", "Chip 'All' updated to " + shouldBeChecked);
                    }
                } else {
                    // Regular category chip
                    boolean shouldBeChecked = selectedCategories.contains(chipText);
                    android.util.Log.d("MainActivity", "Chip '" + chipText + "': currentChecked=" + chip.isChecked() + ", shouldBe=" + shouldBeChecked);
                    if (chip.isChecked() != shouldBeChecked) {
                        // Remove listener temporarily to avoid triggering OnCheckedChangeListener
                        chip.setOnCheckedChangeListener(null);
                        chip.setChecked(shouldBeChecked);
                        updateChipColors(chip, shouldBeChecked);
                        // Restore listener
                        restoreChipListener(chip, chipText, shouldBeChecked);
                        android.util.Log.d("MainActivity", "Chip '" + chipText + "' updated to " + shouldBeChecked);
                    }
                }
            }
        }
    }

    /**
     * Restore the OnCheckedChangeListener for a category chip
     * This is the same listener logic from createCategoryChip()
     */
    private void restoreChipListener(Chip chip, String category, boolean currentCheckedState) {
        chip.setOnCheckedChangeListener((buttonView, isCheckedNow) -> {
            // Prevent recursive calls
            if (!buttonView.isPressed()) return;

            // Update chip colors when state changes
            updateChipColors(chip, isCheckedNow);

            // Use safe equals to avoid NPE
            boolean isAll = "All".equals(category);

            if (isAll) {
                // "All" chip behavior: clear all selections and reset to all
                if (isCheckedNow) {
                    // Uncheck all other chips
                    for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                        View child = categoryChipGroup.getChildAt(i);
                        if (child instanceof Chip) {
                            Chip otherChip = (Chip) child;
                            if (otherChip != chip && otherChip.isChecked()) {
                                otherChip.setChecked(false);
                                // Manually update colors for programmatically unchecked chips
                                updateChipColors(otherChip, false);
                            }
                        }
                    }
                    selectedCategories.clear();
                } else {
                    // "All" cannot be unchecked manually - keep it checked if nothing else is selected
                    if (selectedCategories.isEmpty()) {
                        chip.setChecked(true);
                        // Manually update colors
                        updateChipColors(chip, true);
                    }
                }
            } else {
                // Regular category chip behavior
                if (isCheckedNow) {
                    // Uncheck "All" when selecting a specific category - use stored reference
                    if (categoryAllChip != null && categoryAllChip.isChecked()) {
                        categoryAllChip.setChecked(false);
                        // Manually update colors since programmatic setChecked might not trigger listener
                        updateChipColors(categoryAllChip, false);
                    }
                    // Add to selected categories
                    selectedCategories.add(category);
                } else {
                    // Remove from selected categories
                    selectedCategories.remove(category);

                    // If no categories selected, check "All"
                    if (selectedCategories.isEmpty()) {
                        if (categoryAllChip != null) {
                            categoryAllChip.setChecked(true);
                            // Manually update colors
                            updateChipColors(categoryAllChip, true);
                        }
                    }
                }
            }

            applyFilters();
        });
    }

    private void loadData() {
        // Check network status first
        boolean hasNetwork = NetworkUtils.isNetworkAvailable(this);

        // Check if database is empty
        viewModel.isDatabaseEmpty(isEmpty -> {
            runOnUiThread(() -> {
                if (isEmpty) {
                    // Database is empty - need to fetch from API
                    if (hasNetwork) {
                        // Has network - fetch from API
                        showLoading();
                        fetchFromApi();
                    } else {
                        // No network and no cached data - show error
                        showError(
                            "No internet connection",
                            "Cannot load data. Please connect to the internet and try again.",
                            true
                        );
                    }
                } else {
                    // Database has data
                    if (hasNetwork) {
                        // Has network - load from cache first, then refresh in background
                        applyFilters();
                        // Silently refresh data in background
                        viewModel.fetchPostsFromApi(new PostRepository.FetchCallback() {
                            @Override
                            public void onSuccess() {
                                // Data refreshed silently
                            }

                            @Override
                            public void onError(String message) {
                                // Ignore error - we already have cached data
                            }
                        });
                    } else {
                        // No network - load from cache with warning
                        applyFilters();
                        Toast.makeText(MainActivity.this,
                                "Offline mode: Showing cached data", Toast.LENGTH_SHORT).show();
                    }
                }
            });
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
                    // Check network status
                    boolean hasNetwork = NetworkUtils.isNetworkAvailable(MainActivity.this);

                    // Try to load from cache anyway
                    viewModel.isDatabaseEmpty(isEmpty -> {
                        runOnUiThread(() -> {
                            if (isEmpty) {
                                // No cached data - show error
                                String networkStatus = hasNetwork ?
                                    "Connected but unable to load data" :
                                    "No internet connection";
                                showError(
                                    networkStatus,
                                    "Could not load data: " + message,
                                    true
                                );
                            } else {
                                // Has cached data - show it with warning
                                applyFilters();
                                Toast.makeText(MainActivity.this,
                                        "Network error. Showing cached data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                });
            }
        });
    }

    private void refreshData() {
        // Check network before attempting refresh
        if (!NetworkUtils.isNetworkAvailable(this)) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this,
                "No internet connection. Cannot refresh.", Toast.LENGTH_SHORT).show();
            return;
        }

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
        androidx.lifecycle.LiveData<List<Post>> postsLiveData;

        // Choose the base data source
        if (showFavoritesOnly) {
            postsLiveData = viewModel.getFavoritePosts();
        } else {
            postsLiveData = viewModel.getAllPosts();
        }

        // Check if we need to switch LiveData source
        boolean needToSwitchSource = (activePostsLiveData != postsLiveData);

        if (needToSwitchSource) {
            // Remove observers from previously observed LiveData (if any)
            if (activePostsLiveData != null) {
                activePostsLiveData.removeObservers(this);
            }

            // Track the currently active LiveData
            activePostsLiveData = postsLiveData;
            isLiveDataObserved = false;
        }

        // Only observe if not already observed
        if (!isLiveDataObserved) {
            isLiveDataObserved = true;

            // Observe and cache posts
            postsLiveData.observe(this, posts -> {
                if (posts != null) {
                    // Update cache
                    allPostsCache = new ArrayList<>(posts);
                    // Apply filters with new data
                    filterAndDisplayPosts();
                }
            });
        } else {
            // Already observing, just apply filters to cached data
            filterAndDisplayPosts();
        }
    }

    /**
     * Filter cached posts based on current state and update UI
     * This method does NOT re-observe LiveData, preserving selection state
     */
    private void filterAndDisplayPosts() {
        if (allPostsCache.isEmpty()) {
            showError("No data", "No books available", false);
            return;
        }

        List<Post> filteredPosts = new ArrayList<>(allPostsCache);

        // Apply category filter (client-side for multiple categories)
        if (!selectedCategories.isEmpty()) {
            filteredPosts = filteredPosts.stream()
                    .filter(post -> selectedCategories.contains(post.getCategory()))
                    .collect(Collectors.toList());
        }

        // Apply search filter (search by book title only - null-safe and case-insensitive)
        if (!currentSearchQuery.isEmpty()) {
            String query = currentSearchQuery.toLowerCase();
            filteredPosts = filteredPosts.stream()
                    .filter(post -> {
                        String title = post.getTitle() != null ? post.getTitle().toLowerCase() : "";
                        return title.contains(query);
                    })
                    .collect(Collectors.toList());
        }

        // Update UI based on results
        if (!filteredPosts.isEmpty()) {
            adapter.setPosts(filteredPosts);
            showContent();
        } else {
            String title = "No results";
            String message;
            if (showFavoritesOnly) {
                message = "No favorites yet.\nTap the star on any book to add it to favorites!";
            } else if (!currentSearchQuery.isEmpty()) {
                message = "No books found for \"" + currentSearchQuery + "\"";
            } else if (!selectedCategories.isEmpty()) {
                message = "No books found in selected categories";
            } else {
                message = "No books available";
            }
            showError(title, message, false);
        }
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
        errorContainer.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        errorContainer.setVisibility(View.GONE);
    }

    private void showError(String networkStatus, String message, boolean showRetryButton) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        errorContainer.setVisibility(View.VISIBLE);

        // Update error text
        errorTextView.setText(message);

        // Update network status
        networkStatusText.setText(networkStatus);

        // Show/hide retry button
        retryButton.setVisibility(showRetryButton ? View.VISIBLE : View.GONE);

        // Update icon based on error type
        if (networkStatus.toLowerCase().contains("no internet") ||
            networkStatus.toLowerCase().contains("no connection")) {
            errorIcon.setImageResource(android.R.drawable.stat_notify_error);
        } else {
            errorIcon.setImageResource(android.R.drawable.ic_dialog_alert);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d("MainActivity", "onResume called, selectedCategories=" + selectedCategories);

        // Restore category chip visual states when returning from DetailActivity
        // Use postDelayed to ensure UI is fully rendered before restoring
        recyclerView.postDelayed(() -> {
            android.util.Log.d("MainActivity", "About to restore chip states");
            restoreCategoryChipStates();
        }, 100);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current state to survive activity recreation
        outState.putStringArrayList("selectedCategories", new ArrayList<>(selectedCategories));
        outState.putBoolean("showFavoritesOnly", showFavoritesOnly);
        outState.putString("currentSearchQuery", currentSearchQuery);
        android.util.Log.d("MainActivity", "Saved state: selectedCategories=" + selectedCategories);
    }
}
