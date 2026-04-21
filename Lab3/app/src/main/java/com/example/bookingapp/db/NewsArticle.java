package com.example.bookingapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "news_cache")
public class NewsArticle {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String description;
}