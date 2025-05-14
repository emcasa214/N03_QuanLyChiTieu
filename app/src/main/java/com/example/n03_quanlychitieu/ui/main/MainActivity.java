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
import com.example.n03_quanlychitieu.model.Budgets;
import com.example.n03_quanlychitieu.model.Expenses;
import com.example.n03_quanlychitieu.model.Incomes;
import com.example.n03_quanlychitieu.model.Notifications;
import com.example.n03_quanlychitieu.model.Users;
import com.example.n03_quanlychitieu.ui.category.AddCategoryActivity;
import com.example.n03_quanlychitieu.ui.user.UserProfileActivity;
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

    DatabaseHelper dbHelper;

    DrawerLayout drawerLayout;
    SearchView searchView;
    Toolbar toolbar;
    NavigationView navView;
    RecyclerView rvThuChi;
    ArrayList<String> listThuChistr;
    ThuChiAdapter adapter;

    ImageButton btnUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "MainActivity started");
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
//        dbHelper.getReadableDatabase();

//        dbHelper.insertSampleData();
        khoitao();
        toggle();
        menuClick();
        setupRecyclerView();
        takeListView();
        setClickUser();
        setupSearch();
//        onBackPressed();
    }

    public void khoitao(){
        drawerLayout = findViewById(R.id.drawmain);
        toolbar = findViewById(R.id.toolbar);
        navView = findViewById(R.id.navigation_view);
        rvThuChi = findViewById(R.id.rvThuChi);
        btnUser = findViewById(R.id.btnUser);
        searchView = findViewById(R.id.search_view);
        setSupportActionBar(toolbar);
    }

    //set up click cho icon user sang màn hình thông tin cá nhân
    public void setClickUser(){
        btnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });
    }

     // toggle() – thêm nút “hamburger” - nút 3 gạch ngang ngang í :)), vào toolbar để điều khiển đóng/mở drawer
    public void toggle(){
        // Thêm nút toggle (3 gạch)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }
    // menuClick() – Hàm gọi hàm xử lý sự kiện khi người dùng chọn một mục trong NavigationView
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

     // handleNavigation() – xử lý sự kiện khi người dùng chọn một mục trong NavigationView, làm nút nào thì xử lý nút đó vô đây
    private void handleNavigation(int itemId) {
        if (itemId == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
        }
        else if(itemId == R.id.nav_view_categories){
            startActivity(new Intent(this, AddCategoryActivity.class));
        }

    }
//    set up cho ô tìm kiếm
    public void setupSearch(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchListView(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    takeListView(); // Hiển thị lại toàn bộ khi xóa tìm kiếm
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
                    "SELECT description, amount, create_at FROM Incomes WHERE description LIKE ?",
                    new String[]{"%" + keyword + "%"});
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
                    "SELECT description, amount, create_at FROM Expenses WHERE description LIKE ?",
                    new String[]{"%" + keyword + "%"});
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
//    //hàm xử lý hiển thị 10 thu chi gần nhất vào Danh sách thu chi - 12/5/2025
    private void setupRecyclerView() {
        listThuChistr = new ArrayList<>();
        adapter = new ThuChiAdapter(listThuChistr);
        rvThuChi.setLayoutManager(new LinearLayoutManager(this));
        rvThuChi.setAdapter(adapter);
    }
    private void takeListView() {
        new Thread(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            ArrayList<String> temp = new ArrayList<>();

            // Lấy dữ liệu và sắp xếp theo thời gian giảm dần (mới nhất trước)
            Cursor cIn = db.rawQuery(
                    "SELECT description, amount, create_at FROM Incomes", null);
            while (cIn.moveToNext()) {
                String d = cIn.getString(0);
                double a = cIn.getDouble(1);
                String date = cIn.getString(2);
                long amountInt = (long) a;
                temp.add(date + " - " + d + " - " + String.format("%,d", amountInt) + " VND");
            }
            cIn.close();

            Cursor cEx = db.rawQuery(
                    "SELECT description, amount, create_at FROM Expenses", null);
            while (cEx.moveToNext()) {
                String d = cEx.getString(0);
                double a = cEx.getDouble(1);
                String date = cEx.getString(2);
                long amountInt = (long) a;
                temp.add(date + " - " + d + " - " + String.format("%,d", amountInt) + " VND");
            }
            cEx.close();

            // Sắp xếp theo thời gian giảm dần (giả sử create_at có định dạng yyyy-MM-dd hoặc yyyy-MM-dd HH:mm:ss)
            Collections.sort(temp, (s1, s2) -> {
                String date1 = s1.split(" - ")[0];
                String date2 = s2.split(" - ")[0];
                return date2.compareTo(date1); // giảm dần
            });

            // Cập nhật RecyclerView
            runOnUiThread(() -> {
                listThuChistr.clear();
                listThuChistr.addAll(temp);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }


//    @Override
//    public void onBackPressed() {
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }
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


}