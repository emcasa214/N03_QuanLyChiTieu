package com.example.n03_quanlychitieu.ui.sign;

import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.dao.ReportDAO;
import com.example.n03_quanlychitieu.db.DatabaseHelper;

import com.example.n03_quanlychitieu.utils.AuthenticationManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportTransaction extends AppCompatActivity {
    private TextView tvTotalIncome, tvTotalExpense, tvBalance;
    private PieChart chartSummary;
    private ReportDAO reportDAO;
    private AuthenticationManager authManager;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private Date currentStartDate, currentEndDate;
    private ImageButton btn_back, btn_download;

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
        btn_back = findViewById(R.id.btn_back_report);
        btn_download = findViewById(R.id.btn_download);

        btn_download.setOnClickListener(v -> downloadReport());

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

    private void downloadReport() {
        // lay du lieu tu cac TextView hien thi giao dien
        String totalIncome = tvTotalIncome.getText().toString();
        String totalExpense = tvTotalExpense.getText().toString();
        String balance = tvBalance.getText().toString();

        // Tao tren file voi dinh dang: BaoCao_ngay_thang_nam.pdf
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
        String fileName = "BaoCao_" + sdf.format(new Date()) + ".pdf";

        // Tao va lưu file bao cao
        createdPdfReport(fileName, totalIncome, totalExpense, balance);

        // Thong bao cho nguoi dung
        Toast.makeText(this, "Đã tải báo cáo thành công", Toast.LENGTH_SHORT).show();
    }

    private void createdPdfReport(String fileName, String income, String expense, String balance) {
        // Tao doi tuong PdfDocument
        PdfDocument document = new PdfDocument();

        // Tao mot trang
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        // Lay Canvas de ve noi dung
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // Vẽ tiêu đề
        paint.setTextSize(24f);
        paint.setColor(Color.BLACK);
        canvas.drawText("BÁO CÁO TÀI CHÍNH", 100, 100, paint);

        // Vẽ thông tin tổng thu
        paint.setTextSize(18f);
        canvas.drawText("Tổng thu: " + income, 100, 150, paint);

        // Vẽ thông tin tổng chi
        canvas.drawText("Tổng chi: " + expense, 100, 200, paint);

        // Vẽ thông tin số dư
        canvas.drawText("Số dư: " + balance, 100, 250, paint);

        // Vẽ ngày tạo báo cáo
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        canvas.drawText("Ngày tạo: " + currentDate, 100, 300, paint);

        // Kết thúc trang
        document.finishPage(page);

        // Lưu file vào thư mục Downloads
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, fileName);

        try {
            document.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi lưu file", Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }

        // Thông báo hệ thống về file mới
        MediaScannerConnection.scanFile(
                this,
                new String[]{file.getAbsolutePath()},
                null,
                null
        );

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

        // Tạo tên file với timestamp
        String fileName = "BaoCao_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

        // Gọi PDFGenerator để tạo file

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
    }

    private void setupClickListeners() {

        // Các nút lọc thời gian
        findViewById(R.id.btn_today).setOnClickListener(v -> loadTodayData());
        findViewById(R.id.btn_week).setOnClickListener(v -> loadWeekData());
        findViewById(R.id.btn_month).setOnClickListener(v -> loadMonthData());
        findViewById(R.id.btn_custom).setOnClickListener(v -> showCustomDateDialog());
    }

    private void loadTodayData() {
        currentStartDate = reportDAO.getStartOfDay(new Date());
        currentEndDate = reportDAO.getEndOfDay(new Date());

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
        double totalIncome = reportDAO.getTotalIncome(userId, currentStartDate, currentEndDate);
        double totalExpense = reportDAO.getTotalExpense(userId, currentStartDate, currentEndDate);
        double balance = totalIncome - totalExpense;


        // Hiển thị tổng quan
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalIncome.setText(currencyFormat.format(totalIncome));
        tvTotalExpense.setText(currencyFormat.format(totalExpense));
        tvBalance.setText(currencyFormat.format(balance));

        // Vẽ biểu đồ
        setupPieChart(totalIncome, totalExpense);

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


}