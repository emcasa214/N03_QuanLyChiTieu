package com.example.n03_quanlychitieu.ui.sign;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.adapter.BudgetAdapter;
import com.example.n03_quanlychitieu.dao.BudgetDAO;
import com.example.n03_quanlychitieu.dao.CategoryDAO;
import com.example.n03_quanlychitieu.dao.NotificationDAO;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.model.Budgets;
import com.example.n03_quanlychitieu.model.Categories;
import com.example.n03_quanlychitieu.model.Notifications;
import com.example.n03_quanlychitieu.utils.AuthenticationManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class SetBudgets extends AppCompatActivity implements BudgetAdapter.OnBudgetClickListener {

    private AuthenticationManager authManager;
    private BudgetDAO budgetDAO;
    private CategoryDAO categoryDAO;
    private String currentUserId;
    private TextInputEditText etAmount, etStartDate, etEndDate, etDescription;
    private AutoCompleteTextView actvCategory;
    private MaterialButton btnSave;
    private RecyclerView rvBudgets;
    private View emptyView;
    private LinearLayout bottomSheet;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

    private BudgetAdapter budgetAdapter;
    private List<Budgets> budgetsList = new ArrayList<>();
    private List<Categories> categories = new ArrayList<>();
    private final Calendar calendar = Calendar.getInstance();
    private ImageButton btn_back;
    private NotificationDAO notificationDAO;
    private DatabaseHelper dbHelper;
    private Notifications tb;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        // Initialize authentication
        authManager = AuthenticationManager.getInstance(this);
        if (!authManager.isUserLoggedIn()) {
            finish();
            return;
        }
        currentUserId = authManager.getCurrentUser().getUser_id();

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        budgetDAO = new BudgetDAO(dbHelper.getWritableDatabase());
        categoryDAO = new CategoryDAO(dbHelper.getWritableDatabase());
        notificationDAO = new NotificationDAO(dbHelper.getWritableDatabase());
        // Initialize views
        initViews();

        // Setup bottom sheet behavior
        setupBottomSheet();

        // Setup category dropdown
        setupCategoryDropdown();

        // Setup date pickers
        setupDatePickers();

        // Setup recycler view
        setupRecyclerView();

        // Load budgets
        loadBudgets();

        // Set save button click listener
        btnSave.setOnClickListener(v -> saveBudget());

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initViews() {
        actvCategory = findViewById(R.id.actv_category);
        etAmount = findViewById(R.id.et_amount);
        etStartDate = findViewById(R.id.et_start_date);
        etEndDate = findViewById(R.id.et_end_date);
        etDescription = findViewById(R.id.et_description);
        btnSave = findViewById(R.id.btn_save);
        rvBudgets = findViewById(R.id.rv_budgets);
        emptyView = findViewById(R.id.empty_view);
        bottomSheet = findViewById(R.id.bottom_sheet);
        btn_back = findViewById(R.id.btn_back_budget);

        notificationDAO = new NotificationDAO(dbHelper.getWritableDatabase());
        budgetDAO = new BudgetDAO(dbHelper.getReadableDatabase());
    }

    private void setupBottomSheet() {
        // Get the BottomSheetBehavior
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Set the initial state
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Set the peek height (the height of the bottom sheet when collapsed)
        bottomSheetBehavior.setPeekHeight(200);

        // Make sure the bottom sheet is not hideable
        bottomSheetBehavior.setHideable(false);

        // Set bottom sheet callback
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        // Fully expanded
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        // Collapsed to peek height
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        // User is dragging
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        // Settling to final position
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        // Hidden (shouldn't happen as we set hideable to false)
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Called when the bottom sheet is sliding
            }
        });
    }

    private void setupCategoryDropdown() {
        categories = categoryDAO.getAllCategories(currentUserId);
        List<String> categoryNames = new ArrayList<>();
        for (Categories category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categoryNames
        );
        actvCategory.setAdapter(categoryAdapter);
    }

    private void setupDatePickers() {
        DatePickerDialog.OnDateSetListener startDateListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            etStartDate.setText(dateFormat.format(calendar.getTime()));
        };

        DatePickerDialog.OnDateSetListener endDateListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            etEndDate.setText(dateFormat.format(calendar.getTime()));
        };

        etStartDate.setOnClickListener(v -> new DatePickerDialog(
                SetBudgets.this,
                startDateListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show());

        etEndDate.setOnClickListener(v -> new DatePickerDialog(
                SetBudgets.this,
                endDateListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show());
    }

    private void setupRecyclerView() {
        budgetAdapter = new BudgetAdapter(this, budgetsList, categoryDAO, budgetDAO, this);
        rvBudgets.setLayoutManager(new LinearLayoutManager(this));
        rvBudgets.setAdapter(budgetAdapter);
    }

    private void loadBudgets() {
        List<Budgets> newBudgets = budgetDAO.getBudgetsByUser(currentUserId);
        budgetAdapter.updateData(newBudgets);

        // Kiểm tra và gửi thông báo cho từng ngân sách
        for (Budgets budget : newBudgets) {
            checkBudgetStatusAndNotify(budget);
        }

        if (newBudgets.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            rvBudgets.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            rvBudgets.setVisibility(View.VISIBLE);
        }
    }

    private void checkBudgetStatusAndNotify(Budgets budget) {
        double spent = budgetDAO.getTotalSpentForBudget(budget.getBudget_id());
        double budgetAmount = budget.getAmount();
        String budgetDesc = budget.getDescription();

        if (spent > budgetAmount) {
            // Vượt quá ngân sách
            Notifications notification = new Notifications(
                    UUID.randomUUID().toString(),
                    "Bạn đã chi tiêu vượt quá ngân sách " + budgetDesc,
                    false, null, "warn", currentUserId
            );
            notificationDAO.insert(notification);
            long result = budgetDAO.delete(budget.getBudget_id());
            if (result != -1) {
                Notifications deleteNoti = new Notifications(
                        UUID.randomUUID().toString(),
                        "Đã tự động xóa ngân sách " + budgetDesc + " do vượt quá hạn mức",
                        false, null, "info", currentUserId
                );
                notificationDAO.insert(deleteNoti);
                loadBudgets(); // Tải lại danh sách
            }
        } else if (spent >= budgetAmount * 0.9) { // 90% ngân sách
            // Sắp hết ngân sách
            Notifications notification = new Notifications(
                    UUID.randomUUID().toString(),
                    "Bạn đã chi tiêu gần hết ngân sách " + budgetDesc,
                    false, null, "warn", currentUserId
            );
            notificationDAO.insert(notification);
        } else if (spent == budgetAmount) {
            // Hết ngân sách
            Notifications notification = new Notifications(
                    UUID.randomUUID().toString(),
                    "Bạn đã chi tiêu hết ngân sách " + budgetDesc,
                    false, null, "warn", currentUserId
            );
            notificationDAO.insert(notification);
        }
    }

    private void saveBudget() {
        // Validate inputs
        String categoryName = actvCategory.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String description = etDescription.getText().toString().trim();


        if (categoryName.isEmpty()) {
            actvCategory.setError("Vui lòng chọn danh mục");
            return;
        }

        if (amountStr.isEmpty()) {
            etAmount.setError("Vui lòng nhập số tiền");
            return;
        }

        if (startDate.isEmpty()) {
            etStartDate.setError("Vui lòng chọn ngày bắt đầu");
            return;
        }

        if (endDate.isEmpty()) {
            etEndDate.setError("Vui lòng chọn ngày kết thúc");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Số tiền không hợp lệ");
            return;
        }

        // Find category ID
        String categoryId = null;
        for (Categories category : categories) {
            if (category.getName().equals(categoryName)) {
                categoryId = category.getCategory_id();
                break;
            }
        }

        if (categoryId == null) {
            Toast.makeText(this, "Danh mục không tồn tại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new budget
        Budgets newBudget = new Budgets();
        newBudget.setBudget_id(generateBudgetId());
        newBudget.setAmount(amount);
        newBudget.setStart_date(startDate);
        newBudget.setEnd_date(endDate);
        newBudget.setDescription(description);
        newBudget.setUser_id(currentUserId);
        newBudget.setCategory_id(categoryId);

        // Save to database
        long result = budgetDAO.insert(newBudget);
        if (result != -1) {
            Toast.makeText(this, "Đã lưu giới hạn chi tiêu", Toast.LENGTH_SHORT).show();
            checkBudgetStatusAndNotify(newBudget);

            clearForm();

            // Expand the bottom sheet to show the new budget
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            budgetsList.add(0, newBudget);
            budgetAdapter.notifyItemInserted(0);
            rvBudgets.smoothScrollToPosition(0);
        } else {
            Toast.makeText(this, "Lỗi khi lưu giới hạn", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateBudgetId() {
        return "BUD_" + System.currentTimeMillis();
    }

    private void clearForm() {
        actvCategory.setText("");
        etAmount.setText("");
        etStartDate.setText("");
        etEndDate.setText("");
        etDescription.setText("");
    }

    @Override
    public void onBudgetClick(Budgets budget) {
        // Handle budget click (e.g., show edit dialog)
        Toast.makeText(this, "Clicked: " + budget.getDescription(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBudgetLongClick(Budgets budget) {
        // Handle long click (e.g., delete budget)
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa giới hạn " + budget.getDescription() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Thực hiện xóa khi người dùng xác nhận
                    long result = budgetDAO.delete(budget.getBudget_id());
                    if (result != -1) {
                        // Xóa thành công - cập nhật UI
                        int position = budgetsList.indexOf(budget);
                        if (position != -1) {
                            budgetsList.remove(position);
                            budgetAdapter.notifyItemRemoved(position);

                            // Hiển thị thông báo thành công
                            Toast.makeText(this, "Đã xóa giới hạn " + budget.getDescription(),
                                    Toast.LENGTH_SHORT).show();

                            // Kiểm tra nếu danh sách trống thì ẩn RecyclerView
                            if (budgetsList.isEmpty()) {
                                rvBudgets.setVisibility(View.GONE);
                                emptyView.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        // Xóa thất bại
                        Toast.makeText(this, "Lỗi khi xóa giới hạn", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}