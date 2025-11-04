package com.example.nguyenduyhung_se184681.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.nguyenduyhung_se184681.model.Post;
import com.example.nguyenduyhung_se184681.repository.PostRepository;

import java.util.List;

/**
 * ViewModel for managing Post data
 * Survives configuration changes
 */
public class PostViewModel extends AndroidViewModel {

    private final PostRepository repository;
    private final LiveData<List<Post>> allPosts;

    public PostViewModel(@NonNull Application application) {
        super(application);
        repository = new PostRepository(application);
        allPosts = repository.getAllPosts();
    }

    // Get all posts
    public LiveData<List<Post>> getAllPosts() {
        return allPosts;
    }

    // Get post by ID
    public LiveData<Post> getPostById(int postId) {
        return repository.getPostById(postId);
    }

    // Get favorite posts
    public LiveData<List<Post>> getFavoritePosts() {
        return repository.getFavoritePosts();
    }

    // Search posts
    public LiveData<List<Post>> searchPosts(String query) {
        return repository.searchPosts(query);
    }

    // Get posts by category
    public LiveData<List<Post>> getPostsByCategory(String category) {
        return repository.getPostsByCategory(category);
    }

    // Get posts by multiple categories
    public LiveData<List<Post>> getPostsByCategories(List<String> categories) {
        return repository.getPostsByCategories(categories);
    }

    // Get all categories
    public LiveData<List<String>> getAllCategories() {
        return repository.getAllCategories();
    }

    // Fetch posts from API
    public void fetchPostsFromApi(PostRepository.FetchCallback callback) {
        repository.fetchPostsFromApi(callback);
    }

    // Toggle favorite
    public void toggleFavorite(Post post, PostRepository.FavoriteCallback callback) {
        repository.toggleFavorite(post, callback);
    }

    // Check if database is empty
    public void isDatabaseEmpty(PostRepository.DatabaseCheckCallback callback) {
        repository.isDatabaseEmpty(callback);
    }
}

