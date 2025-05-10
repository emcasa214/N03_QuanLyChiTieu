package com.example.n03_quanlychitieu.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.ui.category.AddCategoryActivity;
import com.google.android.material.navigation.NavigationView;
import com.example.n03_quanlychitieu.ui.income.ViewIncomeActivity;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        khoitao();
        toggle();
        menuClick();
        //onBackPressed();
    }

    /**
     * Khởi tạo (khoitao) – ánh xạ các view từ XML sang Java và set Toolbar làm ActionBar
     */
    public void khoitao(){
        drawerLayout = findViewById(R.id.drawmain);
        toolbar = findViewById(R.id.toolbar);
        navView = findViewById(R.id.navigation_view);
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
        else if (itemId == R.id.nav_view_income) { // Thêm xử lý cho "Xem thu nhập"
            startActivity(new Intent(this, ViewIncomeActivity.class));
        }
//        else if (itemId == R.id.nav_settings) {
//            startActivity(new Intent(this, SettingsActivity.class));
//        } else if (itemId == R.id.nav_logout) {
//            logout();
//        }

    }

    /**
     * onBackPressed() – ghi đè để nếu drawer đang mở thì đóng drawer,
     *                ngược lại mới thực hiện hành vi back mặc định
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}