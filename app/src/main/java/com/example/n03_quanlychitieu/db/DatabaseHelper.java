package com.example.n03_quanlychitieu.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;

import com.example.n03_quanlychitieu.model.Users;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import at.favre.lib.crypto.bcrypt.BCrypt;

import com.example.n03_quanlychitieu.model.Budgets;
import com.example.n03_quanlychitieu.model.Expenses;
import com.example.n03_quanlychitieu.model.Incomes;
import com.example.n03_quanlychitieu.model.Notifications;
import com.example.n03_quanlychitieu.model.Users;

import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "fin_manager.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.Users.CREATE_TABLE);
        db.execSQL(DatabaseContract.Categories.CREATE_TABLE);
        db.execSQL(DatabaseContract.Notifications.CREATE_TABLE);
        db.execSQL(DatabaseContract.Budgets.CREATE_TABLE);
        db.execSQL(DatabaseContract.Incomes.CREATE_TABLE);
        db.execSQL(DatabaseContract.Expenses.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Expenses.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Incomes.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Budgets.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Notifications.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Categories.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Users.TABLE_NAME);
        onCreate(db);
    }

    /***
     * User handle query
     */
    public interface UserCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public void addUserAsync(String userID, String username, String email, String hashPassword, UserCallback callback) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put(DatabaseContract.Users.COLUMN_USER_ID, userID);
                contentValues.put(DatabaseContract.Users.COLUMN_USERNAME, username);
                contentValues.put(DatabaseContract.Users.COLUMN_EMAIL, email);
                contentValues.put(DatabaseContract.Users.COLUMN_PASSWORD, hashPassword);

                long result = db.insert(DatabaseContract.Users.TABLE_NAME, null, contentValues);

                // Chuyển kết quả về main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (result != -1) {
                        callback.onSuccess();
                    } else {
                        callback.onError("Failed to insert user");
                    }
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onError(e.getMessage());
                });
            }
        });
    }

    public interface UserCheckCallback {
        void onUsernameExists();
        void onEmailExists();
        void onAvailable(); // Không bị trùng
        void onError(String message);
    }

    // check user
    public void checkUser(String username, String email, UserCheckCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (SQLiteDatabase db = this.getReadableDatabase()) {
                // Kiểm tra trùng username
                boolean usernameExists = checkFieldExists(db,
                        DatabaseContract.Users.COLUMN_USERNAME, username);

                // Kiểm tra trùng email
                boolean emailExists = checkFieldExists(db,
                        DatabaseContract.Users.COLUMN_EMAIL, email);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (usernameExists) {
                        callback.onUsernameExists();
                    } else if (emailExists) {
                        callback.onEmailExists();
                    } else {
                        callback.onAvailable();
                    }
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onError("Lỗi hệ thống: " + e.getMessage());
                });
            }
        });
    }

    // Helper method để kiểm tra trường dữ liệu
    private boolean checkFieldExists(SQLiteDatabase db, String column, String value) {
        Cursor cursor = db.query(
                DatabaseContract.Users.TABLE_NAME,
                new String[]{column},
                column + " = ?",
                new String[]{value},
                null, null, null
        );

        boolean exists = (cursor != null && cursor.getCount() > 0);
        return exists;
    }

    // get User

    public interface GetUserCallback {
        void onUserLoaded(Users user);
        void onUserNotFound();
        void onError(String errorMessage);
    }

    @SuppressLint("Range")
    public void getUserAsync(String usernameOrEmail, String rawPassword, GetUserCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (SQLiteDatabase db = this.getReadableDatabase()) {
                Cursor c = db.query(
                        DatabaseContract.Users.TABLE_NAME,
                        null,
                        DatabaseContract.Users.COLUMN_USERNAME + " = ? OR " +
                                DatabaseContract.Users.COLUMN_EMAIL + " = ?",
                        new String[]{usernameOrEmail, usernameOrEmail},
                        null, null, null
                );

                Users user;
                boolean passwordMatch;

                if (c != null && c.moveToFirst()) {
                    user = new Users();
                    user.setUser_id(c.getString(c.getColumnIndex(DatabaseContract.Users.COLUMN_USER_ID)));
                    user.setUsername(c.getString(c.getColumnIndex(DatabaseContract.Users.COLUMN_USERNAME)));
                    user.setEmail(c.getString(c.getColumnIndex(DatabaseContract.Users.COLUMN_EMAIL)));
                    String strokeHash = c.getString(c.getColumnIndex(DatabaseContract.Users.COLUMN_PASSWORD));
                    user.setPassword(null);
                    user.setAvatar_url(c.getString(c.getColumnIndex(DatabaseContract.Users.COLUMN_AVATAR_URL)));
                    passwordMatch = BCrypt.verifyer().verify(rawPassword.toCharArray(), strokeHash).verified;
                } else {
                    user = null;
                    passwordMatch = false;
                }

                // Trả kết quả về main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (user != null && passwordMatch) {
                        callback.onUserLoaded(user);
                    } else {
                        callback.onUserNotFound();
                    }
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onError("Database error: " + e.getMessage());
                });
            }
        });
    }
    /***
     * User handle query
     */

}
