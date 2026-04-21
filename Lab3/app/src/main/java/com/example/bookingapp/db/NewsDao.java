package com.example.bookingapp.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface NewsDao {
    @Query("SELECT * FROM news_cache")
    List<NewsArticle> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<NewsArticle> articles);

    @Query("DELETE FROM news_cache")
    void deleteAll();
}