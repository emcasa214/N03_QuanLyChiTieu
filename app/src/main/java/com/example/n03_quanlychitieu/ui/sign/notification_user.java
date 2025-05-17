package com.example.n03_quanlychitieu.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.adapter.NotificationAdapter;
import com.example.n03_quanlychitieu.dao.NotificationDAO;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.model.Budgets;
import com.example.n03_quanlychitieu.model.Notifications;
import com.example.n03_quanlychitieu.utils.AuthenticationManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class notification_user extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notifications> notificationsList = new ArrayList<>();
    private TabLayout tabLayout;
    private View emtyView;
    private NotificationDAO notificationDAO;
    private String currentUserId;
    private AuthenticationManager authManager;
    private TextView test_in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        notificationDAO = new NotificationDAO(dbHelper.getWritableDatabase());
        recyclerView = findViewById(R.id.recycler_notifications);
        tabLayout = findViewById(R.id.tab_layout);
        emtyView = findViewById(R.id.empty_view);
        test_in = findViewById(R.id.testOin);

        authManager = AuthenticationManager.getInstance(notification_user.this);

        // Kiểm tra người dùng đăng nhập
        if (authManager.isUserLoggedIn()) {
            currentUserId = authManager.getCurrentUser().getUser_id();
        } else {
            // CHuyển đến màn hình đăng nhập
            startActivity(new Intent(this, LogIn.class));
            finish();
            return;
        }

        // thiết lập recycleview
        setupRecyclerView();

        // Thiết lập tablayout
        setupTabLayout();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this, notificationsList, notification -> {
            // xử lý khi click vào thông báo
            if (!notification.isIs_read()) {
                notificationDAO.markAsRead(notification.getNotification_id());
                loadNotifications(false);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadNotifications(false);
                        break;
                    case 1: // Chưa đọc
                        loadUnreadNotifications();
                        break;
                    case 2: // Cảnh báo
                        loadWarningNotifications();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    private void loadNotifications(boolean onlyUnread) {
        List<Notifications> notifications;
        if(onlyUnread) {
            notifications = notificationDAO.getUnreadNotifications(currentUserId);
        }else {
            notifications = notificationDAO.getNotificationsByUser(currentUserId);
        }
        updateNotificationList(notifications);
    }

    private void loadUnreadNotifications() {
        List<Notifications> notifications = notificationDAO.getUnreadNotifications(currentUserId);
        updateNotificationList(notifications);
    }

    private void loadWarningNotifications() {
        List<Notifications> allNotifications = notificationDAO.getNotificationsByUser(currentUserId);
        List<Notifications> warningNotifications = new ArrayList<>();
        for (Notifications notification : allNotifications) {
            if ("warn".equals(notification.getNotification_type())) {
                warningNotifications.add(notification);
            }
        }
        updateNotificationList(warningNotifications);
    }

    private void updateNotificationList(List<Notifications> notifications) {
        notificationsList.clear();
        notificationsList.addAll(notifications);

        adapter.updateData(notifications);

        // Hiển thị empty view nếu không có thông báo
        if (notificationsList.isEmpty()) {
            emtyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emtyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications(false);
    }
}
