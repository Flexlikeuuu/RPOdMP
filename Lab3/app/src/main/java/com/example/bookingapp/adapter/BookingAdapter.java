package com.example.bookingapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookingapp.R;
import com.example.bookingapp.db.Booking;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
    private List<Booking> list;
    private OnBookingDeleteListener listener;

    public interface OnBookingDeleteListener {
        void onDelete(Booking booking);
    }

    public BookingAdapter(List<Booking> list, OnBookingDeleteListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking b = list.get(position);
        holder.title.setText(b.apartmentTitle);
        holder.date.setText(b.bookingDate);
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(b));
    }

    @Override
    public int getItemCount() { return list != null ? list.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date;
        ImageButton btnDelete;
        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.tvBookingTitle);
            date = v.findViewById(R.id.tvBookingDate);
            btnDelete = v.findViewById(R.id.btnDeleteBooking);
        }
    }
}