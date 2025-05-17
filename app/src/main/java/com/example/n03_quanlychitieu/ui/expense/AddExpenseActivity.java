package com.example.n03_quanlychitieu.ui.expense;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.model.Budgets;
import com.example.n03_quanlychitieu.model.Categories;
import com.example.n03_quanlychitieu.model.Users;
import com.example.n03_quanlychitieu.utils.AuthenticationManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {
    private static final String TAG = "AddExpenseActivity";
    private TextInputEditText etDate, etAmount, etDescription;
    private TextInputLayout tilDate, tilAmount, tilDescription;
    private Button btnSave;
    private ImageButton btnBack;
    private Spinner spinnerCategory, spinnerBudget;
    private DatabaseHelper databaseHelper;

    Users currentUser;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);
        Log.d(TAG, "onCreate called");

        databaseHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tilDate = findViewById(R.id.tilDate);
        tilAmount = findViewById(R.id.tilAmount);
        tilDescription = findViewById(R.id.tilDescription);
        etDate = findViewById(R.id.etDate);
        etAmount = findViewById(R.id.etAmount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerBudget = findViewById(R.id.spinnerBudget);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        currentUser = AuthenticationManager.getInstance(this).getCurrentUser();
        userId = currentUser.getUser_id();

        loadBudgets();
        loadCategories();

//        if (savedInstanceState != null) {
//            etDate.setText(savedInstanceState.getString("date"));
//            etAmount.setText(savedInstanceState.getString("amount"));
//            etCategory.setText(savedInstanceState.getString("category"));
//            etDescription.setText(savedInstanceState.getString("description"));
//            Log.d(TAG, "Restored state: date=" + etDate.getText().toString() + ", amount=" + etAmount.getText().toString() +
//                    ", category=" + etCategory.getText().toString() + ", description=" + etDescription.getText().toString());
//        }

        etDate.setOnClickListener(v -> showDatePicker());
        Log.d(TAG, "Date picker listener set");

        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            setResult(RESULT_CANCELED);
            finish();
        });

        btnSave.setOnClickListener(v -> {
            Log.d(TAG, "Save button clicked");

            tilDate.setError(null);
            tilAmount.setError(null);
            tilDescription.setError(null);

            String date = etDate.getText().toString().trim();
            String amount = etAmount.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            // Lấy đối tượng Categories từ Spinner
            Categories selectedCategory = (Categories) spinnerCategory.getSelectedItem();
            String categoryId = selectedCategory != null ? selectedCategory.getCategory_id() : null;

            // Lấy đối tượng Budgets từ Spinner
            Budgets selectedBudget = (Budgets) spinnerBudget.getSelectedItem();
            String budgetId = selectedBudget != null ? selectedBudget.getBudget_id() : null;
            String categoryName = selectedCategory != null ? selectedCategory.getName() : "Không xác định";

            Log.d(TAG, "Input values: date=" + date + ", amount=" + amount + ", category=" + selectedCategory.getName() + ", description=" + description + ", budget = " + selectedBudget.getBudget_id() );

            if (date.isEmpty()) {
                Log.w(TAG, "Validation failed: Date is empty");
                tilDate.setError("Vui lòng chọn ngày");
                return;
            }
            if (amount.isEmpty()) {
                Log.w(TAG, "Validation failed: Amount is empty");
                tilAmount.setError("Vui lòng nhập số tiền");
                return;
            }
            if (categoryId == null || categoryId.equals("none")) {
                Toast.makeText(this, "Vui lòng chọn danh mục hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (description.isEmpty()) {
                Log.w(TAG, "Validation failed: Description is empty");
                tilDescription.setError("Vui lòng nhập mô tả");
                return;
            }
            // Xử lý budgetId
            String finalBudgetId = budgetId != null && budgetId.equals("none") ? null : budgetId;

            try {
                double amountValue = Double.parseDouble(amount);
                if (amountValue <= 0) {
                    tilAmount.setError("Số tiền phải lớn hơn 0");
                    return;
                }

                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                inputFormat.setLenient(false);
                java.util.Date parsedDate = inputFormat.parse(date);
                String formattedDate = outputFormat.format(parsedDate);

                btnBack.setEnabled(false);
                btnSave.setEnabled(false);

                databaseHelper.addExpenseAsync(
                        userId,
                        String.valueOf(amountValue),
                        categoryId,
                        description,
                        formattedDate,
                        finalBudgetId,
                        new DatabaseHelper.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "addExpenseAsync: Success");
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("date", date);
                                resultIntent.putExtra("amount", amount);
                                resultIntent.putExtra("category", categoryName);
                                resultIntent.putExtra("description", description);
                                resultIntent.putExtra("budget", finalBudgetId);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.e(TAG, "addExpenseAsync: Error - " + errorMessage);
                                tilAmount.setError("Lỗi: " + errorMessage);
                                btnBack.setEnabled(true);
                                btnSave.setEnabled(true);
                            }
                        }
                );
            } catch (NumberFormatException e) {
                tilAmount.setError("Số tiền phải là một số hợp lệ");
            } catch (java.text.ParseException e) {
                tilDate.setError("Định dạng ngày không hợp lệ (dd/MM/yyyy)");
            } catch (Exception e) {
                tilAmount.setError("Lỗi không xác định: " + e.getMessage());
                btnBack.setEnabled(true);
                btnSave.setEnabled(true);
            }
        });
        Log.d(TAG, "Save button listener set");
    }
    private void loadBudgets(){
        List<Budgets> budgets = databaseHelper.getAllBudgets(userId);
        if (budgets.isEmpty()) {
            budgets.add(new Budgets("none", "Không có ngân sách"));
        } else {
            budgets.add(0, new Budgets("none", "Chọn ngân sách"));
        }
        ArrayAdapter<Budgets> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, budgets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBudget.setAdapter(adapter);
    }

    private void loadCategories() {
        List<Categories> categoryList = databaseHelper.getAllCategories(userId, "expense");

        if (categoryList.isEmpty()) {
            categoryList.add(new Categories("none", "Không có danh mục"));
        } else {
            categoryList.add(0, new Categories("none", "Chọn danh mục"));
        }

        ArrayAdapter<Categories> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }



//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putString("date", etDate.getText().toString().trim());
//        outState.putString("amount", etAmount.getText().toString().trim());
//        outState.putString("category", etCategory.getText().toString().trim());
//        outState.putString("description", etDescription.getText().toString().trim());
//    }

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
}