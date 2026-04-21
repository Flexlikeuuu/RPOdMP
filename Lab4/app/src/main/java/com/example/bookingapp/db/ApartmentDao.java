package com.example.bookingapp.db;
import androidx.room.*;
import java.util.List;

@Dao
public interface ApartmentDao {
    @Query("SELECT * FROM apartments")
    List<Apartment> getAll();
    @Query("SELECT * FROM apartments WHERE id = :id LIMIT 1")
    Apartment getById(int id);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Apartment apartment);
    @Delete
    void delete(Apartment apartment);
}