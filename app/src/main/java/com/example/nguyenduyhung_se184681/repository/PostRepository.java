package com.example.nguyenduyhung_se184681.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.nguyenduyhung_se184681.database.AppDatabase;
import com.example.nguyenduyhung_se184681.database.PostDao;
import com.example.nguyenduyhung_se184681.model.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class implementing offline-first architecture
 * NOW using REAL Google Books API
 */
public class PostRepository {

    private static final String TAG = "PostRepository";
    private final PostDao postDao;
    private final ExecutorService executorService;

    public PostRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        postDao = database.postDao();
        executorService = Executors.newFixedThreadPool(2);
    }

    // Get all posts from database (LiveData)
    public LiveData<List<Post>> getAllPosts() {
        return postDao.getAllPosts();
    }

    // Get post by ID from database (LiveData)
    public LiveData<Post> getPostById(int postId) {
        return postDao.getPostById(postId);
    }

    // Get favorite posts
    public LiveData<List<Post>> getFavoritePosts() {
        return postDao.getFavoritePosts();
    }

    // Search posts
    public LiveData<List<Post>> searchPosts(String query) {
        return postDao.searchPosts(query);
    }

    // Get posts by category
    public LiveData<List<Post>> getPostsByCategory(String category) {
        return postDao.getPostsByCategory(category);
    }

    // Get posts by multiple categories
    public LiveData<List<Post>> getPostsByCategories(List<String> categories) {
        return postDao.getPostsByCategories(categories);
    }

    // Get all categories
    public LiveData<List<String>> getAllCategories() {
        return postDao.getAllCategories();
    }

    // Check if database is empty
    public void isDatabaseEmpty(DatabaseCheckCallback callback) {
        executorService.execute(() -> {
            int count = postDao.getPostCount();
            callback.onResult(count == 0);
        });
    }

    // Fetch posts from Google Books API
    public void fetchPostsFromApi(final FetchCallback callback) {
        executorService.execute(() -> {
            try {
                // Multiple book searches to get diverse content
                String[] searchQueries = {
                    "programming",
                    "android development",
                    "java",
                    "mobile apps",
                    "software engineering"
                };

                List<Post> allPosts = new ArrayList<>();
                int postId = 1;

                // Get current favorite states before replacing
                List<Post> existingPosts = postDao.getAllPostsSync();

                for (String query : searchQueries) {
                    try {
                        // Call Google Books API
                        retrofit2.Response<com.example.nguyenduyhung_se184681.model.GoogleBooksResponse> response =
                            com.example.nguyenduyhung_se184681.api.RetrofitClient.getApiService()
                                .searchBooks(query, 20)
                                .execute();

                        if (response.isSuccessful() && response.body() != null) {
                            com.example.nguyenduyhung_se184681.model.GoogleBooksResponse booksResponse = response.body();

                            if (booksResponse.getItems() != null) {
                                // Convert books to Post model
                                for (com.example.nguyenduyhung_se184681.model.GoogleBooksResponse.BookItem book : booksResponse.getItems()) {
                                    Post post = convertBookToPost(book, postId++);
                                    if (post != null) {
                                        allPosts.add(post);
                                    }

                                    // Limit to 100 posts total
                                    if (allPosts.size() >= 100) break;
                                }
                            }
                        }

                        if (allPosts.size() >= 100) break;

                        // Small delay between requests
                        Thread.sleep(200);

                    } catch (Exception e) {
                        Log.w(TAG, "Error fetching books for query: " + query, e);
                    }
                }

                if (allPosts.isEmpty()) {
                    throw new Exception("No books found from API");
                }

                // Preserve favorite status from existing posts
                for (Post newPost : allPosts) {
                    for (Post existingPost : existingPosts) {
                        if (newPost.getId() == existingPost.getId()) {
                            newPost.setFavorite(existingPost.isFavorite());
                            break;
                        }
                    }
                }

                // Save to database
                postDao.insertAll(allPosts);
                Log.d(TAG, "Successfully loaded " + allPosts.size() + " books from Google Books API");

                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching books from API", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    /**
     * Convert Google Books BookItem to Post model
     */
    private Post convertBookToPost(com.example.nguyenduyhung_se184681.model.GoogleBooksResponse.BookItem book, int postId) {
        try {
            com.example.nguyenduyhung_se184681.model.GoogleBooksResponse.VolumeInfo info = book.getVolumeInfo();
            if (info == null || info.getTitle() == null) {
                return null;
            }

            Post post = new Post();
            post.setId(postId);

            // Set title
            post.setTitle(info.getTitle());

            // Set description/body
            String body = info.getDescription();
            if (body == null || body.isEmpty()) {
                body = "A book about " + info.getTitle();
                if (info.getAuthors() != null && !info.getAuthors().isEmpty()) {
                    body += " by " + String.join(", ", info.getAuthors());
                }
            }
            // Limit body length
            if (body.length() > 500) {
                body = body.substring(0, 497) + "...";
            }
            post.setBody(body);

            // Set category from book categories
            String category = "Technology";
            if (info.getCategories() != null && !info.getCategories().isEmpty()) {
                category = info.getCategories().get(0);
                // Simplify category names
                if (category.contains("Programming") || category.contains("Computers")) {
                    category = "Programming";
                } else if (category.contains("Technology")) {
                    category = "Technology";
                } else if (category.contains("Business")) {
                    category = "Business";
                } else if (category.contains("Education")) {
                    category = "Education";
                }
            }
            post.setCategory(category);

            // Set image URL
            String imageUrl = null;
            if (info.getImageLinks() != null) {
                imageUrl = info.getImageLinks().getThumbnail();
                if (imageUrl != null) {
                    // Use HTTPS
                    imageUrl = imageUrl.replace("http://", "https://");
                }
            }
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = "https://picsum.photos/400/600?random=" + postId;
            }
            post.setImageUrl(imageUrl);

            // Set userId (for legacy purposes)
            post.setUserId((postId % 5) + 1);

            post.setFavorite(false);

            return post;
        } catch (Exception e) {
            Log.e(TAG, "Error converting book to post", e);
            return null;
        }
    }

    // Toggle favorite status
    public void toggleFavorite(Post post, FavoriteCallback callback) {
        executorService.execute(() -> {
            try {
                // Read current stored value to avoid race conditions or stale objects
                Post current = postDao.getPostByIdSync(post.getId());
                if (current == null) {
                    // Fall back to toggling the passed post if DB record missing
                    boolean newState = !post.isFavorite();
                    postDao.updateFavoriteStatus(post.getId(), newState);
                    if (callback != null) callback.onSuccess(newState);
                } else {
                    boolean newState = !current.isFavorite();
                    // Update only the favorite column to avoid overwriting other fields
                    postDao.updateFavoriteStatus(current.getId(), newState);
                    if (callback != null) callback.onSuccess(newState);
                }
             } catch (Exception e) {
                 Log.e(TAG, "Error toggling favorite", e);
                 if (callback != null) {
                     callback.onError(e.getMessage());
                 }
             }
         });
     }

    // Callback interfaces
    public interface FetchCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface FavoriteCallback {
        void onSuccess(boolean isFavorite);
        void onError(String error);
    }

    public interface DatabaseCheckCallback {
        void onResult(boolean isEmpty);
    }
}
