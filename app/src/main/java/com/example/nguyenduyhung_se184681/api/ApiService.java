package com.example.nguyenduyhung_se184681.api;

import com.example.nguyenduyhung_se184681.model.GoogleBooksResponse;
import com.example.nguyenduyhung_se184681.model.Post;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit API Service interface
 * Defines endpoints for Google Books API
 */
public interface ApiService {

    // Fetch all posts from JSONPlaceholder (OLD - for reference)
    @GET("posts")
    Call<List<Post>> getPosts();

    // Fetch books from Google Books API
    // Example: https://www.googleapis.com/books/v1/volumes?q=programming&maxResults=40
    @GET("volumes")
    Call<GoogleBooksResponse> searchBooks(
            @Query("q") String query,
            @Query("maxResults") int maxResults
    );
}

