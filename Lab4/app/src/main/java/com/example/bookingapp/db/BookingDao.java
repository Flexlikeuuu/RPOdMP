package com.example.bookingapp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface BookingDao {
    @Query("SELECT * FROM bookings WHERE userId = :uid ORDER BY id DESC")
    List<Booking> getAllForUser(String uid);

    @Insert
    void insert(Booking booking);

    @Delete
    void delete(Booking booking);
}