package com.example.bookingapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookings")
public class Booking {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int apartmentId;
    public String apartmentTitle;
    public String bookingDate;
    public String userId;
}