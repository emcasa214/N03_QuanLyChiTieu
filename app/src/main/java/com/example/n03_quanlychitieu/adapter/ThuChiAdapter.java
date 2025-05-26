package com.example.n03_quanlychitieu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.n03_quanlychitieu.R;

import java.util.List;

public class ThuChiAdapter extends RecyclerView.Adapter<ThuChiAdapter.ViewHolder> {
    private List<ThuChiItem> items;

    public ThuChiAdapter(List<ThuChiItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_thuchi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThuChiItem item = items.get(position);
        holder.tvDate.setText(item.getDate());
        holder.tvDescription.setText(item.getDescription());
        holder.tvAmount.setText(item.getAmount());

        // Phân biệt Thu và Chi bằng màu sắc
        if (item.getType().equals("income")) {
            holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDescription, tvAmount;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }

    public static class ThuChiItem {
        private String date;
        private String description;
        private String amount;
        private String type; // "income" hoặc "expense"

        public ThuChiItem(String date, String description, String amount, String type) {
            this.date = date;
            this.description = description;
            this.amount = amount;
            this.type = type;
        }

        public String getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }

        public String getAmount() {
            return amount;
        }

        public String getType() {
            return type;
        }
    }
}