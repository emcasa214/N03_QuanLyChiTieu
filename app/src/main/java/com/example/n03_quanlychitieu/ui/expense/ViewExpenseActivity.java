package com.example.n03_quanlychitieu.ui.expense;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.ui.main.MainActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ViewExpenseActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_EXPENSE = 200;
    private static final int REQUEST_CODE_UPDATE_EXPENSE = 201;
    private static final String PREFS_NAME = "ExpensePrefs";
    private static final String KEY_EXPENSE_LIST = "expense_list";
    private static final String TAG = "ViewExpenseActivity";

    private TableLayout tableExpense;
    private List<Expense> expenseList;
    private DecimalFormat decimalFormat;
    private SharedPreferences sharedPreferences;
    private int selectedPosition = -1; // Lưu vị trí của mục được chọn để sửa
    private List<TableRow> tableRows = new ArrayList<>();

    private static class Expense {
        String date;
        String amount;
        String category;
        String description;

        Expense(String date, String amount, String category, String description) {
            this.date = date != null ? date : "";
            this.amount = amount != null ? amount : "";
            this.category = category != null ? category : "";
            this.description = description != null ? description : "";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expense);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        expenseList = new ArrayList<>();
        decimalFormat = new DecimalFormat("#,###");

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Chi tiêu");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            Log.e(TAG, "Toolbar is null");
        }

        tableExpense = findViewById(R.id.tableExpense);
        if (tableExpense == null) {
            Log.e(TAG, "TableExpense is null");
        }

        loadExpenseList();
        populateTable();
        Log.d(TAG, "onCreate: Danh sách chi tiêu ban đầu: " + expenseList.size() + " mục");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_expense, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_expense) {
            Intent intent = new Intent(ViewExpenseActivity.this, AddExpenseActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_EXPENSE);
            return true;
        } else if (id == R.id.action_edit_expense) {
            Log.d(TAG, "Nhấn Sửa chi tiêu, selectedPosition: " + selectedPosition);
            if (selectedPosition < 0 || selectedPosition >= expenseList.size()) {
                Toast.makeText(this, "Vui lòng nhấn vào một mục để sửa", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Vị trí không hợp lệ: " + selectedPosition + ", Kích thước danh sách: " + expenseList.size());
            } else {
                Expense expense = expenseList.get(selectedPosition);
                if (expense != null) {
                    Log.d(TAG, "Sửa chi tiêu tại vị trí: " + selectedPosition + ", Dữ liệu: " + expense.date + ", " + expense.amount + ", " + expense.category + ", " + expense.description);
                    Intent intent = new Intent(ViewExpenseActivity.this, UpdateExpenseActivity.class);
                    intent.putExtra("date", expense.date);
                    intent.putExtra("amount", expense.amount);
                    intent.putExtra("category", expense.category);
                    intent.putExtra("description", expense.description);
                    intent.putExtra("position", selectedPosition);
                    startActivityForResult(intent, REQUEST_CODE_UPDATE_EXPENSE);
                } else {
                    Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Dữ liệu tại vị trí " + selectedPosition + " là null");
                }
            }
            return true;
        } else if (id == android.R.id.home) {
            Intent intent = new Intent(ViewExpenseActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_EXPENSE && resultCode == RESULT_OK && data != null) {
            String date = data.getStringExtra("date");
            String amount = data.getStringExtra("amount");
            String category = data.getStringExtra("category");
            String description = data.getStringExtra("description");

            if (date != null && amount != null && category != null && description != null) {
                expenseList.add(new Expense(date, amount, category, description));
                saveExpenseList();
                populateTable();
                Log.d(TAG, "Thêm chi tiêu thành công, danh sách: " + expenseList.size() + " mục");
            } else {
                Log.e(TAG, "Dữ liệu thêm không hợp lệ: date=" + date + ", amount=" + amount + ", category=" + category + ", description=" + description);
            }
        } else if (requestCode == REQUEST_CODE_UPDATE_EXPENSE && resultCode == RESULT_OK && data != null) {
            int position = data.getIntExtra("position", -1);
            Log.d(TAG, "Nhận kết quả sửa, position: " + position);
            if (position >= 0 && position < expenseList.size()) {
                String date = data.getStringExtra("date");
                String amount = data.getStringExtra("amount");
                String category = data.getStringExtra("category");
                String description = data.getStringExtra("description");
                if (date != null && amount != null && category != null && description != null) {
                    expenseList.set(position, new Expense(date, amount, category, description));
                    saveExpenseList();
                    populateTable();
                    selectedPosition = -1;
                    resetRowBackgrounds();
                    Log.d(TAG, "Cập nhật thành công tại vị trí: " + position + ", Dữ liệu: " + date + ", " + amount + ", " + category + ", " + description);
                } else {
                    Log.e(TAG, "Dữ liệu trả về không hợp lệ: date=" + date + ", amount=" + amount + ", category=" + category + ", description=" + description);
                    Toast.makeText(this, "Lỗi cập nhật dữ liệu", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Vị trí không hợp lệ: " + position + ", Kích thước danh sách: " + expenseList.size());
                Toast.makeText(this, "Lỗi cập nhật dữ liệu", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveExpenseList() {
        StringBuilder sb = new StringBuilder();
        for (Expense expense : expenseList) {
            String safeDate = (expense.date != null ? expense.date.replace("|", "").replace(";", "") : "");
            String safeAmount = (expense.amount != null ? expense.amount.replace("|", "").replace(";", "") : "");
            String safeCategory = (expense.category != null ? expense.category.replace("|", "").replace(";", "") : "");
            String safeDescription = (expense.description != null ? expense.description.replace("|", "").replace(";", "") : "");
            sb.append(safeDate).append("|").append(safeAmount).append("|").append(safeCategory).append("|").append(safeDescription).append(";");
        }
        sharedPreferences.edit().putString(KEY_EXPENSE_LIST, sb.toString()).apply();
        Log.d(TAG, "Lưu danh sách thành công: " + sb.toString());
    }

    private void loadExpenseList() {
        String data = sharedPreferences.getString(KEY_EXPENSE_LIST, "");
        expenseList.clear();
        if (!data.isEmpty()) {
            String[] records = data.split(";");
            for (String record : records) {
                if (record.trim().isEmpty()) continue;
                String[] parts = record.split("\\|");
                if (parts.length == 4) {
                    expenseList.add(new Expense(parts[0], parts[1], parts[2], parts[3]));
                } else {
                    Log.w(TAG, "Dữ liệu không hợp lệ: " + record);
                }
            }
        }
        Log.d(TAG, "Tải danh sách thành công: " + expenseList.size() + " mục");
    }

    private void populateTable() {
        if (tableExpense == null) {
            Log.e(TAG, "TableExpense is null, không thể populate");
            return;
        }
        while (tableExpense.getChildCount() > 1) {
            tableExpense.removeViewAt(tableExpense.getChildCount() - 1);
        }
        tableRows.clear();

        for (int i = 0; i < expenseList.size(); i++) {
            Expense expense = expenseList.get(i);
            addExpenseToTable(expense, i);
        }
        Log.d(TAG, "Cập nhật bảng thành công với " + expenseList.size() + " mục");
    }

    private void addExpenseToTable(Expense expense, int position) {
        if (expense == null) {
            Log.e(TAG, "Expense is null at position " + position);
            return;
        }
        TableRow newRow = new TableRow(this);
        if (newRow == null) {
            Log.e(TAG, "TableRow is null");
            return;
        }
        newRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView tvDate = new TextView(this);
        tvDate.setText(expense.date != null ? expense.date : "");
        tvDate.setPadding(8, 8, 8, 8);
        tvDate.setTextSize(14);
        TableRow.LayoutParams dateParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvDate.setLayoutParams(dateParams);
        newRow.addView(tvDate);

        TextView tvAmount = new TextView(this);
        try {
            double amountValue = Double.parseDouble(expense.amount);
            tvAmount.setText(decimalFormat.format(amountValue));
        } catch (NumberFormatException e) {
            tvAmount.setText(expense.amount != null ? expense.amount : "0");
            Log.w(TAG, "Lỗi chuyển đổi số tiền tại vị trí " + position + ": " + e.getMessage());
        }
        tvAmount.setPadding(8, 8, 8, 8);
        tvAmount.setTextSize(14);
        TableRow.LayoutParams amountParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvAmount.setLayoutParams(amountParams);
        newRow.addView(tvAmount);

        TextView tvCategory = new TextView(this);
        tvCategory.setText(expense.category != null ? expense.category : "");
        tvCategory.setPadding(8, 8, 8, 8);
        tvCategory.setTextSize(14);
        TableRow.LayoutParams categoryParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvCategory.setLayoutParams(categoryParams);
        newRow.addView(tvCategory);

        TextView tvDescription = new TextView(this);
        tvDescription.setText(expense.description != null ? expense.description : "");
        tvDescription.setPadding(8, 8, 8, 8);
        tvDescription.setTextSize(14);
        TableRow.LayoutParams descriptionParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvDescription.setLayoutParams(descriptionParams);
        newRow.addView(tvDescription);

        newRow.setOnClickListener(v -> {
            selectedPosition = position;
            resetRowBackgrounds();
            newRow.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            Log.d(TAG, "Chọn hàng tại vị trí: " + selectedPosition + ", Dữ liệu: " + expense.date + ", " + expense.amount + ", " + expense.category + ", " + expense.description);
        });

        newRow.setOnLongClickListener(v -> {
            new AlertDialog.Builder(ViewExpenseActivity.this)
                    .setTitle("Xóa chi tiêu")
                    .setMessage("Bạn có chắc muốn xóa mục này?")
                    .setPositiveButton("OK", (dialog, which) -> {
                        if (position < expenseList.size()) {
                            expenseList.remove(position);
                            saveExpenseList();
                            populateTable();
                            selectedPosition = -1;
                            Toast.makeText(ViewExpenseActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Xóa thành công tại vị trí: " + position);
                        } else {
                            Log.e(TAG, "Vị trí xóa không hợp lệ: " + position);
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            return true;
        });

        tableExpense.addView(newRow);
        tableRows.add(newRow);
    }

    private void resetRowBackgrounds() {
        for (TableRow row : tableRows) {
            row.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
    }
}