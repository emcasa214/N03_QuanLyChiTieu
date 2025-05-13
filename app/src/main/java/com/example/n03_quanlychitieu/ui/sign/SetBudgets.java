package com.example.n03_quanlychitieu.ui.sign;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.n03_quanlychitieu.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class SetBudgets extends AppCompatActivity {
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        // Thiết lập BottomSheetBehavior
        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
//        if (bottomSheet != null) {
//            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
//            // Các cài đặt behavior khác...
//        }
//        // Cấu hình behavior
//        bottomSheetBehavior.setHideable(false); // Không cho ẩn hoàn toaàn
//        bottomSheetBehavior.setPeekHeight(120); // Chiều cao tối thiểu khi thu gọn (120dp)
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // Trang thái ban đầu
//
//        // Thiết lập callback
//        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                switch (newState) {
//                    case BottomSheetBehavior.STATE_COLLAPSED:
//                        // Trạng thái thu goọn
//                        break;
//                    case BottomSheetBehavior.STATE_EXPANDED:
//                        // Trạng thái mở rộng
//                        break;
//                    case BottomSheetBehavior.STATE_DRAGGING:
//                    case BottomSheetBehavior.STATE_SETTLING:
//                        // Đang kéo hoặc đang ổn định
//                        break;
//                }
//            }
//
//            @Override
//            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                // Xử lý hiệu ứng trong quá trình kéo
//            }
//        });

        // Thiết lập RecyclerView

        // Xử lý sự kiện nút lưu
//        findViewById(R.id.btn_save).setOnClickListener(v ->);
    }

//    private void setupRecyclerView() {
//        RecyclerView recyclerView = findViewById(R.id.rv_budgets);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        // Giả sử bạn có BudgetAdapter
//        BudgetAdapter adapter = new BudgetAdapter(getBudgets());
//        recyclerView.setAdapter(adapter);
//
//        // Hiển thị empty view nếu không có dữ liệu
//        updateEmptyView();
//    }

    //    private void updateEmptyView() {
//        boolean isEmpty = getBudgets().isEmpty();
//        findViewById(R.id.empty_view).setVisibility(isEmpty ? View.VISIBLE : View.GONE);
//        findViewById(R.id.rv_budgets).setVisibility(isEmpty ? View.GONE : View.VISIBLE);
//    }
//
//    private List<Budget> getBudgets() {
//        // Lấy danh sách giới hạn từ database
//        return budgetDao.getBudgetsByUser(currentUserId);
//    }
//
//    private void saveBudget() {
//        // Xử lý lưu giới hạn mới
//        // Sau khi lưu thành công:
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//        updateRecyclerView();
//    }
//    private void updateRecyclerView() {
//        BudgetAdapter adapter = (BudgetAdapter) ((RecyclerView) findViewById(R.id.rv_budgets)).getAdapter();
//        adapter.updateData(getBudgets());
//        updateEmptyView();
//    }
}
