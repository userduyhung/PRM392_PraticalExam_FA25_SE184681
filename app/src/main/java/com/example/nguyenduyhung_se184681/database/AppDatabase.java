package com.example.nguyenduyhung_se184681.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.nguyenduyhung_se184681.model.Post;

/**
 * Room Database class for the application
 * Singleton pattern to ensure single instance
 * Version 3: Switched to Google Books API data
 */
@Database(entities = {Post.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract PostDao postDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "post_database"
            )
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
}

