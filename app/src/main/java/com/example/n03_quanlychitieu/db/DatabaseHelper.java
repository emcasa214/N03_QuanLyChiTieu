package com.example.n03_quanlychitieu.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.n03_quanlychitieu.model.Users;
import com.example.n03_quanlychitieu.model.Budgets;
import com.example.n03_quanlychitieu.model.Expenses;
import com.example.n03_quanlychitieu.model.Incomes;
import com.example.n03_quanlychitieu.model.Notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    public static final String DATABASE_NAME = "fin_manager.db";
    private static final int DATABASE_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        Log.d(TAG, "Foreign key constraints enabled");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.Users.CREATE_TABLE);
        db.execSQL(DatabaseContract.Categories.CREATE_TABLE);
        db.execSQL(DatabaseContract.Notifications.CREATE_TABLE);
        db.execSQL(DatabaseContract.Budgets.CREATE_TABLE);
        db.execSQL(DatabaseContract.Incomes.CREATE_TABLE);
        db.execSQL(DatabaseContract.Expenses.CREATE_TABLE);

        // Thêm dữ liệu mẫu
        ContentValues userValues = new ContentValues();
        userValues.put(DatabaseContract.Users.COLUMN_USER_ID, "user1");
        userValues.put(DatabaseContract.Users.COLUMN_USERNAME, "testuser");
        userValues.put(DatabaseContract.Users.COLUMN_EMAIL, "test@example.com");
        userValues.put(DatabaseContract.Users.COLUMN_PASSWORD, BCrypt.withDefaults().hashToString(12, "password".toCharArray()));
        db.insert(DatabaseContract.Users.TABLE_NAME, null, userValues);

        ContentValues categoryValues = new ContentValues();
        categoryValues.put(DatabaseContract.Categories.COLUMN_CATEGORY_ID, "1");
        categoryValues.put(DatabaseContract.Categories.COLUMN_NAME, "Salary");
        categoryValues.put(DatabaseContract.Categories.COLUMN_TYPE, "income");
        db.insert(DatabaseContract.Categories.TABLE_NAME, null, categoryValues);
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

    // ---------- Interfaces Callback ----------
    public interface SimpleCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface UserCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface UserCheckCallback {
        void onUsernameExists();
        void onEmailExists();
        void onAvailable();
        void onError(String message);
    }

    public interface GetUserCallback {
        void onUserLoaded(Users user);
        void onUserNotFound();
        void onError(String errorMessage);
    }

    public interface GetIncomeListCallback {
        void onSuccess(List<Incomes> incomes);
        void onError(String errorMessage);
    }

    // ---------- Thêm dữ liệu ----------
    public void addIncomeAsync(String userId, String amount, String categoryId, String date, String description, SimpleCallback callback) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put(DatabaseContract.Incomes.COLUMN_USER_ID, userId);
                contentValues.put(DatabaseContract.Incomes.COLUMN_AMOUNT, amount);
                contentValues.put(DatabaseContract.Incomes.COLUMN_CATEGORY_ID, categoryId);
                contentValues.put(DatabaseContract.Incomes.COLUMN_CREATE_AT, date);
                contentValues.put(DatabaseContract.Incomes.COLUMN_DESCRIPTION, description);

                long result = db.insert(DatabaseContract.Incomes.TABLE_NAME, null, contentValues);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (result != -1) callback.onSuccess();
                    else callback.onError("Failed to add income");
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
            }
        });
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

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (result != -1) callback.onSuccess();
                    else callback.onError("Failed to insert user");
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // ---------- Kiểm tra người dùng ----------
    public void checkUser(String username, String email, UserCheckCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (SQLiteDatabase db = this.getReadableDatabase()) {
                boolean usernameExists = checkFieldExists(db, DatabaseContract.Users.COLUMN_USERNAME, username);
                boolean emailExists = checkFieldExists(db, DatabaseContract.Users.COLUMN_EMAIL, email);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (usernameExists) callback.onUsernameExists();
                    else if (emailExists) callback.onEmailExists();
                    else callback.onAvailable();
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Lỗi hệ thống: " + e.getMessage()));
            }
        });
    }

    private boolean checkFieldExists(SQLiteDatabase db, String column, String value) {
        Cursor cursor = db.query(
                DatabaseContract.Users.TABLE_NAME,
                new String[]{column},
                column + " = ?",
                new String[]{value},
                null, null, null
        );

        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    // ---------- Lấy thông tin người dùng ----------
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

                Users user = null;
                boolean passwordMatch = false;

                if (c != null && c.moveToFirst()) {
                    user = new Users();
                    user.setUser_id(c.getString(c.getColumnIndex(DatabaseContract.Users.COLUMN_USER_ID)));
                    user.setUsername(c.getString(c.getColumnIndex(DatabaseContract.Users.COLUMN_USERNAME)));
                    user.setEmail(c.getString(c.getColumnIndex(DatabaseContract.Users.COLUMN_EMAIL)));
                    String storedHash = c.getString(c.getColumnIndex(DatabaseContract.Users.COLUMN_PASSWORD));
                    user.setAvatar_url(c.getString(c.getColumnIndex(DatabaseContract.Users.COLUMN_AVATAR_URL)));
                    user.setPassword(null); // không trả về mật khẩu
                    passwordMatch = BCrypt.verifyer().verify(rawPassword.toCharArray(), storedHash).verified;
                }

                Users finalUser = user;
                boolean finalPasswordMatch = passwordMatch;

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (finalUser != null && finalPasswordMatch) callback.onUserLoaded(finalUser);
                    else callback.onUserNotFound();
                });

                if (c != null) c.close();
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Database error: " + e.getMessage()));
            }
        });
    }

    // ---------- Lấy danh sách thu nhập ----------
    @SuppressLint("Range")
    public void getAllIncomeByUserId(String userId, GetIncomeListCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Incomes> incomesList = new ArrayList<>();
            try (SQLiteDatabase db = this.getReadableDatabase()) {
                Cursor c = db.query(
                        DatabaseContract.Incomes.TABLE_NAME,
                        null,
                        DatabaseContract.Incomes.COLUMN_USER_ID + " = ?",
                        new String[]{userId},
                        null, null,
                        DatabaseContract.Incomes.COLUMN_CREATE_AT + " DESC"
                );

                if (c != null && c.moveToFirst()) {
                    do {
                        Incomes income = new Incomes();
                        income.setIncome_id(c.getString(c.getColumnIndex(DatabaseContract.Incomes.COLUMN_INCOME_ID)));
                        income.setUser_id(c.getString(c.getColumnIndex(DatabaseContract.Incomes.COLUMN_USER_ID)));
                        income.setAmount(Double.parseDouble(c.getString(c.getColumnIndex(DatabaseContract.Incomes.COLUMN_AMOUNT))));
                        income.setCategory_id(c.getString(c.getColumnIndex(DatabaseContract.Incomes.COLUMN_CATEGORY_ID)));
                        income.setCreate_at(c.getString(c.getColumnIndex(DatabaseContract.Incomes.COLUMN_CREATE_AT)));
                        income.setDescription(c.getString(c.getColumnIndex(DatabaseContract.Incomes.COLUMN_DESCRIPTION)));
                        incomesList.add(income);
                    } while (c.moveToNext());
                    c.close();
                }

                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(incomesList));
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Error fetching incomes: " + e.getMessage()));
            }
        });
    }
}