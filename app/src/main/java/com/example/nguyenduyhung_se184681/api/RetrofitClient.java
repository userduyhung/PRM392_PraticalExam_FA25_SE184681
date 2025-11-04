package com.example.nguyenduyhung_se184681.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit Client Singleton
 * Provides configured Retrofit instance for Google Books API
 */
public class RetrofitClient {

    // Google Books API - Free, no API key required for basic usage
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}

