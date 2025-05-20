package com.example.n03_quanlychitieu.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.model.Categories;
import com.example.n03_quanlychitieu.model.Expenses;
import com.example.n03_quanlychitieu.model.Incomes;


import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_INCOME = 1;
    private static final int TYPE_EXPENSE = 2;
    private static final int TYPE_CATEGORY_STATS = 3;
    private static final int TYPE_TIME_STATS = 4;

    private Context context;
    private List<Object> items;
    private int displayType;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat statsDateFormat;

    public ReportAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        this.statsDateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
    }

    // Các phương thức setData giữ nguyên như cũ
    public void setIncomes(List<Incomes> incomes) {
        this.items = new ArrayList<>();
        this.items.addAll(incomes);
        this.displayType = TYPE_INCOME; // cái này ang lỗi
        notifyDataSetChanged();
    }

    public void setExpenses(List<Expenses> expenses) {
        this.items.clear();
        this.items.addAll(expenses);
        this.displayType = TYPE_EXPENSE;
        notifyDataSetChanged();
    }

    public void setCategoryStats(Map<Categories, Double> stats) {
        this.items.clear();
        this.items.addAll(stats.entrySet());
        this.displayType = TYPE_CATEGORY_STATS;
        notifyDataSetChanged();
    }

    public void setTimeStats(Map<String, Double> stats) {
        this.items.clear();
        this.items.addAll(stats.entrySet());
        this.displayType = TYPE_TIME_STATS;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return displayType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ReportViewHolder(view);
    }

    // cái này đang sửa
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ReportViewHolder viewHolder = (ReportViewHolder) holder;
        Object item = items.get(position);

        try {
            switch (displayType) {
                case TYPE_INCOME:
                    if (item instanceof Incomes) {
                        viewHolder.bindIncome((Incomes) item);
                    }
                    break;
                case TYPE_EXPENSE:
                    if (item instanceof Expenses) {
                        viewHolder.bindExpense((Expenses) item);
                    }
                    break;
                case TYPE_CATEGORY_STATS:
                    if (item instanceof Map.Entry) {
                        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) item;
                        if (entry.getKey() instanceof Categories && entry.getValue() instanceof Double) {
                            viewHolder.bindCategoryStats((Categories) entry.getKey(), (Double) entry.getValue());
                        }
                    }
                    break;
                case TYPE_TIME_STATS:
                    if (item instanceof Map.Entry) {
                        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) item;
                        if (entry.getKey() instanceof String && entry.getValue() instanceof Double) {
                            viewHolder.bindTimeStats((String) entry.getKey(), (Double) entry.getValue());
                        }
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown display type: " + displayType);
            }
        } catch (ClassCastException e) {
            Log.e("ReportAdapter", "Type casting error", e);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvTitle, tvSubtitle, tvAmount, tvTime;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        public void bindIncome(Incomes income) {
            tvTitle.setText(income.getDescription());
            tvSubtitle.setText(income.getDescription());
            tvAmount.setTextColor(ContextCompat.getColor(context, R.color.green));
            tvAmount.setText("+" + currencyFormat.format(income.getAmount()));
            tvTime.setText(dateFormat.format(new Date(Long.parseLong(income.getCreate_at()))));
            // Set icon từ category
            ivCategoryIcon.setImageResource(getCategoryIcon(income.getCategory_id()));
        }

        public void bindExpense(Expenses expense) {
            tvTitle.setText(expense.getDescription());
            tvSubtitle.setText(expense.getDescription());
            tvAmount.setTextColor(ContextCompat.getColor(context, R.color.red));
            tvAmount.setText("-" + currencyFormat.format(expense.getAmount()));
            tvTime.setText(dateFormat.format(new Date(Long.parseLong(expense.getCreate_at()))));
            // Set icon từ category
            ivCategoryIcon.setImageResource(getCategoryIcon(expense.getCategory_id()));
        }

        public void bindCategoryStats(Categories category, Double amount) {
            tvTitle.setText(category.getName());
            tvSubtitle.setText("Tổng: " + currencyFormat.format(amount));
            tvAmount.setVisibility(View.GONE);
            tvTime.setVisibility(View.GONE);
            // Set icon từ category
            ivCategoryIcon.setImageResource(getCategoryIcon(category.getCategory_id()));
        }

        public void bindTimeStats(String timePeriod, Double amount) {
            tvTitle.setText("Thống kê ngày " + timePeriod);
            tvSubtitle.setText("Tổng: " + currencyFormat.format(amount));
            tvAmount.setVisibility(View.GONE);
            tvTime.setVisibility(View.GONE);
            // Set icon chung cho thống kê thời gian
            ivCategoryIcon.setImageResource(R.drawable.ic_calendar);
        }
    }

    private int getCategoryIcon(String categoryId) {
        // Triển khai logic lấy icon từ categoryId
        // Ví dụ đơn giản:
        return R.drawable.ic_default_category;
    }
}