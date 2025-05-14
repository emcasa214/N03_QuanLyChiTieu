package com.example.n03_quanlychitieu.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.n03_quanlychitieu.db.DatabaseContract;
import com.example.n03_quanlychitieu.model.Notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class NotificationDAO {
    private SQLiteDatabase db;
    public NotificationDAO(SQLiteDatabase db) { this.db = db; }

    private ContentValues toContentValues(Notifications notification) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Notifications.COLUMN_NOTIFICATION_ID, notification.getNotification_id());
        values.put(DatabaseContract.Notifications.COLUMN_CONTENT, notification.getContent());
        values.put(DatabaseContract.Notifications.COLUMN_IS_READ, notification.isIs_read() ? 1 : 0);
        values.put(DatabaseContract.Notifications.COLUMN_NOTIFICATION_TYPE, notification.getNotification_type());
        values.put(DatabaseContract.Notifications.COLUMN_USER_ID, notification.getUser_id());

        return values;
    }

    public long insert(Notifications notification) {
        if (notification == null) {
            return -1;
        }
        ContentValues values = toContentValues(notification);
        return db.insert(DatabaseContract.Notifications.TABLE_NAME, null, values);
    }

    public List<Notifications> getAllNotifications() {
        List<Notifications> notifications = new ArrayList<>();
        Cursor cursor = db.query(
                DatabaseContract.Notifications.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null,
                DatabaseContract.Notifications.COLUMN_CREATE_AT + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                Notifications notification_ = new Notifications(
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_NOTIFICATION_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_CONTENT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_IS_READ)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_CREATE_AT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_NOTIFICATION_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_USER_ID))
                        );
                notifications.add(notification_);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return notifications;
    }

    public List<Notifications> getNotificationsByUser(String userId) {
        List<Notifications> notifications = new ArrayList<>();
        String selection = DatabaseContract.Notifications.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {userId};

        Cursor cursor = db.query(
                DatabaseContract.Notifications.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseContract.Notifications.COLUMN_CREATE_AT + " DESC"
        );
        if (cursor.moveToFirst()) {
            do {
                Notifications notification = new Notifications(
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_NOTIFICATION_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_CONTENT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_IS_READ)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_CREATE_AT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_NOTIFICATION_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_USER_ID))
                );
                notifications.add(notification);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notifications;
    }

    public List<Notifications> getUnreadNotifications(String userId) {
        List<Notifications> notifications = new ArrayList<>();
        String selection = DatabaseContract.Notifications.COLUMN_USER_ID + " = ? AND " +
        DatabaseContract.Notifications.COLUMN_IS_READ + " = 0";

        String[] selectionArgs = {userId};

        Cursor cursor = db.query(
                DatabaseContract.Notifications.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseContract.Notifications.COLUMN_CREATE_AT + " DESC"
        );
        if (cursor.moveToFirst()) {
            do {
                Notifications notification = new Notifications(
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_NOTIFICATION_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_CONTENT)),
                        false ,
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_CREATE_AT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_NOTIFICATION_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_USER_ID))
                );
                notifications.add(notification);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notifications;
    }

    public Notifications getNotificationById(String notificationID) {
        String selection = DatabaseContract.Notifications.COLUMN_NOTIFICATION_ID + " = ?";
        String[] selectionArgs = {notificationID};

        Cursor cursor = db.query(
                DatabaseContract.Notifications.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        if (cursor.moveToFirst()) {
            Notifications notification = new Notifications(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_NOTIFICATION_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_CONTENT)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_IS_READ)) == 1,
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_CREATE_AT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_NOTIFICATION_TYPE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Notifications.COLUMN_USER_ID))
            );
            cursor.close();
            return notification;
        }
        return null;
    }

    public int markAsRead(String notificationID) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Notifications.COLUMN_IS_READ, 1);
        String selection = DatabaseContract.Notifications.COLUMN_NOTIFICATION_ID + " = ?";
        String[] selectionArgs = {notificationID};

        return db.update(
                DatabaseContract.Notifications.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
    }

    public int markAllRead(String userId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Notifications.COLUMN_IS_READ, 1);
        String selection = DatabaseContract.Notifications.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {userId};

        return db.update(
                DatabaseContract.Notifications.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
    }

    public int delete(String notificationID) {
        String selection = DatabaseContract.Notifications.COLUMN_NOTIFICATION_ID + " = ?";
        String[] selectionArgs = {notificationID};

        return db.delete(
                DatabaseContract.Notifications.TABLE_NAME,
                selection,
                selectionArgs
        );
    }

    public int getUnreadCount(String userID) {
        String query = "SELECT COUNT(*) FROM " + DatabaseContract.Notifications.TABLE_NAME +
                " WHERE " + DatabaseContract.Notifications.COLUMN_USER_ID + " = ? AND " +
                DatabaseContract.Notifications.COLUMN_IS_READ + " = 0";
        String[] selectionArgs = {userID};

        Cursor cursor = db.rawQuery(query, selectionArgs);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}
