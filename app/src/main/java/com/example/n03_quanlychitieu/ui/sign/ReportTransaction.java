package com.example.n03_quanlychitieu.ui.sign;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.adapter.ReportAdapter;
import com.example.n03_quanlychitieu.dao.ReportDAO;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.model.Categories;
import com.example.n03_quanlychitieu.model.Expenses;
import com.example.n03_quanlychitieu.model.Incomes;
import com.example.n03_quanlychitieu.utils.AuthenticationManager;
import com.example.n03_quanlychitieu.utils.PDFGenerator;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportTransaction extends AppCompatActivity {
    private TextView tvTotalIncome, tvTotalExpense, tvBalance;
    private PieChart chartSummary;
    private RecyclerView recyclerTransactions;
    private View emptyView;

    private ReportAdapter reportAdapter;
    private ReportDAO reportDAO;
    private AuthenticationManager authManager;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private PDFGenerator pdfGenerator;
    private Date currentStartDate, currentEndDate;
    private boolean showIncome = true; // Mặc định hiển thị thu nhập
    private ImageButton btn_back;
    private TextView kiemtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_report);

        // Khởi tạo các view
        initViews();

        // Khởi tạo các đối tượng cần thiết
        authManager = AuthenticationManager.getInstance(this);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        reportDAO = new ReportDAO(dbHelper.getWritableDatabase());
        pdfGenerator = new PDFGenerator(this);
        btn_back = findViewById(R.id.btn_back_report);
        kiemtra = findViewById(R.id.test);

        ImageButton btnDownload = findViewById(R.id.btn_download);
        btnDownload.setOnClickListener(v -> checkPermissionAndGeneratePDF());

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập sự kiện click
        setupClickListeners();

        // Load dữ liệu mặc định (hôm nay)
        loadTodayData();

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // Kiểm tra quyền trước khi tạo PDF
    private void checkPermissionAndGeneratePDF() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else {
            generatePDF();
        }
    }

    // Tạo và lưu file PDF
    private void generatePDF() {
        String userId = authManager.getCurrentUser().getUser_id();

        // Lấy dữ liệu hiện tại
        double totalIncome = reportDAO.getTotalIncome(userId, currentStartDate, currentEndDate);
        double totalExpense = reportDAO.getTotalExpense(userId, currentStartDate, currentEndDate);
        double balance = totalIncome - totalExpense;

        List<Incomes> incomes = reportDAO.getIncomesByTimeRange(userId, currentStartDate, currentEndDate);
        List<Expenses> expenses = reportDAO.getExpensesByTimeRange(userId, currentStartDate, currentEndDate);
        Map<Categories, Double> categoryStats = reportDAO.getCategoryStats(userId, currentStartDate, currentEndDate, "expense");
        Map<String, Double> timeStats = reportDAO.getTimeStats(userId, currentStartDate, currentEndDate, "expense", "day");

        // Tạo tên file với timestamp
        String fileName = "BaoCao_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

        // Gọi PDFGenerator để tạo file
        pdfGenerator.generateFinancialReport(
                fileName,
                totalIncome,
                totalExpense,
                balance,
                incomes,
                expenses,
                categoryStats,
                timeStats
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generatePDF();
            } else {
                Toast.makeText(this, "Cần cấp quyền để lưu báo cáo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        tvBalance = findViewById(R.id.tv_balance);
        chartSummary = findViewById(R.id.chart_summary);
        recyclerTransactions = findViewById(R.id.recycler_transactions);
        emptyView = findViewById(R.id.empty_view);

        // Thiết lập TabLayout
        com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    // Tab "Theo danh mục"
                    loadCategoryStats();
                } else {
                    // Tab "Theo thời gian"
                    loadTimeStats();
                }
            }

            @Override public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            @Override public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        reportAdapter = new ReportAdapter(this);
        recyclerTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerTransactions.setAdapter(reportAdapter);
    }

    private void setupClickListeners() {
        ImageButton btnFilter = findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> toggleIncomeExpense());

        // Các nút lọc thời gian
        findViewById(R.id.btn_today).setOnClickListener(v -> loadTodayData());
        findViewById(R.id.btn_week).setOnClickListener(v -> loadWeekData());
        findViewById(R.id.btn_month).setOnClickListener(v -> loadMonthData());
        findViewById(R.id.btn_custom).setOnClickListener(v -> showCustomDateDialog());
    }

    private void toggleIncomeExpense() {
        showIncome = !showIncome;
        if (showIncome) {
            loadIncomeData();
        } else {
            loadExpenseData();
        }
    }

    private void loadTodayData() {
        currentStartDate = reportDAO.getStartOfDay(new Date());
        currentEndDate = reportDAO.getEndOfDay(new Date());
//        kiemtra.setText(currentEndDate.toString());
        loadDataForDateRange();
    }

    private void loadWeekData() {
        Date[] weekRange = reportDAO.getCurrentWeekRange();
        currentStartDate = weekRange[0];
        currentEndDate = weekRange[1];
        loadDataForDateRange();
    }

    private void loadMonthData() {
        Date[] monthRange = reportDAO.getCurrentMonthRange();
        currentStartDate = monthRange[0];
        currentEndDate = monthRange[1];
        loadDataForDateRange();
    }

    private void showCustomDateDialog() {
        // Tạo dialog bằng MaterialDatePicker
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Chọn khoảng thời gian");

        // Thiết lập ngày hiện tại làm phạm vi mặc định
        Calendar calendar = Calendar.getInstance();
        long today = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_MONTH, -7); // Mặc định chọn 7 ngày gần nhất
        long oneWeekAgo = calendar.getTimeInMillis();

        builder.setSelection(Pair.create(oneWeekAgo, today));

        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        picker.show(getSupportFragmentManager(), "DATE_PICKER");

        // Xử lý khi chọn ngày xong
        picker.addOnPositiveButtonClickListener(selection -> {
            currentStartDate = new Date(selection.first);
            currentEndDate = new Date(selection.second);
            loadDataForDateRange();
        });
    }

    private void loadDataForDateRange() {
        String userId = authManager.getCurrentUser().getUser_id();

        // Tính tổng thu, tổng chi
        // Đang ko truy vấn được
        double totalIncome = reportDAO.getTotalIncome(userId, currentStartDate, currentEndDate);
        double totalExpense = reportDAO.getTotalExpense(userId, currentStartDate, currentEndDate);
        double balance = totalIncome - totalExpense;

//        kiemtra.setText(String.valueOf(totalIncome));

        // Hiển thị tổng quan
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalIncome.setText(currencyFormat.format(totalIncome));
        tvTotalExpense.setText(currencyFormat.format(totalExpense));
        tvBalance.setText(currencyFormat.format(balance));

        // Vẽ biểu đồ
        setupPieChart(totalIncome, totalExpense);

        // Load dữ liệu mặc định (thu nhập)
        loadIncomeData();
    }

    private void loadIncomeData() {
        String userId = authManager.getCurrentUser().getUser_id();
        List<Incomes> incomes = reportDAO.getIncomesByTimeRange(userId, currentStartDate, currentEndDate);
//        kiemtra.setText(incomes.toString());

        if (incomes.isEmpty()) {
            showEmptyView();
        } else {
            hideEmptyView();
            reportAdapter.setIncomes(incomes);
        }
    }

    private void loadExpenseData() {
        String userId = authManager.getCurrentUser().getUser_id();
        List<Expenses> expenses = reportDAO.getExpensesByTimeRange(userId, currentStartDate, currentEndDate);


        if (expenses.isEmpty()) {
            showEmptyView();
        } else {
            hideEmptyView();
            reportAdapter.setExpenses(expenses);
        }
    }

    private void loadCategoryStats() {
        String userId = authManager.getCurrentUser().getUser_id();
        String type = showIncome ? "income" : "expense";
        Map<Categories, Double> stats = reportDAO.getCategoryStats(userId, currentStartDate, currentEndDate, type);

        if (stats.isEmpty()) {
            showEmptyView();
        } else {
            hideEmptyView();
            reportAdapter.setCategoryStats(stats);
        }
    }

    private void loadTimeStats() {
        String userId = authManager.getCurrentUser().getUser_id();
        String type = showIncome ? "income" : "expense";
        Map<String, Double> stats = reportDAO.getTimeStats(userId, currentStartDate, currentEndDate, type, "day");

        if (stats.isEmpty()) {
            showEmptyView();
        } else {
            hideEmptyView();
            reportAdapter.setTimeStats(stats);
        }
    }

    private void setupPieChart(double income, double expense) {
        List<PieEntry> entries = new ArrayList<>();
        if (income > 0) entries.add(new PieEntry((float) income, "Income"));
        if (expense > 0) entries.add(new PieEntry((float) expense, "Expense"));

        if (entries.isEmpty()) {
            chartSummary.setVisibility(View.GONE);
            return;
        }

        chartSummary.setVisibility(View.VISIBLE);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.parseColor("#4CAF50"), Color.parseColor("#F44336")});
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        chartSummary.setData(data);
        chartSummary.getDescription().setEnabled(false);
        chartSummary.getLegend().setEnabled(true);
        chartSummary.setEntryLabelColor(Color.BLACK);
        chartSummary.setHoleRadius(40f);
        chartSummary.setTransparentCircleRadius(45f);
        chartSummary.animateY(1000);
        chartSummary.invalidate();
    }

    private void showEmptyView() {
        recyclerTransactions.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    private void hideEmptyView() {
        recyclerTransactions.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }
}