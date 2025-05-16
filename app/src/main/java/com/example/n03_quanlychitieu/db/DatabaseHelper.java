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


    public interface EmailCallback {
        void onSuccess(boolean emailExists);

        void onError(String errorMessage);
    }

    public void checkEmail(String email, EmailCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (SQLiteDatabase db = this.getReadableDatabase()) {
                boolean emailExists = checkEmailExists(db, email);

                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onSuccess(emailExists);
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onError(e.getMessage());
                });
            }
        });
    }

    private boolean checkEmailExists(SQLiteDatabase db, String email) {
        // Limit 1
        String query = "SELECT 1 FROM " + DatabaseContract.Users.TABLE_NAME +
                " WHERE " + DatabaseContract.Users.COLUMN_EMAIL + " = ? LIMIT 1";

        try (Cursor cursor = db.rawQuery(query, new String[]{email})) {
            return cursor != null && cursor.moveToFirst();
        }
    }

    public interface ResetPasswordCallback {
        void onSuccess(int rowsAffected);

        void onError(String errorMessage);
    }

    public void resetPassword(String email, String hashPassword, ResetPasswordCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (SQLiteDatabase db = this.getWritableDatabase()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(DatabaseContract.Users.COLUMN_PASSWORD, hashPassword);

                int rowsAffected = db.update(
                        DatabaseContract.Users.TABLE_NAME,
                        contentValues,
                        DatabaseContract.Users.COLUMN_EMAIL + " = ?",
                        new String[]{email}
                );

                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onSuccess(rowsAffected);
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onError(e.getMessage());
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

    // Sign up with google
    public void addGoogleUserAsync(String userID, String username, String email, String avatarUrl, UserCallback callback) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DatabaseContract.Users.COLUMN_USER_ID, userID);
                values.put(DatabaseContract.Users.COLUMN_USERNAME, username);
                values.put(DatabaseContract.Users.COLUMN_EMAIL, email);
                values.put(DatabaseContract.Users.COLUMN_PASSWORD, ""); // Không có password
                values.put(DatabaseContract.Users.COLUMN_AVATAR_URL, avatarUrl);

                long result = db.insert(DatabaseContract.Users.TABLE_NAME, null, values);

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

    public interface GetUserByEmailCallback {
        void onUserLoaded(Users user);

        void onUserNotFound();

        void onError(String errorMessage);
    }

    @SuppressLint("Range")
    public void getUserByEmail(String email, GetUserByEmailCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (SQLiteDatabase db = this.getReadableDatabase()) {
                Cursor cursor = db.query(
                        DatabaseContract.Users.TABLE_NAME,
                        null,
                        DatabaseContract.Users.COLUMN_EMAIL + " = ?",
                        new String[]{email},
                        null, null, null
                );

                Users user;
                if (cursor != null && cursor.moveToFirst()) {
                    user = new Users();
                    user.setUser_id(cursor.getString(cursor.getColumnIndex(DatabaseContract.Users.COLUMN_USER_ID)));
                    user.setUsername(cursor.getString(cursor.getColumnIndex(DatabaseContract.Users.COLUMN_USERNAME)));
                    user.setEmail(cursor.getString(cursor.getColumnIndex(DatabaseContract.Users.COLUMN_EMAIL)));
                    user.setPassword(cursor.getString(cursor.getColumnIndex(DatabaseContract.Users.COLUMN_PASSWORD)));
                    user.setAvatar_url(cursor.getString(cursor.getColumnIndex(DatabaseContract.Users.COLUMN_AVATAR_URL)));
                    user.setCreated_at(cursor.getString(cursor.getColumnIndex(DatabaseContract.Users.COLUMN_CREATED_AT)));
                } else {
                    user = null;
                }

                // Trả kết quả về main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (user != null) {
                        callback.onUserLoaded(user);
                    } else {
                        callback.onUserNotFound();
                    }
                });

                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onError("Database error: " + e.getMessage());
                });
            }
        });
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
                Log.e("DatabaseHelper", "Error opening/querying DB", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onError("Database error: " + e.getMessage());
                });
            }
        });
    }


    /***
     * User handle query
     */

    public Users getUserById(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Users user = null;

        Cursor cursor = db.query(
                "Users", // Tên bảng
                null, // Lấy tất cả các cột
                "user_id = ?", // Điều kiện WHERE
                new String[]{userId}, // Giá trị điều kiện
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = new Users(
                    cursor.getString(cursor.getColumnIndexOrThrow("user_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    cursor.getString(cursor.getColumnIndexOrThrow("avatar_url")),
                    cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
            );
            cursor.close();
        }

        db.close();
        return user;
    }

    public String getPasswordForUser(String userId) {
        String stored = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            // Query chọn cột password
            String sql = "SELECT password FROM Users WHERE user_id = ?";
            cursor = db.rawQuery(sql, new String[]{userId});

            if (cursor.moveToFirst()) {
                stored = cursor.getString(cursor.getColumnIndexOrThrow("password"));
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return stored;
    }

    public Boolean updateUser(String id, String name, String gmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", name);
        contentValues.put("email", gmail);
        long result = db.update("Users", contentValues, "user_id = ?", new String[]{id});
        if (result == -1) {
            return false;
        }
        return true;
    }

    public void checkUserForUpdate(String userId, String username, String email, UserCheckCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (SQLiteDatabase db = this.getReadableDatabase()) {
                // Kiểm tra trùng username (ngoại trừ user hiện tại)
                boolean usernameExists = checkFieldExistsForUpdate(db,
                        DatabaseContract.Users.COLUMN_USERNAME, username, userId);

                // Kiểm tra trùng email (ngoại trừ user hiện tại)
                boolean emailExists = checkFieldExistsForUpdate(db,
                        DatabaseContract.Users.COLUMN_EMAIL, email, userId);

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

    private boolean checkFieldExistsForUpdate(SQLiteDatabase db, String column, String value, String userId) {
        Cursor cursor = db.query(
                DatabaseContract.Users.TABLE_NAME,
                new String[]{column},
                column + " = ? AND user_id != ?", // Loại trừ user hiện tại
                new String[]{value, userId},
                null, null, null
        );

        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public void insertSampleData() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Chèn Categories
        db.execSQL("INSERT OR IGNORE INTO Categories (category_id, name, icon, color, type, user_id) VALUES " +
                "('cat1', 'Ăn uống', 'ic_food', '#FF5722', 'expense', '6cc204cb-95b7-4e7d-9b55-5e088834f033')," +
                "('cat2', 'Lương', 'ic_salary', '#4CAF50', 'income', '6cc204cb-95b7-4e7d-9b55-5e088834f033');");

        // Chèn Budgets
        db.execSQL("INSERT OR IGNORE INTO Budgets (budget_id, amount, start_date, end_date, description, user_id, category_id) VALUES " +
                "('bud1', 2000000, '2025-05-01', '2025-05-31', 'Shopping' ,'6cc204cb-95b7-4e7d-9b55-5e088834f033', 'cat1');");

        // Chèn Expenses
        db.execSQL("INSERT OR IGNORE INTO Expenses (expense_id, amount, description, create_at, user_id, category_id, budget_id) VALUES " +
                "('exp1', 50000, 'Ăn sáng', '2025-05-02', '6cc204cb-95b7-4e7d-9b55-5e088834f033', 'cat1', 'bud1')," +
                "('exp2', 120000, 'Ăn trưa', '2025-05-05', '6cc204cb-95b7-4e7d-9b55-5e088834f033', 'cat1', 'bud1');");

        // Chèn Incomes
        db.execSQL("INSERT OR IGNORE INTO Incomes (income_id, amount, description, create_at, user_id, category_id) VALUES " +
                "('inc1', 10000000, 'Lương tháng 5', '2025-05-01', '6cc204cb-95b7-4e7d-9b55-5e088834f033', 'cat2');");

        // Chèn Notifications
//        db.execSQL("INSERT OR IGNORE INTO Notifications (notification_id, content, is_read, created_at, notification_type, user_id) VALUES " +
//                "('noti1', 'Bạn đã chi tiêu vượt ngân sách!', 0, '2025-05-15 08:00:00', 'warn', '6cc204cb-95b7-4e7d-9b55-5e088834f033');");

        db.close();
    }
    public interface SimpleCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public void addIncomeAsync(String userId, String amount, String categoryId, String date, SimpleCallback callback) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                double amountValue = Double.parseDouble(amount); // ép kiểu tại đây

                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put("user_id", userId);
                contentValues.put("amount", amountValue); // đúng kiểu double
                contentValues.put("category_id", categoryId);
                contentValues.put("create_at", date);


                long result = db.insert("incomes", null, contentValues);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (result != -1) {
                        callback.onSuccess();
                    } else {
                        callback.onError("Không thể thêm thu nhập");
                    }
                });
            } catch (NumberFormatException e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onError("Số tiền không hợp lệ: " + amount);
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onError("Lỗi: " + e.getMessage());
                });
            }
        });
    }



}
