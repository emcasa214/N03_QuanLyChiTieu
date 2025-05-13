package com.example.n03_quanlychitieu.ui.sign;

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
import com.example.n03_quanlychitieu.model.Notifications;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class notification_user extends AppCompatActivity {
//    private RecyclerView recyclerView;
//    private NotificationAdapter adapter;
//    private List<Notifications> notificationsList = new ArrayList<>();
//    private TabLayout tabLayout;
//    private FloatingActionButton fabMarkAllRead;
//    private TextView badgeUnread;
//    private ImageButton btnSearch;
//    private View emtyView;
//    private NotificationDAO notificationDAO;
//    private String currentUserId = "user1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

//        DatabaseHelper dbHelper = new DatabaseHelper(this);
//        notificationDAO = new NotificationDAO(dbHelper.getWritableDatabase());

//        recyclerView = findViewById(R.id.recycler_notifications);
//        tabLayout = findViewById(R.id.tab_layout);
//        fabMarkAllRead = findViewById(R.id.fab_mark_all_read);
//        badgeUnread = findViewById(R.id.badge_unread);
//        btnSearch = findViewById(R.id.btn_search);
//        emtyView = findViewById(R.id.empty_view);

        // thiết lập recycleview
//        setupRecyclerView();
//
//        // Thiết lập tablayout
//        setupTabLayout();
//
//        // Tai dữ liệu ban đầu
//        setupEvents();


    }

//    private void setupRecyclerView() {
//        adapter = new NotificationAdapter(this, notificationsList, notification -> {
//            // xử lý khi click vào thông báo
//            if (!notification.isIs_read()) {
//                notificationDAO.markAsRead(notification.getNotification_id());
//                loadNotifications(false); // refresh danh sách
//            }
//        });
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(adapter);
//    }
//
//    private void setupTabLayout() {
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                switch (tab.getPosition()) {
//                    case 0:
//                        loadNotifications(false);
//                        break;
//                    case 1: // Chưa đọc
//                        loadUnreadNotifications();
//                        break;
//                    case 2: // Cảnh báo
//                        loadWarningNotifications();
//                        break;
//                }
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });
//    }
//
//    private void setupEvents() {
//        fabMarkAllRead.setOnClickListener(v -> {
//            notificationDAO.markAllRead(currentUserId);
//            loadNotifications(false);
//            updateUnreadBadge();
//        });
//
//        btnSearch.setOnClickListener(v -> {
//
//        });
//    }
//
//    private void loadNotifications(boolean onlyUnread) {
//        List<Notifications> notifications;
//        if(onlyUnread) {
//            notifications = notificationDAO.getUnreadNotifications(currentUserId);
//        }else {
//            notifications = notificationDAO.getNotificationsByUser(currentUserId);
//        }
//        updateNotificationList(notifications);
//        updateUnreadBadge();
//    }
//
//    private void loadUnreadNotifications() {
//        List<Notifications> notifications = notificationDAO.getUnreadNotifications(currentUserId);
//        updateNotificationList(notifications);
//    }
//
//    private void loadWarningNotifications() {
//        List<Notifications> allNotifications = notificationDAO.getNotificationsByUser(currentUserId);
//        List<Notifications> warningNotifications = new ArrayList<>();
//        for (Notifications notification : allNotifications) {
//            if ("warning".equals(notification.getNotification_type())) {
//                warningNotifications.add(notification);
//            }
//        }
//        updateNotificationList(warningNotifications);
//    }
//
//    private void updateNotificationList(List<Notifications> notifications) {
//        notificationsList.clear();
//        notificationsList.addAll(notifications);
//        adapter.notifyDataSetChanged();
//
//        // Hiển thị empty view nếu không có thông báo
//        if (notificationsList.isEmpty()) {
//            emtyView.setVisibility(View.VISIBLE);
//            recyclerView.setVisibility(View.GONE);
//        } else {
//            emtyView.setVisibility(View.GONE);
//            recyclerView.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private void updateUnreadBadge() {
//        int unreadCount = notificationDAO.getUnreadCount(currentUserId);
//        if (unreadCount > 0) {
//            badgeUnread.setText(String.valueOf(unreadCount));
//            badgeUnread.setVisibility(View.VISIBLE);
//        } else {
//            badgeUnread.setVisibility(View.GONE);
//        }
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        loadNotifications(false);
//        updateUnreadBadge();
//    }
}
