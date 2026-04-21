package com.example.bookingapp.db;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "apartments")
public class Apartment {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String description;
    public String price;
    public String date;
    public String imageUrl;
    public Apartment() {}
}