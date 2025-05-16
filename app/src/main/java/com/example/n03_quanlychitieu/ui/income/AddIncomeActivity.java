package com.example.n03_quanlychitieu.ui.income;

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

public class AddIncomeActivity extends AppCompatActivity {
    private static final String TAG = "AddIncomeActivity";
    private TextInputEditText etAmount, etSource, etDate;
    private TextInputLayout tilAmount, tilSource, tilDate;
    private Button btnSave;
    private ImageButton btnBack;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_income);
        Log.d(TAG, "onCreate called");

        databaseHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tilAmount = findViewById(R.id.tilAmount);
        tilSource = findViewById(R.id.tilSource);
        tilDate = findViewById(R.id.tilDate);
        etAmount = findViewById(R.id.etAmount);
        etSource = findViewById(R.id.etSource);
        etDate = findViewById(R.id.etDate);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        if (savedInstanceState != null) {
            etAmount.setText(savedInstanceState.getString("amount"));
            etSource.setText(savedInstanceState.getString("source"));
            etDate.setText(savedInstanceState.getString("date"));
            Log.d(TAG, "Restored state: amount=" + etAmount.getText().toString() + ", source=" + etSource.getText().toString() + ", date=" + etDate.getText().toString());
        }

        etDate.setOnClickListener(v -> showDatePicker());
        Log.d(TAG, "Date picker listener set");

        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            setResult(RESULT_CANCELED);
            finish();
        });

        btnSave.setOnClickListener(v -> {
            Log.d(TAG, "Save button clicked");

            tilAmount.setError(null);
            tilSource.setError(null);
            tilDate.setError(null);

            String amount = etAmount.getText().toString().trim();
            String source = etSource.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            Log.d(TAG, "Input values: amount=" + amount + ", source=" + source + ", date=" + date);

            if (amount.isEmpty()) {
                Log.w(TAG, "Validation failed: Amount is empty");
                tilAmount.setError("Vui lòng nhập số tiền");
                return;
            }

            if (source.isEmpty()) {
                Log.w(TAG, "Validation failed: Source is empty");
                tilSource.setError("Vui lòng nhập nguồn thu nhập");
                return;
            }

            if (date.isEmpty()) {
                Log.w(TAG, "Validation failed: Date is empty");
                tilDate.setError("Vui lòng chọn ngày");
                return;
            }

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

                String userId = "user1";

                btnBack.setEnabled(false);
                btnSave.setEnabled(false);

                databaseHelper.addIncomeAsync(
                        userId,
                        String.valueOf(amountValue),
                        "1",
                        formattedDate,
                        new DatabaseHelper.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("amount", amount);
                                resultIntent.putExtra("source", source);
                                resultIntent.putExtra("date", date);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            }

                            @Override
                            public void onError(String errorMessage) {
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("amount", etAmount.getText().toString().trim());
        outState.putString("source", etSource.getText().toString().trim());
        outState.putString("date", etDate.getText().toString().trim());
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
