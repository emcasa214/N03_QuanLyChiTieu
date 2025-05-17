package com.example.n03_quanlychitieu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.model.Budgets;
import com.example.n03_quanlychitieu.model.Notifications;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private Context context;
    private List<Notifications> notificationList;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notifications notification);
    }

    public NotificationAdapter(Context context, List<Notifications> notificationList, OnNotificationClickListener listener) {
        this.context = context;
        this.notificationList = new ArrayList<>(notificationList);
        this.listener = listener;
    }

    private static class NotificationDiffCallback extends DiffUtil.Callback {
        private final List<Notifications> oldList;
        private final List<Notifications> newList;

        public NotificationDiffCallback(List<Notifications> oldList, List<Notifications> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getNotification_id().equals(newList.get(newItemPosition).getNotification_id());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Notifications oldNoti = oldList.get(oldItemPosition);
            Notifications newNoti = newList.get(newItemPosition);
            return oldNoti.equals(newNoti);
        }
    }

    //    public void updateData(List<Notifications> newNotifications) {
//        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new NotificationDiffCallback(this.notificationList, newNotifications));
//        this.notificationList.clear();
//        this.notificationList.addAll(newNotifications);
//        diffResult.dispatchUpdatesTo(this);
//    }
    public void updateData(List<Notifications> newNotifications) {
        // Tạo bản sao để tránh tham chiếu
        List<Notifications> updatedList = new ArrayList<>(newNotifications);

        // So sánh và cập nhật từng item
        for (int i = 0; i < updatedList.size(); i++) {
            if (i < notificationList.size()) {
                if (!notificationList.get(i).equals(updatedList.get(i))) {
                    notificationList.set(i, updatedList.get(i));
                    notifyItemChanged(i); // Chỉ cập nhật item thay đổi
                }
            } else {
                notificationList.add(updatedList.get(i));
                notifyItemInserted(i); // Thông báo item mới
            }
        }

        // Xóa các item thừa (nếu có)
        if (notificationList.size() > updatedList.size()) {
            for (int i = notificationList.size() - 1; i >= updatedList.size(); i--) {
                notificationList.remove(i);
                notifyItemRemoved(i); // Thông báo item bị xóa
            }
        }
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notifications notification = notificationList.get(position);
        if (notification == null) return;

        // Thiết lập icon dựa trên loại thông báo
        int iconRes = "warning".equals(notification.getNotification_type()) ? R.drawable.ic_warning : R.drawable.ic_empty_notifications;

        holder.ivIcon.setImageResource(iconRes);

        holder.tvTitle.setText(notification.getContent());
        holder.tvTime.setText(formatTime(notification.getCreate_at()));

        // Hiển thị indicator chưa đọc với animation
        if (notification.isIs_read()) {
            holder.viewUnreadIndicator.setVisibility(View.GONE);
        } else {
            holder.viewUnreadIndicator.setVisibility(View.VISIBLE);
            holder.viewUnreadIndicator.setAlpha(1f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);

                // Thêm animation khi click
                if (!notification.isIs_read()) {
                    holder.viewUnreadIndicator.animate().alpha(0f).setDuration(300).withEndAction(() -> holder.viewUnreadIndicator.setVisibility(View.GONE)).start();
                }
            }
        });
    }

    // Thêm hàm format thời gian
    private String formatTime(String rawTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(rawTime);

            if (isToday(date)) {
                return new SimpleDateFormat("HH:mm").format(date);
            } else {
                return new SimpleDateFormat("dd/MM").format(date);
            }
        } catch (Exception e) {
            return rawTime;
        }
    }

    private boolean isToday(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTime(date);
        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) && today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvTime;
        View viewUnreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_notification_icon);
            tvTitle = itemView.findViewById(R.id.tv_notification_content);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            viewUnreadIndicator = itemView.findViewById(R.id.view_unread_indicator);
        }
    }
}