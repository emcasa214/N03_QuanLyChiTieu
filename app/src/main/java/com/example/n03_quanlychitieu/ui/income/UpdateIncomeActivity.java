package com.example.n03_quanlychitieu.ui.income;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.n03_quanlychitieu.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class UpdateIncomeActivity extends AppCompatActivity {

    private static final String TAG = "UpdateIncomeActivity";

    private TextInputEditText etAmount, etSource, etDate;
    private Button btnUpdate, btnCancel;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_income);

        calendar = Calendar.getInstance();

        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarUpdate);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Khởi tạo views
        etAmount = findViewById(R.id.etAmount);
        etSource = findViewById(R.id.etSource);
        etDate = findViewById(R.id.etDate);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        String amount = intent.getStringExtra("amount");
        String source = intent.getStringExtra("source");
        String date = intent.getStringExtra("date");
        int position = intent.getIntExtra("position", -1);

        Log.d(TAG, "Nhận dữ liệu: amount=" + amount + ", source=" + source + ", date=" + date + ", position=" + position);

        if (amount != null) {
            etAmount.setText(amount);
        } else {
            Log.w(TAG, "Amount is null");
        }
        if (source != null) {
            etSource.setText(source);
        } else {
            Log.w(TAG, "Source is null");
        }
        if (date != null) {
            etDate.setText(date);
        } else {
            Log.w(TAG, "Date is null");
        }

        // DatePicker cho trường ngày
        etDate.setOnClickListener(v -> showDatePicker());

        // Xử lý nút Cập nhật
        btnUpdate.setOnClickListener(v -> {
            String newAmount = etAmount.getText().toString().trim();
            String newSource = etSource.getText().toString().trim();
            String newDate = etDate.getText().toString().trim();

            if (!newAmount.isEmpty() && !newSource.isEmpty() && !newDate.isEmpty()) {
                try {
                    Double.parseDouble(newAmount);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("amount", newAmount);
                    resultIntent.putExtra("source", newSource);
                    resultIntent.putExtra("date", newDate);
                    resultIntent.putExtra("position", position);
                    setResult(RESULT_OK, resultIntent);
                    Log.d(TAG, "Gửi dữ liệu trở lại: amount=" + newAmount + ", source=" + newSource + ", date=" + newDate + ", position=" + position);
                    finish();
                } catch (NumberFormatException e) {
                    etAmount.setError("Số tiền không hợp lệ");
                    Log.w(TAG, "Số tiền không hợp lệ: " + newAmount);
                }
            } else {
                etAmount.setError(newAmount.isEmpty() ? "Vui lòng nhập số tiền" : null);
                etSource.setError(newSource.isEmpty() ? "Vui lòng nhập nguồn" : null);
                etDate.setError(newDate.isEmpty() ? "Vui lòng chọn ngày" : null);
                Log.w(TAG, "Dữ liệu không hợp lệ: amount=" + newAmount + ", source=" + newSource + ", date=" + newDate);
            }
        });

        // Xử lý nút Hủy
        btnCancel.setOnClickListener(v -> {
            Log.d(TAG, "Nhấn Hủy");
            finish();
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String date = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
                    etDate.setText(date);
                    Log.d(TAG, "Chọn ngày: " + date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "Nhấn nút Back trên Toolbar");
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}