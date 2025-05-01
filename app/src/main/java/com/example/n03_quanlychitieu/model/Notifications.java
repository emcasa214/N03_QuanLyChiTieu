package com.example.n03_quanlychitieu.model;

public class Notifications {
    private String notification_id;
    private String content;
    private boolean is_read;
    private String create_at;
    private String notification_type;
    private String user_id;
    public Notifications() {
    }
    public Notifications(String notification_id, String content, boolean is_read, String create_at, String notification_type, String user_id) {
        this.notification_id = notification_id;
        this.content = content;
        this.is_read = is_read;
        this.create_at = create_at;
        this.notification_type = notification_type;
        this.user_id = user_id;
    }

    public String getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(String notification_id) {
        this.notification_id = notification_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
    }

    public String getCreate_at() {
        return create_at;
    }

    public void setCreate_at(String create_at) {
        this.create_at = create_at;
    }

    public String getNotification_type() {
        return notification_type;
    }

    public void setNotification_type(String notification_type) {
        this.notification_type = notification_type;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "Notifications{" +
                "notification_id='" + notification_id + '\'' +
                ", content='" + content + '\'' +
                ", is_read=" + is_read +
                ", create_at='" + create_at + '\'' +
                ", notification_type='" + notification_type + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }
}
