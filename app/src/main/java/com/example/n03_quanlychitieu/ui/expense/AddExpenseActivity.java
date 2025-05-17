package com.example.n03_quanlychitieu.ui.expense;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {
    private static final String TAG = "AddExpenseActivity";
    private TextInputEditText etDate, etAmount, etCategory, etDescription;
    private TextInputLayout tilDate, tilAmount, tilCategory, tilDescription;
    private Button btnSave;
    private ImageButton btnBack;
    private DatabaseHelper databaseHelper;

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
        tilCategory = findViewById(R.id.tilCategory);
        tilDescription = findViewById(R.id.tilDescription);
        etDate = findViewById(R.id.etDate);
        etAmount = findViewById(R.id.etAmount);
        etCategory = findViewById(R.id.etCategory);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        if (savedInstanceState != null) {
            etDate.setText(savedInstanceState.getString("date"));
            etAmount.setText(savedInstanceState.getString("amount"));
            etCategory.setText(savedInstanceState.getString("category"));
            etDescription.setText(savedInstanceState.getString("description"));
            Log.d(TAG, "Restored state: date=" + etDate.getText().toString() + ", amount=" + etAmount.getText().toString() +
                    ", category=" + etCategory.getText().toString() + ", description=" + etDescription.getText().toString());
        }

        etDate.setOnClickListener(v -> showDatePicker());
        Log.d(TAG, "Date picker listener set");

        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            setResult(RESULT_CANCELED);
            finish();
        });

//        btnSave.setOnClickListener(v -> {
//            Log.d(TAG, "Save button clicked");
//
//            tilDate.setError(null);
//            tilAmount.setError(null);
//            tilCategory.setError(null);
//            tilDescription.setError(null);
//
//            String date = etDate.getText().toString().trim();
//            String amount = etAmount.getText().toString().trim();
//            String category = etCategory.getText().toString().trim();
//            String description = etDescription.getText().toString().trim();
//            Log.d(TAG, "Input values: date=" + date + ", amount=" + amount + ", category=" + category + ", description=" + description);
//
//            if (date.isEmpty()) {
//                Log.w(TAG, "Validation failed: Date is empty");
//                tilDate.setError("Vui lòng chọn ngày");
//                return;
//            }
//            if (amount.isEmpty()) {
//                Log.w(TAG, "Validation failed: Amount is empty");
//                tilAmount.setError("Vui lòng nhập số tiền");
//                return;
//            }
//            if (category.isEmpty()) {
//                Log.w(TAG, "Validation failed: Category is empty");
//                tilCategory.setError("Vui lòng nhập danh mục");
//                return;
//            }
//            if (description.isEmpty()) {
//                Log.w(TAG, "Validation failed: Description is empty");
//                tilDescription.setError("Vui lòng nhập mô tả");
//                return;
//            }
//
//            try {
//                double amountValue = Double.parseDouble(amount);
//                if (amountValue <= 0) {
//                    tilAmount.setError("Số tiền phải lớn hơn 0");
//                    return;
//                }
//
//                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//                inputFormat.setLenient(false);
//                java.util.Date parsedDate = inputFormat.parse(date);
//                String formattedDate = outputFormat.format(parsedDate);
//
//                String userId = "user1"; // Thay bằng userId thực tế từ session
//
//                btnBack.setEnabled(false);
//                btnSave.setEnabled(false);
//
//                databaseHelper.addExpenseAsync(
//                        userId,
//                        String.valueOf(amountValue),
//                        category,  // Đây là category_id, cần đảm bảo category hợp lệ
//                        description,
//                        formattedDate,
//                        new DatabaseHelper.SimpleCallback() {
//                            @Override
//                            public void onSuccess() {
//                                Intent resultIntent = new Intent();
//                                resultIntent.putExtra("date", date);
//                                resultIntent.putExtra("amount", amount);
//                                resultIntent.putExtra("category", category);
//                                resultIntent.putExtra("description", description);
//                                setResult(RESULT_OK, resultIntent);
//                                finish();
//                            }
//
//                            @Override
//                            public void onError(String errorMessage) {
//                                tilAmount.setError("Lỗi: " + errorMessage);
//                                btnBack.setEnabled(true);
//                                btnSave.setEnabled(true);
//                            }
//                        }
//                );
//            } catch (NumberFormatException e) {
//                tilAmount.setError("Số tiền phải là một số hợp lệ");
//            } catch (java.text.ParseException e) {
//                tilDate.setError("Định dạng ngày không hợp lệ (dd/MM/yyyy)");
//            } catch (Exception e) {
//                tilAmount.setError("Lỗi không xác định: " + e.getMessage());
//                btnBack.setEnabled(true);
//                btnSave.setEnabled(true);
//            }
//        });
        Log.d(TAG, "Save button listener set");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("date", etDate.getText().toString().trim());
        outState.putString("amount", etAmount.getText().toString().trim());
        outState.putString("category", etCategory.getText().toString().trim());
        outState.putString("description", etDescription.getText().toString().trim());
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
}