package com.example.n03_quanlychitieu.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
//    public <T> void saveToDatabase(List<T> dataList, String tableName) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        for (T data : dataList) {
//            ContentValues values = new ContentValues();
//
//            Notifications notification = (Notifications) data;
//            values.put("notification_id", notification.getNotification_id());
//            values.put("content", notification.getContent());
//            values.put("is_read", notification.isIs_read() ? 1 : 0);
//            values.put("create_at", notification.getCreate_at());
//            values.put("notification_type", notification.getNotification_type()); // "warn" hoặc "info"
//            values.put("user_id", notification.getUser_id());
//
//            if (data instanceof Incomes) {
//                Incomes income = (Incomes) data;
//                values.put("income_id", income.getIncome_id());
//                values.put("amount", income.getAmount());
//                values.put("description", income.getDescription());
//                values.put("create_at", income.getCreate_at());
//                values.put("user_id", income.getUser_id());
//                values.put("category_id", income.getCategory_id());
//            } else if (data instanceof Expenses) {
//                Expenses expense = (Expenses) data;
//                values.put("expense_id", expense.getExpense_id());
//                values.put("amount", expense.getAmount());
//                values.put("description", expense.getDescription());
//                values.put("create_at", expense.getCreate_at());
//                values.put("user_id", expense.getUser_id());
//                values.put("category_id", expense.getCategory_id());
//                values.put("budget_id", expense.getBudget_id());
//            } else if (data instanceof Budgets) {
//                Budgets budget = (Budgets) data;
//                values.put("budget_id", budget.getBudget_id());
//                values.put("amount", budget.getAmount());
//                values.put("start_date", budget.getStart_date());
//                values.put("end_date", budget.getEnd_date());
//                values.put("description", budget.getDescription());
//                values.put("user_id", budget.getUser_id());
//                values.put("category_id", budget.getCategory_id());
//            } else if (data instanceof Notifications) {
//                Notifications notification = (Notifications) data;
//                values.put("notification_id", notification.getNotification_id());
//                values.put("content", notification.getContent());
//                values.put("is_read", notification.isIs_read() ? 1 : 0);
//                values.put("create_at", notification.getCreate_at());
//                values.put("notification_type", notification.getNotification_type());
//                values.put("user_id", notification.getUser_id());
//            } else if (data instanceof Users) {
//                Users user = (Users) data;
//                values.put("user_id", user.getUser_id());
//                values.put("username", user.getUsername());
//                values.put("email", user.getEmail());
//                values.put("password", user.getPassword());
//                values.put("avatar_url", user.getAvatar_url());
//                values.put("created_at", user.getCreated_at());
//            }
//            db.insert(tableName, null, values);
//        }
//
//        db.close();
//    }
}
