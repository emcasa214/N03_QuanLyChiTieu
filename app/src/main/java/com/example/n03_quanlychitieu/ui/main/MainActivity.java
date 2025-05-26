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
import com.example.n03_quanlychitieu.dao.NotificationDAO;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.model.Notifications;
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
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    DrawerLayout drawerLayout;
    SearchView searchView;
    Toolbar toolbar;
    NavigationView navView;
    RecyclerView rvThuChi;
    ArrayList<ThuChiAdapter.ThuChiItem> listThuChi;
    ThuChiAdapter adapter;
    ImageButton bell;
    ImageButton btnUser;
    AuthenticationManager auth;
    private String userId;
    private TextView badgeUnread;
    private NotificationDAO notificationDAO;
    private Notifications tb;
    private TextView txtSoTienChi, txtSoTienCon, txtSoTienThu;

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
        updateFinancialData();
    }

    public void khoitao() {
        drawerLayout = findViewById(R.id.drawmain);
        toolbar = findViewById(R.id.toolbar);
        navView = findViewById(R.id.navigation_view);
        rvThuChi = findViewById(R.id.rvThuChi);
        bell = findViewById(R.id.notification_button);
        btnUser = findViewById(R.id.btnUser);
        searchView = findViewById(R.id.search_view);
        badgeUnread = findViewById(R.id.badge_unread);
        txtSoTienChi = findViewById(R.id.txtSoTienChi);
        txtSoTienCon = findViewById(R.id.txtSoTienCon);
        txtSoTienThu = findViewById(R.id.txtSoTienThu);
        setSupportActionBar(toolbar);
        tb = new Notifications(UUID.randomUUID().toString(), "Bạn đã đăng nhập vào ứng dụng!", false, null, "info", auth.getCurrentUser().getUser_id());

        // Sử dụng instance dbHelper đã khởi tạo ở cấp class
        notificationDAO = new NotificationDAO(dbHelper.getWritableDatabase());
        notificationDAO.insert(tb);
    }

    public void setClickUser() {
        btnUser.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }

    public void toggle() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    public void menuClick() {
        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();
            handleNavigation(id);
            return true;
        });
    }

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

        if (intent != null && userId != null) {
            intent.putExtra("userId", userId);
            startActivity(intent);
        } else if (intent != null) {
            Toast.makeText(this, "Lỗi: User ID không tồn tại", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogIn.class));
            finish();
        }
    }

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
            ArrayList<ThuChiAdapter.ThuChiItem> temp = new ArrayList<>();

            // Tìm kiếm trong Incomes
            Cursor cIn = db.rawQuery(
                    "SELECT description, amount, create_at FROM Incomes WHERE description LIKE ? AND user_id = ?",
                    new String[]{"%" + keyword + "%", userId});
            if (cIn != null) {
                while (cIn.moveToNext()) {
                    String description = cIn.getString(0);
                    double amount = cIn.getDouble(1);
                    String date = cIn.getString(2);
                    long amountInt = (long) amount;
                    temp.add(new ThuChiAdapter.ThuChiItem(date, description, String.format("%,d", amountInt) + " VND", "income"));
                }
                cIn.close();
            }

            // Tìm kiếm trong Expenses
            Cursor cEx = db.rawQuery(
                    "SELECT description, amount, create_at FROM Expenses WHERE description LIKE ? AND user_id = ?",
                    new String[]{"%" + keyword + "%", userId});
            if (cEx != null) {
                while (cEx.moveToNext()) {
                    String description = cEx.getString(0);
                    double amount = cEx.getDouble(1);
                    String date = cEx.getString(2);
                    long amountInt = (long) amount;
                    temp.add(new ThuChiAdapter.ThuChiItem(date, description, String.format("%,d", amountInt) + " VND", "expense"));
                }
                cEx.close();
            }

            // Sắp xếp theo thời gian giảm dần
            Collections.sort(temp, (item1, item2) -> item2.getDate().compareTo(item1.getDate()));

            runOnUiThread(() -> {
                listThuChi.clear();
                listThuChi.addAll(temp);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void setupRecyclerView() {
        listThuChi = new ArrayList<>();
        adapter = new ThuChiAdapter(listThuChi);
        rvThuChi.setLayoutManager(new LinearLayoutManager(this));
        rvThuChi.setAdapter(adapter);
    }

    private void takeListView() {
        new Thread(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            ArrayList<ThuChiAdapter.ThuChiItem> temp = new ArrayList<>();

            // Lấy dữ liệu từ Incomes
            Cursor cIn = db.rawQuery(
                    "SELECT description, amount, create_at FROM Incomes WHERE user_id = ?",
                    new String[]{userId});
            if (cIn != null) {
                while (cIn.moveToNext()) {
                    String description = cIn.getString(0);
                    double amount = cIn.getDouble(1);
                    String date = cIn.getString(2);
                    long amountInt = (long) amount;
                    temp.add(new ThuChiAdapter.ThuChiItem(date, description, String.format("%,d", amountInt) + " VND", "income"));
                }
                cIn.close();
            }

            // Lấy dữ liệu từ Expenses
            Cursor cEx = db.rawQuery(
                    "SELECT description, amount, create_at FROM Expenses WHERE user_id = ?",
                    new String[]{userId});
            if (cEx != null) {
                while (cEx.moveToNext()) {
                    String description = cEx.getString(0);
                    double amount = cEx.getDouble(1);
                    String date = cEx.getString(2);
                    long amountInt = (long) amount;
                    temp.add(new ThuChiAdapter.ThuChiItem(date, description, String.format("%,d", amountInt) + " VND", "expense"));
                }
                cEx.close();
            }

            // Sắp xếp theo thời gian giảm dần
            Collections.sort(temp, (item1, item2) -> item2.getDate().compareTo(item1.getDate()));

            runOnUiThread(() -> {
                listThuChi.clear();
                listThuChi.addAll(temp);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

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

    private void navigateNotification() {
        bell.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, notification_user.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }

    private void updateUnreadBadge() {
        int unreadCount = notificationDAO.getUnreadCount(auth.getCurrentUser().getUser_id());
        if (unreadCount > 0) {
            badgeUnread.setText(String.valueOf(unreadCount));
            badgeUnread.setVisibility(View.VISIBLE);
        } else {
            badgeUnread.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUnreadBadge();
        updateFinancialData();
    }

    private void updateFinancialData() {
        new Thread(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            double totalExpense;
            double totalIncome;

            // Tính tổng chi (expense)
            Cursor cEx = db.rawQuery(
                    "SELECT SUM(amount) FROM Expenses WHERE user_id = ?",
                    new String[]{userId});
            if (cEx != null && cEx.moveToFirst()) {
                totalExpense = cEx.getDouble(0);
            } else {
                totalExpense = 0.0;
            }
            if (cEx != null) cEx.close();

            // Tính tổng thu (income)
            Cursor cIn = db.rawQuery(
                    "SELECT SUM(amount) FROM Incomes WHERE user_id = ?",
                    new String[]{userId});
            if (cIn != null && cIn.moveToFirst()) {
                totalIncome = cIn.getDouble(0);
            } else {
                totalIncome = 0.0;
            }
            if (cIn != null) cIn.close();

            // Tính còn lại
            double conLai = totalIncome - totalExpense;

            // Cập nhật giao diện trên main thread
            runOnUiThread(() -> {
                txtSoTienChi.setText(String.format("%,d", (long) totalExpense) + " VND");
                txtSoTienCon.setText(String.format("%,d", (long) conLai) + " VND");
                txtSoTienThu.setText(String.format("%,d", (long) totalIncome) + " VND");
            });
        }).start();
    }
}