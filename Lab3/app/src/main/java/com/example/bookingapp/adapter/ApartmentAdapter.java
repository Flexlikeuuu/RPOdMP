package com.example.bookingapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bookingapp.R;
import com.example.bookingapp.db.Apartment;
import java.util.List;

public class ApartmentAdapter extends RecyclerView.Adapter<ApartmentAdapter.ViewHolder> {
    private List<Apartment> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(Apartment a);
        void onDeleteClick(Apartment a);
        void onBookClick(Apartment a);
    }

    public ApartmentAdapter(List<Apartment> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<Apartment> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_apartment, p, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int p) {
        Apartment a = list.get(p);
        h.title.setText(a.title);
        h.price.setText(a.price + "$");
        h.description.setText(a.description);
        h.date.setText(a.date);

        Glide.with(h.itemView.getContext())
                .load(a.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(h.img);

        h.btnEdit.setOnClickListener(v -> listener.onEditClick(a));
        h.btnDelete.setOnClickListener(v -> listener.onDeleteClick(a));
        h.btnBook.setOnClickListener(v -> listener.onBookClick(a));
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, description, date;
        ImageView img;
        ImageButton btnEdit, btnDelete, btnBook;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            price = v.findViewById(R.id.tvPrice);
            description = v.findViewById(R.id.tvDescription);
            date = v.findViewById(R.id.tvDate);
            img = v.findViewById(R.id.ivApartment);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
            btnBook = v.findViewById(R.id.btnBook);
        }
    }
}