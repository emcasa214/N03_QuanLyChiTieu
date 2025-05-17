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
    private TextInputLayout tilAmount, tilSource, tilDate, tilCategory;
    private Button btnUpdate, btnCancel;
    private Spinner spinnerCategory;
    private DatabaseHelper databaseHelper;
    private String userId, incomeId;
    private Map<String, String> categoryNameToIdMap; // Ánh xạ giữa tên danh mục và categoryId

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_income);

        databaseHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbarUpdate);
        setSupportActionBar(toolbar);

        tilAmount = findViewById(R.id.tilAmount);
        tilSource = findViewById(R.id.tilSource);
        tilDate = findViewById(R.id.tilDate);
        tilCategory = findViewById(R.id.tilCategory);
        etAmount = findViewById(R.id.etAmount);
        etSource = findViewById(R.id.etSource);
        etDate = findViewById(R.id.etDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);

        userId = getIntent().getStringExtra("userId");
        incomeId = getIntent().getStringExtra("incomeId");

        if (userId == null || incomeId == null) {
            Toast.makeText(this, "User ID or Income ID is missing", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        categoryNameToIdMap = new HashMap<>();

        etAmount.setText(getIntent().getStringExtra("amount"));
        etSource.setText(getIntent().getStringExtra("source"));
        etDate.setText(getIntent().getStringExtra("date"));

        loadCategories(getIntent().getStringExtra("categoryId"));

        etDate.setOnClickListener(v -> showDatePicker());

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        btnUpdate.setOnClickListener(v -> {
            tilAmount.setError(null);
            tilSource.setError(null);
            tilDate.setError(null);
            tilCategory.setError(null);

            String amount = etAmount.getText().toString().trim();
            String source = etSource.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String selectedCategoryName = spinnerCategory.getSelectedItem() != null ?
                    spinnerCategory.getSelectedItem().toString() : null;
            String categoryId = selectedCategoryName != null ? categoryNameToIdMap.get(selectedCategoryName) : null;

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
                tilCategory.setError("Vui lòng chọn danh mục");
                return;
            }

            try {
                double amountValue = Double.parseDouble(amount);
                if (amountValue <= 0) {
                    tilAmount.setError("Số tiền phải lớn hơn 0");
                    return;
                }

                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                inputFormat.setLenient(false);
                java.util.Date parsedDate = inputFormat.parse(date);
                String formattedDate = outputFormat.format(parsedDate);

                btnUpdate.setEnabled(false);
                btnCancel.setEnabled(false);

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
                                setResult(RESULT_OK);
                                Toast.makeText(UpdateIncomeActivity.this, "Cập nhật thu nhập thành công", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(String errorMessage) {
                                tilAmount.setError("Lỗi: " + errorMessage);
                                btnUpdate.setEnabled(true);
                                btnCancel.setEnabled(true);
                                Log.e(TAG, "Error updating income: " + errorMessage);
                            }
                        }
                );
            } catch (NumberFormatException e) {
                tilAmount.setError("Số tiền phải là một số hợp lệ");
                Log.e(TAG, "NumberFormatException: " + e.getMessage());
            } catch (java.text.ParseException e) {
                tilDate.setError("Định dạng ngày không hợp lệ (dd/MM/yyyy)");
                Log.e(TAG, "ParseException: " + e.getMessage());
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

    private void loadCategories(String selectedCategoryId) {
        List<String> categoryNames = new ArrayList<>();
        int selectedPosition = 0;
        try {
            List<DatabaseHelper.Category> categoryList = databaseHelper.getCategoriesByUserIdAndType(userId, "income");
            if (categoryList.isEmpty()) {
                Toast.makeText(this, "Không có danh mục thu nhập.", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
            for (int i = 0; i < categoryList.size(); i++) {
                DatabaseHelper.Category cat = categoryList.get(i);
                categoryNames.add(cat.getName());
                categoryNameToIdMap.put(cat.getName(), cat.getCategoryId());
                if (cat.getCategoryId().equals(selectedCategoryId)) {
                    selectedPosition = i;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading categories: " + e.getMessage());
            Toast.makeText(this, "Lỗi tải danh mục. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setSelection(selectedPosition);
    }
}