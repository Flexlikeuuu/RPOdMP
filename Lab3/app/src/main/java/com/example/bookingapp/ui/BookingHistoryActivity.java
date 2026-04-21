package com.example.bookingapp.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookingapp.R;
import com.example.bookingapp.adapter.BookingAdapter;
import com.example.bookingapp.repository.AppRepository;

public class BookingHistoryActivity extends AppCompatActivity {
    private AppRepository repo;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        rv = findViewById(R.id.rvBookings);
        rv.setLayoutManager(new LinearLayoutManager(this));

        repo = new AppRepository(this);
        loadBookings();
    }

    private void loadBookings() {
        repo.getAllBookings(bookings -> {
            runOnUiThread(() -> {
                rv.setAdapter(new BookingAdapter(bookings, booking -> {
                    repo.deleteBooking(booking);
                    Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
                    loadBookings();
                }));
            });
        });
    }
}