package com.example.bookingapp.db;
import androidx.room.*;
import java.util.List;

@Dao
public interface ApartmentDao {
    @Query("SELECT * FROM apartments")
    List<Apartment> getAll();

    @Insert
    void insert(Apartment apartment);

    @Update
    void update(Apartment apartment);

    @Delete
    void delete(Apartment apartment);
}