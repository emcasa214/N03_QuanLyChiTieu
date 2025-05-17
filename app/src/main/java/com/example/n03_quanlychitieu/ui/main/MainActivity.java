package com.example.n03_quanlychitieu.ui.main;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
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
import com.example.n03_quanlychitieu.model.Users;
import com.example.n03_quanlychitieu.ui.category.AddCategoryActivity;
import com.example.n03_quanlychitieu.ui.expense.ViewExpenseActivity;
import com.example.n03_quanlychitieu.ui.income.ViewIncomeActivity;
import com.example.n03_quanlychitieu.ui.sign.LogIn;
import com.example.n03_quanlychitieu.ui.sign.ReportTransaction;
import com.example.n03_quanlychitieu.ui.sign.SetBudgets;
import com.example.n03_quanlychitieu.ui.sign.notification_user;
import com.example.n03_quanlychitieu.ui.user.UserProfileActivity;
import com.example.n03_quanlychitieu.utils.AuthenticationManager;
import com.google.android.material.navigation.NavigationView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    DrawerLayout drawerLayout;
    SearchView searchView;
    Toolbar toolbar;
    NavigationView navView;
    RecyclerView rvThuChi;
    ArrayList<String> listThuChistr;
    ThuChiAdapter adapter;
    ImageButton bell;
    ImageButton btnUser;
    AuthenticationManager auth;
    private String userId;
    private TextView test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("MainActivity", "MainActivity started at " + LocalDateTime.now());
        }
        setContentView(R.layout.activity_main);

        // Khởi tạo AuthenticationManager và DatabaseHelper
        auth = AuthenticationManager.getInstance(this);
        dbHelper = new DatabaseHelper(this);

        // Kiểm tra trạng thái đăng nhập
        if (!auth.isUserLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogIn.class));
            finish();
            return;
        }

        // Lấy thông tin người dùng hiện tại
        Users currentUser = auth.getCurrentUser();
        userId = currentUser != null ? currentUser.getUser_id() : null;
        if (userId == null) {
            Toast.makeText(this, "User ID is missing. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            auth.logout();
            startActivity(new Intent(this, LogIn.class));
            finish();
            return;
        }

        // Khởi tạo giao diện và các thành phần khác
        khoitao();
        toggle();
        menuClick();
        setupRecyclerView();
        takeListView();
        setClickUser();
        setupSearch();
        navigateNotification();
    }

    public void khoitao() {
        drawerLayout = findViewById(R.id.drawmain);
        toolbar = findViewById(R.id.toolbar);
        navView = findViewById(R.id.navigation_view);
        rvThuChi = findViewById(R.id.rvThuChi);
        bell = findViewById(R.id.notification_button);
        btnUser = findViewById(R.id.btnUser);
        searchView = findViewById(R.id.search_view);
        test = findViewById(R.id.section_title3);
        setSupportActionBar(toolbar);
    }

    // Chuyển đến màn hình thông tin cá nhân
    public void setClickUser() {
        btnUser.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }

    // Thêm nút hamburger để mở/đóng drawer
    public void toggle() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    // Xử lý sự kiện click trên NavigationView
    public void menuClick() {
        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();
            handleNavigation(id);
            return true;
        });
    }

    // Xử lý điều hướng khi chọn mục trong NavigationView
    private void handleNavigation(int itemId) {
        Intent intent = null;
        if (itemId == R.id.nav_home) {
            intent = new Intent(this, MainActivity.class);
        } else if (itemId == R.id.nav_view_categories) {
            intent = new Intent(this, AddCategoryActivity.class);
        } else if (itemId == R.id.nav_statistics) {
            intent = new Intent(this, ReportTransaction.class);
        } else if (itemId == R.id.nav_budget) {
            intent = new Intent(this, SetBudgets.class);
        } else if (itemId == R.id.nav_view_income) {
            intent = new Intent(this, ViewIncomeActivity.class);
        } else if (itemId == R.id.nav_view_expense) {
            intent = new Intent(this, ViewExpenseActivity.class);
        }

        // Truyền userId cho tất cả các Intent
        if (intent != null && userId != null) {
            intent.putExtra("userId", userId);
            startActivity(intent);
        } else if (intent != null) {
            Toast.makeText(this, "Lỗi: User ID không tồn tại", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogIn.class));
            finish();
        }
    }

    // Cài đặt ô tìm kiếm
    public void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchListView(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    takeListView();
                } else {
                    searchListView(newText);
                }
                return true;
            }
        });
    }

    private void searchListView(String keyword) {
        new Thread(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            ArrayList<String> temp = new ArrayList<>();

            // Tìm kiếm trong Incomes
            Cursor cIn = db.rawQuery(
                    "SELECT description, amount, create_at FROM Incomes WHERE description LIKE ? AND user_id = ?",
                    new String[]{"%" + keyword + "%", userId});
            while (cIn.moveToNext()) {
                String d = cIn.getString(0);
                double a = cIn.getDouble(1);
                String date = cIn.getString(2);
                long amountInt = (long) a;
                temp.add(date + " - " + d + " - " + String.format("%,d", amountInt) + " VND");
            }
            cIn.close();

            // Tìm kiếm trong Expenses
            Cursor cEx = db.rawQuery(
                    "SELECT description, amount, create_at FROM Expenses WHERE description LIKE ? AND user_id = ?",
                    new String[]{"%" + keyword + "%", userId});
            while (cEx.moveToNext()) {
                String d = cEx.getString(0);
                double a = cEx.getDouble(1);
                String date = cEx.getString(2);
                long amountInt = (long) a;
                temp.add(date + " - " + d + " - " + String.format("%,d", amountInt) + " VND");
            }
            cEx.close();

            // Sắp xếp theo thời gian giảm dần
            Collections.sort(temp, (s1, s2) -> {
                String date1 = s1.split(" - ")[0];
                String date2 = s2.split(" - ")[0];
                return date2.compareTo(date1);
            });

            runOnUiThread(() -> {
                listThuChistr.clear();
                listThuChistr.addAll(temp);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    // Cài đặt RecyclerView
    private void setupRecyclerView() {
        listThuChistr = new ArrayList<>();
        adapter = new ThuChiAdapter(listThuChistr);
        rvThuChi.setLayoutManager(new LinearLayoutManager(this));
        rvThuChi.setAdapter(adapter);
    }

    // Lấy danh sách thu chi
    private void takeListView() {
        new Thread(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            ArrayList<String> temp = new ArrayList<>();

            // Lấy dữ liệu từ Incomes
            Cursor cIn = db.rawQuery(
                    "SELECT description, amount, create_at FROM Incomes WHERE user_id = ?",
                    new String[]{userId});
            while (cIn.moveToNext()) {
                String d = cIn.getString(0);
                double a = cIn.getDouble(1);
                String date = cIn.getString(2);
                long amountInt = (long) a;
                temp.add(date + " - " + d + " - " + String.format("%,d", amountInt) + " VND");
            }
            cIn.close();

            // Lấy dữ liệu từ Expenses
            Cursor cEx = db.rawQuery(
                    "SELECT description, amount, create_at FROM Expenses WHERE user_id = ?",
                    new String[]{userId});
            while (cEx.moveToNext()) {
                String d = cEx.getString(0);
                double a = cEx.getDouble(1);
                String date = cEx.getString(2);
                long amountInt = (long) a;
                temp.add(date + " - " + d + " - " + String.format("%,d", amountInt) + " VND");
            }
            cEx.close();

            // Sắp xếp theo thời gian giảm dần
            Collections.sort(temp, (s1, s2) -> {
                String date1 = s1.split(" - ")[0];
                String date2 = s2.split(" - ")[0];
                return date2.compareTo(date1);
            });

            runOnUiThread(() -> {
                listThuChistr.clear();
                listThuChistr.addAll(temp);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    // Xử lý nút Back
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Nhấn BACK lần nữa để thoát", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 3000);
    }

    // Điều hướng đến màn hình thông báo
    private void navigateNotification() {
        bell.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, notification_user.class);
            intent.putExtra("userId", userId);
            startActivity(intent);

        });
    }
}