package com.example.n03_quanlychitieu.ui.expense;

import android.content.Intent;
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
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.ui.main.MainActivity;
import com.example.n03_quanlychitieu.ui.expense.AddExpenseActivity;
import com.example.n03_quanlychitieu.ui.expense.UpdateExpenseActivity;
import com.example.n03_quanlychitieu.utils.AuthenticationManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ViewExpenseActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ADD_EXPENSE    = 200;
    private static final int REQUEST_CODE_UPDATE_EXPENSE = 201;
    private static final String TAG = "ViewExpenseActivity";

    private TableLayout tableExpense;
    private List<Expense> expenseList = new ArrayList<>();
    private List<TableRow> tableRows   = new ArrayList<>();
    private DecimalFormat decimalFormat;
    private DatabaseHelper databaseHelper;
    private AuthenticationManager auth;
    private String userId;
    private int selectedPosition = -1;

    public static class Expense {
        String expenseId;
        double amount;
        String categoryId;
        String description;
        String createAt;

        public Expense(String expenseId, double amount,
                       String categoryId, String description,
                       String createAt) {
            this.expenseId   = expenseId;
            this.amount      = amount;
            this.categoryId  = categoryId;
            this.description = description;
            this.createAt    = createAt;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expense);

        databaseHelper = new DatabaseHelper(this);
        auth           = AuthenticationManager.getInstance(this);
        decimalFormat  = new DecimalFormat("#,###");

        userId = AuthenticationManager.getInstance(this).getCurrentUser().getUser_id();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiêu");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tableExpense = findViewById(R.id.tableExpense);

        loadExpenseList();
        populateTable();
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
            Intent intent = new Intent(this, AddExpenseActivity.class);
            intent.putExtra("userId", userId);
            startActivityForResult(intent, REQUEST_CODE_ADD_EXPENSE);
            return true;
        } else if (id == R.id.action_edit_expense) {
            if (selectedPosition < 0 || selectedPosition >= expenseList.size()) {
                Toast.makeText(this, "Vui lòng nhấn vào một mục để sửa", Toast.LENGTH_SHORT).show();
            } else {
                Expense e = expenseList.get(selectedPosition);
                Log.d(TAG, "expen = " + e.expenseId);
                Intent intent = new Intent(this, UpdateExpenseActivity.class);
                intent.putExtra("userId",      userId);
                intent.putExtra("expenseId",   e.expenseId);
                intent.putExtra("amount",      e.amount);
                intent.putExtra("categoryId",  e.categoryId);
                intent.putExtra("description", e.description);
                intent.putExtra("date",        e.createAt);
                startActivityForResult(intent, REQUEST_CODE_UPDATE_EXPENSE);

            }
            return true;
        } else if (id == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("userId", userId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if ((req == REQUEST_CODE_ADD_EXPENSE || req == REQUEST_CODE_UPDATE_EXPENSE)
                && res == RESULT_OK) {
            Log.d(TAG, "onActivityResult: Kết quả OK từ AddExpenseActivity hoặc UpdateExpenseActivity");
            loadExpenseList();
            populateTable();
            selectedPosition = -1;
            resetRowBackgrounds();
        }
    }

    private void loadExpenseList() {
        expenseList.clear();
        List<Expense> fromDb = databaseHelper.getExpensesByUserId(userId);
        if (fromDb != null) expenseList.addAll(fromDb);
    }

    private void showDeleteConfirmationDialog(Expense expense) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa chi tiêu")
                .setMessage("Bạn có chắc muốn xóa mục này?")
                .setPositiveButton("OK", (dialog, which) -> {
                    databaseHelper.deleteExpenseAsync(
                            expense.expenseId, userId,
                            new DatabaseHelper.SimpleCallback() {
                                @Override
                                public void onSuccess() {
                                    loadExpenseList();
                                    populateTable();
                                    selectedPosition = -1;
                                    Toast.makeText(ViewExpenseActivity.this,
                                            "Xóa thành công", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(String msg) {
                                    Toast.makeText(ViewExpenseActivity.this,
                                            msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void populateTable() {
        // xóa cũ
        while (tableExpense.getChildCount() > 1) {
            tableExpense.removeViewAt(tableExpense.getChildCount() - 1);
        }
        tableRows.clear();

        for (int i = 0; i < expenseList.size(); i++) {
            Expense e = expenseList.get(i);
            TableRow row = new TableRow(this);
            // Số tiền
            TextView tvAmt = new TextView(this);
            try {
                double v = e.amount;
                tvAmt.setText(decimalFormat.format(v) + " VND");
            } catch (Exception ex) {
                tvAmt.setText(e.amount + " VND");
            }
            tvAmt.setPadding(8,8,8,8); tvAmt.setTextSize(14);
            row.addView(tvAmt, new TableRow.LayoutParams(0,TableRow.LayoutParams.WRAP_CONTENT,1f));

            // Danh mục
            TextView tvCat = new TextView(this);
            String name = databaseHelper.getCategoryNameById(e.categoryId);
            tvCat.setText(name!=null?name:"Không rõ");
            tvCat.setPadding(8,8,8,8); tvCat.setTextSize(14);
            row.addView(tvCat, new TableRow.LayoutParams(0,TableRow.LayoutParams.WRAP_CONTENT,1f));

            // Mô tả
            TextView tvDesc = new TextView(this);
            tvDesc.setText(e.description!=null?e.description:"");
            tvDesc.setPadding(8,8,8,8); tvDesc.setTextSize(14);
            row.addView(tvDesc, new TableRow.LayoutParams(0,TableRow.LayoutParams.WRAP_CONTENT,1f));

            // Thời gian
            TextView tvDate = new TextView(this);
            tvDate.setText(e.createAt!=null?e.createAt:"");
            tvDate.setPadding(8,8,8,8); tvDate.setTextSize(14);
            row.addView(tvDate, new TableRow.LayoutParams(0,TableRow.LayoutParams.WRAP_CONTENT,1f));

            final int pos = i;
            row.setOnClickListener(v -> {
                selectedPosition = pos;
                resetRowBackgrounds();
                row.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            });
            row.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Xóa chi tiêu")
                        .setMessage("Bạn có chắc muốn xóa mục này?")
                        .setPositiveButton("OK", (d,w) -> {
                            databaseHelper.deleteExpenseAsync(
                                    e.expenseId, userId,
                                    new DatabaseHelper.SimpleCallback() {
                                        @Override public void onSuccess() {
                                            loadExpenseList();
                                            populateTable();
                                            selectedPosition = -1;
                                            Toast.makeText(ViewExpenseActivity.this,
                                                    "Xóa thành công", Toast.LENGTH_SHORT).show();
                                        }
                                        @Override public void onError(String msg) {
                                            Toast.makeText(ViewExpenseActivity.this,
                                                    msg, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        })
                        .setNegativeButton("Hủy",null)
                        .show();
                return true;
            });

            row.setOnLongClickListener(v -> {
                showDeleteConfirmationDialog(e); // Gọi hàm xử lý xóa
                return true;
            });

            tableExpense.addView(row);
            tableRows.add(row);
        }
    }

    private void resetRowBackgrounds() {
        for (TableRow r : tableRows) {
            r.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
    }
}
