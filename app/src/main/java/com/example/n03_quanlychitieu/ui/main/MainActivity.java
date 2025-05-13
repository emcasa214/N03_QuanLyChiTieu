package com.example.n03_quanlychitieu.ui.main;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.adapter.ThuChiAdapter;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.model.Budgets;
import com.example.n03_quanlychitieu.model.Expenses;
import com.example.n03_quanlychitieu.model.Incomes;
import com.example.n03_quanlychitieu.model.Notifications;
import com.example.n03_quanlychitieu.model.Users;
import com.example.n03_quanlychitieu.ui.category.AddCategoryActivity;
import com.example.n03_quanlychitieu.ui.sign.LogIn;
import com.example.n03_quanlychitieu.ui.sign.ReportTransaction;
import com.example.n03_quanlychitieu.ui.sign.SetBudgets;
import com.example.n03_quanlychitieu.ui.sign.notification_user;
import com.google.android.material.navigation.NavigationView;
import com.example.n03_quanlychitieu.ui.income.ViewIncomeActivity;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navView;
    RecyclerView rvThuChi;
    ArrayList<String> listThuChistr;
    ThuChiAdapter adapter;
    ImageButton bell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        khoitao();
        toggle();
        menuClick();
        setupRecyclerView();
        takeListView();
//        onBackPressed();
        navigateNotification();

    }

    /**
     * Khởi tạo (khoitao) – ánh xạ các view từ XML sang Java và set Toolbar làm ActionBar
     */
    public void khoitao(){
        drawerLayout = findViewById(R.id.drawmain);
        toolbar = findViewById(R.id.toolbar);
        navView = findViewById(R.id.navigation_view);
        rvThuChi = findViewById(R.id.rvThuChi);
        bell = findViewById(R.id.notification_button);
        setSupportActionBar(toolbar);

    }
    /**
     * toggle() – thêm nút “hamburger” - nút 3 gạch ngang ngang í :)), vào toolbar để điều khiển đóng/mở drawer
     */
    public void toggle(){
        // Thêm nút toggle (3 gạch)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }
    /**
     * menuClick() – Hàm gọi hàm xử lý sự kiện khi người dùng chọn một mục trong NavigationView
     */
    public void menuClick(){
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawer(GravityCompat.START);
                int id = item.getItemId();
                handleNavigation(id);
                return true;
            }
        });
    }
    /**
     * handleNavigation() – xử lý sự kiện khi người dùng chọn một mục trong NavigationView, làm nút nào thì xử lý nút đó vô đây
     */
    private void handleNavigation(int itemId) {
        if (itemId == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
        }
        else if(itemId == R.id.nav_view_categories){
            startActivity(new Intent(this, AddCategoryActivity.class));
        } else if (itemId == R.id.nav_statistics) {
            startActivity(new Intent(this, ReportTransaction.class));
        } else if(itemId == R.id.nav_budget) {
            startActivity(new Intent(this, SetBudgets.class));
        }
    }
    //hàm xử lý hiển thị 10 thu chi gần nhất vào Danh sách thu chi - 12/5/2025
    private void setupRecyclerView() {
        listThuChistr = new ArrayList<>();
        adapter = new ThuChiAdapter(listThuChistr);
        rvThuChi.setLayoutManager(new LinearLayoutManager(this));
        rvThuChi.setAdapter(adapter);
    }
    private void takeListView() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ArrayList<String> temp = new ArrayList<>();

        // Lấy dữ liệu Incomes
        Cursor cIn = db.rawQuery(
                "SELECT description, amount, create_at FROM Incomes", null);
        while (cIn.moveToNext()) {
            String d = cIn.getString(0);
            double a = cIn.getDouble(1);
            String date = cIn.getString(2);
//            temp.add("Thu: " + d + " - " + a + " VND - " + date);
            temp.add(date + " - " + d + " - " + a + " VND");
        }
        cIn.close();

        // Lấy dữ liệu Expenses
        Cursor cEx = db.rawQuery(
                "SELECT description, amount, create_at FROM Expenses", null);
        while (cEx.moveToNext()) {
            String d = cEx.getString(0);
            double a = cEx.getDouble(1);
            String date = cEx.getString(2);
//            temp.add("Chi: " + d + " - " + a + " VND - " + date);
            temp.add(date + " - " + d + " - " + a + " VND");
        }
        cEx.close();

        // 1. Khởi tạo formatter
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // 2. Sort giảm dần theo ngày
        Collections.sort(temp, (o1, o2) -> {
            try {
                // tách ra mảng ["2025-05-15", "Bán hàng online", "1500000.0 VND"]
                String[] parts1 = o1.split(" - ");
                String[] parts2 = o2.split(" - ");

                // phần 0 luôn là chuỗi ngày yyyy-MM-dd
                Date d1 = sdf.parse(parts1[0].trim());
                Date d2 = sdf.parse(parts2[0].trim());

                // mới nhất trước
                return d2.compareTo(d1);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });

        // 3. Lấy 10 mục đầu
        ArrayList<String> top10 = new ArrayList<>(
                temp.subList(0, Math.min(10, temp.size()))
        );
        // Update dữ liệu cho RecyclerView
        listThuChistr.clear();
        listThuChistr.addAll(top10);
        adapter.notifyDataSetChanged();
    }


    /**
     * onBackPressed() – ghi đè để nếu drawer đang mở thì đóng drawer,
     *                ngược lại mới thực hiện hành vi back mặc định
     */
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finishAffinity(); // Đóng hoàn toàn ứng dụng
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Nhấn BACK lần nữa để thoát", Toast.LENGTH_SHORT).show();

        // Reset cờ sau 3 giây
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                doubleBackToExitPressedOnce = false, 3000);
    }

    /**
     * Notification
     */
    private void navigateNotification() {
        bell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, notification_user.class);
                startActivity(intent);
            }
        });
    }
}