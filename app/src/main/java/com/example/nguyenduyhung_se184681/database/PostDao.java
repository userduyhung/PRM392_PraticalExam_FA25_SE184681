package com.example.nguyenduyhung_se184681.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.nguyenduyhung_se184681.model.Post;

import java.util.List;

/**
 * Data Access Object for Post entity
 * Provides database operations for posts
 */
@Dao
public interface PostDao {

    // Insert all posts (for initial data load)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Post> posts);

    // Insert single post
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Post post);

    // Update post (for favorite toggle)
    @Update
    void update(Post post);

    // Get all posts
    @Query("SELECT * FROM posts ORDER BY id ASC")
    LiveData<List<Post>> getAllPosts();

    // Get all posts (synchronous - for repository)
    @Query("SELECT * FROM posts ORDER BY id ASC")
    List<Post> getAllPostsSync();

    // Get post by ID
    @Query("SELECT * FROM posts WHERE id = :postId")
    LiveData<Post> getPostById(int postId);

    // Get post by ID (synchronous)
    @Query("SELECT * FROM posts WHERE id = :postId")
    Post getPostByIdSync(int postId);

    // Get favorite posts
    @Query("SELECT * FROM posts WHERE isFavorite = 1 ORDER BY id ASC")
    LiveData<List<Post>> getFavoritePosts();

    // Search posts by title or body
    @Query("SELECT * FROM posts WHERE title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' ORDER BY id ASC")
    LiveData<List<Post>> searchPosts(String query);

    // Get posts by category
    @Query("SELECT * FROM posts WHERE category = :category ORDER BY id ASC")
    LiveData<List<Post>> getPostsByCategory(String category);

    // Get posts by multiple categories
    @Query("SELECT * FROM posts WHERE category IN (:categories) ORDER BY id ASC")
    LiveData<List<Post>> getPostsByCategories(List<String> categories);

    // Get all unique categories
    @Query("SELECT DISTINCT category FROM posts ORDER BY category ASC")
    LiveData<List<String>> getAllCategories();

    // Delete all posts (for refresh)
    @Query("DELETE FROM posts")
    void deleteAll();

    // Check if table is empty
    @Query("SELECT COUNT(*) FROM posts")
    int getPostCount();

    // Toggle favorite status
    @Query("UPDATE posts SET isFavorite = :isFavorite WHERE id = :postId")
    void updateFavoriteStatus(int postId, boolean isFavorite);
}

