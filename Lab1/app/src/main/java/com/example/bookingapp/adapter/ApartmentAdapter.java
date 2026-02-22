package com.example.bookingapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookingapp.R;
import com.example.bookingapp.db.Apartment;
import java.util.List;

public class ApartmentAdapter extends RecyclerView.Adapter<ApartmentAdapter.ViewHolder> {
    private List<Apartment> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(Apartment apartment);
        void onDeleteClick(Apartment apartment);
        void onItemClick(Apartment apartment);
    }

    public ApartmentAdapter(List<Apartment> list) {
        this.list = list;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_apartment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Apartment item = list.get(position);
        holder.title.setText(item.title);
        holder.price.setText("$" + item.price);
        holder.description.setText(item.description != null ? item.description : "");
        holder.date.setText(item.date != null ? item.date : "");

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(item);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(item);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(List<Apartment> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, description, date;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            price = v.findViewById(R.id.tvPrice);
            description = v.findViewById(R.id.tvDescription);
            date = v.findViewById(R.id.tvDate);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
