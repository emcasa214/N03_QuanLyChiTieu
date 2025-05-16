package com.example.n03_quanlychitieu.ui.income;

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

public class ViewIncomeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_INCOME = 100;
    private static final int REQUEST_CODE_UPDATE_INCOME = 101;
    private static final String PREFS_NAME = "IncomePrefs";
    private static final String KEY_INCOME_LIST = "income_list";
    private static final String TAG = "ViewIncomeActivity";

    private TableLayout tableIncome;
    private List<Income> incomeList;
    private DecimalFormat decimalFormat;
    private SharedPreferences sharedPreferences;
    private int selectedPosition = -1; // Lưu vị trí của mục được chọn để sửa
    private List<TableRow> tableRows = new ArrayList<>();

    private static class Income {
        String amount;
        String source;
        String date;

        Income(String amount, String source, String date) {
            this.amount = amount != null ? amount : "";
            this.source = source != null ? source : "";
            this.date = date != null ? date : "";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_income);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        incomeList = new ArrayList<>();
        decimalFormat = new DecimalFormat("#,###");

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Thu nhập");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            Log.e(TAG, "Toolbar is null");
        }

        tableIncome = findViewById(R.id.tableIncome);
        if (tableIncome == null) {
            Log.e(TAG, "TableIncome is null");
        }

        loadIncomeList();
        populateTable();
        Log.d(TAG, "onCreate: Danh sách thu nhập ban đầu: " + incomeList.size() + " mục");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_income, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_income) {
            Intent intent = new Intent(ViewIncomeActivity.this, AddIncomeActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_INCOME);
            return true;
        } else if (id == R.id.action_edit_income) {
            Log.d(TAG, "Nhấn Sửa thu nhập, selectedPosition: " + selectedPosition);
            if (selectedPosition < 0 || selectedPosition >= incomeList.size()) {
                Toast.makeText(this, "Vui lòng nhấn vào một mục để sửa", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Vị trí không hợp lệ: " + selectedPosition + ", Kích thước danh sách: " + incomeList.size());
            } else {
                Income income = incomeList.get(selectedPosition);
                if (income != null) {
                    Log.d(TAG, "Sửa thu nhập tại vị trí: " + selectedPosition + ", Dữ liệu: " + income.amount + ", " + income.source + ", " + income.date);
                    Intent intent = new Intent(ViewIncomeActivity.this, UpdateIncomeActivity.class);
                    intent.putExtra("amount", income.amount);
                    intent.putExtra("source", income.source);
                    intent.putExtra("date", income.date);
                    intent.putExtra("position", selectedPosition);
                    startActivityForResult(intent, REQUEST_CODE_UPDATE_INCOME);
                } else {
                    Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Dữ liệu tại vị trí " + selectedPosition + " là null");
                }
            }
            return true;
        } else if (id == android.R.id.home) {
            Intent intent = new Intent(ViewIncomeActivity.this, MainActivity.class);
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
        if (requestCode == REQUEST_CODE_ADD_INCOME && resultCode == RESULT_OK && data != null) {
            String amount = data.getStringExtra("amount");
            String source = data.getStringExtra("source");
            String date = data.getStringExtra("date");

            if (amount != null && source != null && date != null) {
                incomeList.add(new Income(amount, source, date));
                saveIncomeList();
                populateTable();
                Log.d(TAG, "Thêm thu nhập thành công, danh sách: " + incomeList.size() + " mục");
            } else {
                Log.e(TAG, "Dữ liệu thêm không hợp lệ: amount=" + amount + ", source=" + source + ", date=" + date);
            }
        } else if (requestCode == REQUEST_CODE_UPDATE_INCOME && resultCode == RESULT_OK && data != null) {
            int position = data.getIntExtra("position", -1);
            Log.d(TAG, "Nhận kết quả sửa, position: " + position);
            if (position >= 0 && position < incomeList.size()) {
                String amount = data.getStringExtra("amount");
                String source = data.getStringExtra("source");
                String date = data.getStringExtra("date");
                if (amount != null && source != null && date != null) {
                    incomeList.set(position, new Income(amount, source, date));
                    saveIncomeList();
                    populateTable();
                    selectedPosition = -1;
                    resetRowBackgrounds();
                    Log.d(TAG, "Cập nhật thành công tại vị trí: " + position + ", Dữ liệu: " + amount + ", " + source + ", " + date);
                } else {
                    Log.e(TAG, "Dữ liệu trả về không hợp lệ: amount=" + amount + ", source=" + source + ", date=" + date);
                    Toast.makeText(this, "Lỗi cập nhật dữ liệu", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Vị trí không hợp lệ: " + position + ", Kích thước danh sách: " + incomeList.size());
                Toast.makeText(this, "Lỗi cập nhật dữ liệu", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveIncomeList() {
        StringBuilder sb = new StringBuilder();
        for (Income income : incomeList) {
            String safeAmount = (income.amount != null ? income.amount.replace("|", "").replace(";", "") : "");
            String safeSource = (income.source != null ? income.source.replace("|", "").replace(";", "") : "");
            String safeDate = (income.date != null ? income.date.replace("|", "").replace(";", "") : "");
            sb.append(safeAmount).append("|").append(safeSource).append("|").append(safeDate).append(";");
        }
        sharedPreferences.edit().putString(KEY_INCOME_LIST, sb.toString()).apply();
        Log.d(TAG, "Lưu danh sách thành công: " + sb.toString());
    }

    private void loadIncomeList() {
        String data = sharedPreferences.getString(KEY_INCOME_LIST, "");
        incomeList.clear();
        if (!data.isEmpty()) {
            String[] records = data.split(";");
            for (String record : records) {
                if (record.trim().isEmpty()) continue;
                String[] parts = record.split("\\|");
                if (parts.length == 3) {
                    incomeList.add(new Income(parts[0], parts[1], parts[2]));
                } else {
                    Log.w(TAG, "Dữ liệu không hợp lệ: " + record);
                }
            }
        }
        Log.d(TAG, "Tải danh sách thành công: " + incomeList.size() + " mục");
    }

    private void populateTable() {
        if (tableIncome == null) {
            Log.e(TAG, "TableIncome is null, không thể populate");
            return;
        }
        while (tableIncome.getChildCount() > 1) {
            tableIncome.removeViewAt(tableIncome.getChildCount() - 1);
        }
        tableRows.clear();

        for (int i = 0; i < incomeList.size(); i++) {
            Income income = incomeList.get(i);
            addIncomeToTable(income, i);
        }
        Log.d(TAG, "Cập nhật bảng thành công với " + incomeList.size() + " mục");
    }

    private void addIncomeToTable(Income income, int position) {
        if (income == null) {
            Log.e(TAG, "Income is null at position " + position);
            return;
        }
        TableRow newRow = new TableRow(this);
        if (newRow == null) {
            Log.e(TAG, "TableRow is null");
            return;
        }
        newRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView tvAmount = new TextView(this);
        try {
            double amountValue = Double.parseDouble(income.amount);
            tvAmount.setText(decimalFormat.format(amountValue));
        } catch (NumberFormatException e) {
            tvAmount.setText(income.amount != null ? income.amount : "0");
            Log.w(TAG, "Lỗi chuyển đổi số tiền tại vị trí " + position + ": " + e.getMessage());
        }
        tvAmount.setPadding(8, 8, 8, 8);
        tvAmount.setTextSize(14);
        TableRow.LayoutParams amountParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvAmount.setLayoutParams(amountParams);
        newRow.addView(tvAmount);

        TextView tvSource = new TextView(this);
        tvSource.setText(income.source != null ? income.source : "");
        tvSource.setPadding(8, 8, 8, 8);
        tvSource.setTextSize(14);
        TableRow.LayoutParams sourceParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvSource.setLayoutParams(sourceParams);
        newRow.addView(tvSource);

        TextView tvDate = new TextView(this);
        tvDate.setText(income.date != null ? income.date : "");
        tvDate.setPadding(8, 8, 8, 8);
        tvDate.setTextSize(14);
        TableRow.LayoutParams dateParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvDate.setLayoutParams(dateParams);
        newRow.addView(tvDate);

        newRow.setOnClickListener(v -> {
            selectedPosition = position;
            resetRowBackgrounds();
            newRow.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            Log.d(TAG, "Chọn hàng tại vị trí: " + selectedPosition + ", Dữ liệu: " + income.amount + ", " + income.source + ", " + income.date);
        });

        newRow.setOnLongClickListener(v -> {
            new AlertDialog.Builder(ViewIncomeActivity.this)
                    .setTitle("Xóa thu nhập")
                    .setMessage("Bạn có chắc muốn xóa mục này?")
                    .setPositiveButton("OK", (dialog, which) -> {
                        if (position < incomeList.size()) {
                            incomeList.remove(position);
                            saveIncomeList();
                            populateTable();
                            selectedPosition = -1;
                            Toast.makeText(ViewIncomeActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Xóa thành công tại vị trí: " + position);
                        } else {
                            Log.e(TAG, "Vị trí xóa không hợp lệ: " + position);
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            return true;
        });

        tableIncome.addView(newRow);
        tableRows.add(newRow);
    }

    private void resetRowBackgrounds() {
        for (TableRow row : tableRows) {
            row.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
    }
}