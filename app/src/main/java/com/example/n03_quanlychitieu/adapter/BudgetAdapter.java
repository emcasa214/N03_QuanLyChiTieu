package com.example.n03_quanlychitieu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.dao.CategoryDAO;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.model.Budgets;
import com.example.n03_quanlychitieu.model.Categories;
import com.example.n03_quanlychitieu.ui.sign.SetBudgets;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    private Context context;
    private List<Budgets> budgetsList;
    private OnBudgetClickListener listener;
    private NumberFormat currencyFormat;
    private CategoryDAO categoryDAO;

    public interface OnBudgetClickListener {
        void onBudgetClick(Budgets budget);

        void onBudgetLongClick(Budgets budget);
    }

    public BudgetAdapter(Context context, List<Budgets> budgetsList, CategoryDAO categoryDAO, OnBudgetClickListener listener) {
        this.context = context;
        this.budgetsList = budgetsList;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.categoryDAO = categoryDAO;
    }

    //    public void updateData(List<Budgets> newBudgets) {
//        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new BudgetDiffCallback(this.budgetsList, newBudgets));
//        this.budgetsList.clear();
//        this.budgetsList.addAll(newBudgets);
//        diffResult.dispatchUpdatesTo(this);
//    } // đang bị lỗi ở hàm update

//    public void updateData(List<Budgets> newBudgets) {
//        this.budgetsList.clear();
//        this.budgetsList.addAll(newBudgets);
//        notifyDataSetChanged(); // Cập nhật toàn bộ RecyclerView
//    }

    public void updateData(List<Budgets> newBudgets) {
        // Tạo bản sao để tránh tham chiếu
        List<Budgets> updatedList = new ArrayList<>(newBudgets);

        // So sánh và cập nhật từng item
        for (int i = 0; i < updatedList.size(); i++) {
            if (i < budgetsList.size()) {
                if (!budgetsList.get(i).equals(updatedList.get(i))) {
                    budgetsList.set(i, updatedList.get(i));
                    notifyItemChanged(i); // Chỉ cập nhật item thay đổi
                }
            } else {
                budgetsList.add(updatedList.get(i));
                notifyItemInserted(i); // Thông báo item mới
            }
        }

        // Xóa các item thừa (nếu có)
        if (budgetsList.size() > updatedList.size()) {
            for (int i = budgetsList.size() - 1; i >= updatedList.size(); i--) {
                budgetsList.remove(i);
                notifyItemRemoved(i); // Thông báo item bị xóa
            }
        }
    }

    private static class BudgetDiffCallback extends DiffUtil.Callback {
        private final List<Budgets> oldList;
        private final List<Budgets> newList;

        public BudgetDiffCallback(List<Budgets> oldList, List<Budgets> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getBudget_id().equals(newList.get(newItemPosition).getBudget_id());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budgets budget = budgetsList.get(position);

        Categories category = categoryDAO.getCategoryById(budget.getCategory_id());

        if (budget == null) return;

        // Set category icon
        int iconResId = context.getResources().getIdentifier(category.getIcon(), "drawable", context.getPackageName());
        holder.ivCategoryIcon.setImageResource(getCategoryIcon(iconResId));

        // Set category name
        holder.tvCategoryName.setText(getCategoryName(category.getName()));

        // Format amount
        holder.tvAmount.setText(currencyFormat.format(budget.getAmount()));

        // Format date range
        String dateRange = formatDate(budget.getStart_date()) + " - " + formatDate(budget.getEnd_date());
        holder.tvTimeRange.setText(dateRange);

        // Set description
        holder.tvDescription.setText(budget.getDescription() != null ? budget.getDescription() : "");

        // Calculate progress
        double spentAmount = 30000; // Lấy từ DAO
        double progress = Math.min((spentAmount / budget.getAmount()) * 100, 100);

        holder.progressBar.setProgress((int) progress);
        holder.tvUsedAmount.setText("Đã dùng: " + currencyFormat.format(spentAmount));
        holder.tvRemainingAmount.setText("Còn lại: " + currencyFormat.format(Math.max(budget.getAmount() - spentAmount, 0)));

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onBudgetClick(budget);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onBudgetLongClick(budget);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return budgetsList.size();
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategoryName;
        TextView tvAmount;
        TextView tvTimeRange;
        TextView tvDescription;
        TextView tvUsedAmount;
        TextView tvRemainingAmount;
        LinearProgressIndicator progressBar;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvTimeRange = itemView.findViewById(R.id.tv_time_range);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvUsedAmount = itemView.findViewById(R.id.tv_used_amount);
            tvRemainingAmount = itemView.findViewById(R.id.tv_remaining_amount);
            progressBar = itemView.findViewById(R.id.progress_bar_bu);
        }
    }

    // Helper methods
    private int getCategoryIcon(int categoryIcon) {
        // Return appropriate icon based on categoryId
        return categoryIcon;
    }

    private String getCategoryName(String categoryName) {
        // Return category name based on categoryId
        return categoryName;
    }

    private String formatDate(String dateString) {
        // Format date from yyyy-MM-dd to dd/MM/yyyy
        return dateString;
    }
}
