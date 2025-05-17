package com.example.n03_quanlychitieu.ui.income;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UpdateIncomeActivity extends AppCompatActivity {
    private static final String TAG = "UpdateIncomeActivity";
    private TextInputEditText etAmount, etSource, etDate;
    private TextInputLayout tilAmount, tilSource, tilDate;
    private Button btnUpdate, btnCancel;
    private Spinner spinnerCategory;
    private DatabaseHelper databaseHelper;
    private String userId, incomeId, initialCategoryId;
    private Map<String, String> categoryNameToIdMap;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_income);

        databaseHelper = new DatabaseHelper(this);

        // Initialize UI components
        Toolbar toolbar = findViewById(R.id.toolbarUpdate);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilAmount = findViewById(R.id.tilAmount);
        tilSource = findViewById(R.id.tilSource);
        tilDate = findViewById(R.id.tilDate);
        etAmount = findViewById(R.id.etAmount);
        etSource = findViewById(R.id.etSource);
        etDate = findViewById(R.id.etDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);

        // Get Intent data
        userId = getIntent().getStringExtra("userId");
        incomeId = getIntent().getStringExtra("incomeId");
        initialCategoryId = getIntent().getStringExtra("categoryId");
        Log.d(TAG, "Intent data - userId: " + userId + ", incomeId: " + incomeId + ", categoryId: " + initialCategoryId);

        if (userId == null || userId.isEmpty() || incomeId == null || incomeId.isEmpty()) {
            Log.e(TAG, "Missing userId or incomeId");
            Toast.makeText(this, "Thiếu User ID hoặc Income ID", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        categoryNameToIdMap = new HashMap<>();

        // Populate fields with Intent data
        etAmount.setText(getIntent().getStringExtra("amount"));
        etSource.setText(getIntent().getStringExtra("source"));
        etDate.setText(getIntent().getStringExtra("date"));

        // Load categories into spinner
        if (!loadCategories(initialCategoryId)) {
            Log.e(TAG, "Failed to load categories");
            Toast.makeText(this, "Không thể tải danh mục thu nhập", Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        // Set up date picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Cancel button
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // Update button
        btnUpdate.setOnClickListener(v -> {
            // Clear previous errors
            tilAmount.setError(null);
            tilSource.setError(null);
            tilDate.setError(null);

            String amount = etAmount.getText().toString().trim();
            String source = etSource.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String selectedCategoryName = spinnerCategory.getSelectedItem() != null ?
                    spinnerCategory.getSelectedItem().toString() : null;
            String categoryId = selectedCategoryName != null ? categoryNameToIdMap.get(selectedCategoryName) : null;

            // Log input data
            Log.d(TAG, "Update inputs - amount: " + amount + ", source: " + source + ", date: " + date + ", categoryId: " + categoryId);

            // Validate inputs
            if (amount.isEmpty()) {
                tilAmount.setError("Vui lòng nhập số tiền");
                return;
            }
            if (source.isEmpty()) {
                tilSource.setError("Vui lòng nhập nguồn thu nhập");
                return;
            }
            if (date.isEmpty()) {
                tilDate.setError("Vui lòng chọn ngày");
                return;
            }
            if (categoryId == null || categoryId.isEmpty()) {
                Log.e(TAG, "Category ID is null or empty. Map: " + categoryNameToIdMap);
                Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amountValue = Double.parseDouble(amount);
                if (amountValue <= 0) {
                    tilAmount.setError("Số tiền phải lớn hơn 0");
                    return;
                }

                // Parse and format date
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                inputFormat.setLenient(false);
                java.util.Date parsedDate = inputFormat.parse(date);
                String formattedDate = outputFormat.format(parsedDate);

                btnUpdate.setEnabled(false);
                btnCancel.setEnabled(false);

                // Perform update
                databaseHelper.updateIncomeAsync(
                        incomeId,
                        userId,
                        String.valueOf(amountValue),
                        categoryId,
                        source,
                        formattedDate,
                        new DatabaseHelper.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(UpdateIncomeActivity.this, "Cập nhật thu nhập thành công", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.e(TAG, "Update failed: " + errorMessage);
                                Toast.makeText(UpdateIncomeActivity.this, "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
                                btnUpdate.setEnabled(true);
                                btnCancel.setEnabled(true);
                            }
                        }
                );
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid amount format: " + e.getMessage());
                tilAmount.setError("Số tiền phải là một số hợp lệ");
            } catch (java.text.ParseException e) {
                Log.e(TAG, "Invalid date format: " + e.getMessage());
                tilDate.setError("Định dạng ngày không hợp lệ (dd/MM/yyyy)");
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etDate.setText(sdf.format(selectedDate.getTime()));
                },
                year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private boolean loadCategories(String selectedCategoryId) {
        List<String> categoryNames = new ArrayList<>();
        int selectedPosition = 0;
        try {
            List<DatabaseHelper.Category> categoryList = databaseHelper.getCategoriesByUserIdAndType(userId, "income");
            if (categoryList.isEmpty()) {
                Log.e(TAG, "No income categories found for userId: " + userId);
                return false;
            }
            for (int i = 0; i < categoryList.size(); i++) {
                DatabaseHelper.Category cat = categoryList.get(i);
                categoryNames.add(cat.getName());
                categoryNameToIdMap.put(cat.getName(), cat.getCategoryId());
                if (cat.getCategoryId().equals(selectedCategoryId)) {
                    selectedPosition = i;
                }
            }
            Log.d(TAG, "Categories loaded: " + categoryNameToIdMap);
        } catch (Exception e) {
            Log.e(TAG, "Error loading categories: " + e.getMessage());
            return false;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setSelection(selectedPosition);
        return true;
    }
}