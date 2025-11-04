package com.example.nguyenduyhung_se184681.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Post model representing data from JSONPlaceholder API
 * Also serves as Room entity for local caching
 * NOW with REAL images from Picsum Photos!
 */
@Entity(tableName = "posts")
public class Post {
    @PrimaryKey
    private int id;
    private int userId;
    private String title;
    private String body;
    private boolean isFavorite; // Track favorite status
    private String category; // For filtering (derived from userId)
    private String imageUrl; // Optional: can be generated based on post ID

    // Default constructor for Gson/Room
    public Post() {
    }

    // Constructor - Room will ignore this and use default constructor
    @Ignore
    public Post(int id, int userId, String title, String body) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.isFavorite = false;
        this.category = "Category " + ((userId % 5) + 1); // Generate 5 categories
        this.imageUrl = "https://picsum.photos/400/300?random=" + id; // Random image for each post
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        // Auto-generate imageUrl if not set
        if (this.imageUrl == null || this.imageUrl.isEmpty()) {
            this.imageUrl = "https://picsum.photos/400/300?random=" + id;
        }
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        // Auto-generate category if not set
        if (this.category == null || this.category.isEmpty()) {
            this.category = "Category " + ((userId % 5) + 1);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

