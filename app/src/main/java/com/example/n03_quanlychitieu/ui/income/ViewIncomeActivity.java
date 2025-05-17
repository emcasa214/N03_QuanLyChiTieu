package com.example.n03_quanlychitieu.ui.income;

import android.content.Intent;
import android.os.Build;
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
import com.example.n03_quanlychitieu.ui.income.AddIncomeActivity;
import com.example.n03_quanlychitieu.ui.income.UpdateIncomeActivity;
import com.example.n03_quanlychitieu.ui.sign.LogIn;
import com.example.n03_quanlychitieu.utils.AuthenticationManager;
import com.google.android.material.appbar.AppBarLayout;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ViewIncomeActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ADD_INCOME = 100;
    private static final int REQUEST_CODE_UPDATE_INCOME = 101;
    private static final String TAG = "ViewIncomeActivity";
    private TableLayout tableIncome;
    private List<Income> incomeList;
    private DecimalFormat decimalFormat;
    private int selectedPosition = -1;
    private List<TableRow> tableRows = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private AuthenticationManager auth;
    private String userId;

    public static class Income {
        String id;
        String amount;
        String source;
        String date;
        String categoryId;

        public Income(String incomeId, double amount, String description, String createAt, String userId, String categoryId) {
            this.id = incomeId;
            this.amount = String.valueOf(amount);
            this.source = description != null ? description : "Chưa có mô tả";
            this.date = createAt != null ? createAt : "";
            this.categoryId = categoryId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_income);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "onCreate started at " + LocalDateTime.now());
        }

        auth = AuthenticationManager.getInstance(this);
        databaseHelper = new DatabaseHelper(this);

        if (!auth.isUserLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogIn.class));
            finish();
            return;
        }

        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUser_id() : null;
        }

        if (userId == null) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
            auth.logout();
            startActivity(new Intent(this, LogIn.class));
            finish();
            return;
        }
        Log.d(TAG, "Received userId: " + userId);

        incomeList = new ArrayList<>();
        decimalFormat = new DecimalFormat("#,###");

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thu nhập");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tableIncome = findViewById(R.id.tableIncome);

        loadIncomeList();
        populateTable();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userId", userId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
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
            Intent intent = new Intent(this, AddIncomeActivity.class);
            intent.putExtra("userId", userId);
            startActivityForResult(intent, REQUEST_CODE_ADD_INCOME);
            return true;
        } else if (id == R.id.action_edit_income) {
            if (selectedPosition < 0 || selectedPosition >= incomeList.size()) {
                Toast.makeText(this, "Vui lòng nhấn vào một mục để sửa", Toast.LENGTH_SHORT).show();
            } else {
                Income income = incomeList.get(selectedPosition);
                Intent intent = new Intent(this, UpdateIncomeActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("incomeId", income.id);
                intent.putExtra("amount", income.amount);
                intent.putExtra("source", income.source);
                intent.putExtra("date", income.date);
                intent.putExtra("categoryId", income.categoryId);
                startActivityForResult(intent, REQUEST_CODE_UPDATE_INCOME);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_CODE_ADD_INCOME || requestCode == REQUEST_CODE_UPDATE_INCOME) && resultCode == RESULT_OK) {
            loadIncomeList();
            populateTable();
            selectedPosition = -1;
            resetRowBackgrounds();
        }
    }

    private void loadIncomeList() {
        incomeList.clear();
        if (userId != null) {
            incomeList.addAll(databaseHelper.getIncomesByUserId(userId));
        }
    }

    private void populateTable() {
        while (tableIncome.getChildCount() > 1) {
            tableIncome.removeViewAt(tableIncome.getChildCount() - 1);
        }
        tableRows.clear();
        for (int i = 0; i < incomeList.size(); i++) {
            addIncomeToTable(incomeList.get(i), i);
        }
    }

    private void addIncomeToTable(Income income, int position) {
        TableRow newRow = new TableRow(this);
        newRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        // Cột "Số tiền"
        TextView tvAmount = new TextView(this);
        try {
            double amountValue = Double.parseDouble(income.amount);
            tvAmount.setText(decimalFormat.format(amountValue) + " VND");
        } catch (NumberFormatException e) {
            tvAmount.setText(income.amount + " VND");
        }
        tvAmount.setPadding(8, 8, 8, 8);
        tvAmount.setTextSize(14);
        TableRow.LayoutParams amountParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvAmount.setLayoutParams(amountParams);
        newRow.addView(tvAmount);

        // Cột "Danh mục"
        TextView tvCategory = new TextView(this);
        String categoryName = databaseHelper.getCategoryNameById(income.categoryId);
        tvCategory.setText(categoryName != null ? categoryName : "Không xác định");
        tvCategory.setPadding(8, 8, 8, 8);
        tvCategory.setTextSize(14);
        TableRow.LayoutParams categoryParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvCategory.setLayoutParams(categoryParams);
        newRow.addView(tvCategory);

        // Cột "Mô tả"
        TextView tvSource = new TextView(this);
        tvSource.setText(income.source != null && !income.source.isEmpty() ? income.source : "Chưa có mô tả");
        tvSource.setPadding(8, 8, 8, 8);
        tvSource.setTextSize(14);
        TableRow.LayoutParams sourceParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvSource.setLayoutParams(sourceParams);
        newRow.addView(tvSource);

        // Cột "Thời gian"
        TextView tvDate = new TextView(this);
        tvDate.setText(income.date != null && !income.date.isEmpty() ? income.date : "Chưa có thời gian");
        tvDate.setPadding(8, 8, 8, 8);
        tvDate.setTextSize(14);
        TableRow.LayoutParams dateParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        tvDate.setLayoutParams(dateParams);
        newRow.addView(tvDate);

        newRow.setOnClickListener(v -> {
            selectedPosition = position;
            resetRowBackgrounds();
            newRow.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        });

        newRow.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa thu nhập")
                    .setMessage("Bạn có chắc muốn xóa mục này?")
                    .setPositiveButton("OK", (dialog, which) -> {
                        databaseHelper.deleteIncomeAsync(income.id, userId, new DatabaseHelper.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                loadIncomeList();
                                populateTable();
                                selectedPosition = -1;
                                Toast.makeText(ViewIncomeActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(ViewIncomeActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
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