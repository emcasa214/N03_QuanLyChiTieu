package com.example.n03_quanlychitieu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.model.Notifications;

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
        this.notificationList = notificationList;
        this.listener = listener;
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

        // Thiết lập icon dựa trên loại thông báo
        int iconRes = R.drawable.ic_empty_notifications;
        if ("warning".equals(notification.getNotification_type())) {
            iconRes = R.drawable.ic_warning;
        }
        holder.ivIcon.setImageResource(iconRes);

        holder.tvTitle.setText(notification.getContent());
        holder.tvMessage.setText(notification.getContent());
        holder.tvTime.setText(notification.getCreate_at());

        // Hiển thị indicator chưa đọc
        holder.viewUnreadIndicator.setVisibility(notification.isIs_read() ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvMessage;
        TextView tvTime;
        View viewUnreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_notification_icon);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            viewUnreadIndicator = itemView.findViewById(R.id.view_unread_indicator);
        }
    }
}