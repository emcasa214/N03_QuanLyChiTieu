package com.example.n03_quanlychitieu.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.n03_quanlychitieu.db.DatabaseContract;
import com.example.n03_quanlychitieu.model.Budgets;
import com.example.n03_quanlychitieu.model.Categories;
import com.example.n03_quanlychitieu.model.Expenses;
import com.example.n03_quanlychitieu.model.Incomes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportDAO {
    private SQLiteDatabase db;
    private CategoryDAO categoryDAO;

    public ReportDAO(SQLiteDatabase db) {
        this.db = db;
        this.categoryDAO = new CategoryDAO(db);
    }

    // Lấy tổng thu nhập theo khoảng thời gian
    public double getTotalIncome(String userId, Date startDate, Date endDate) {
        // Định dạng ngày tháng theo đúng format trong database (dd-MM-yyyy)
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String strStartDate = sdf.format(startDate);
        String strEndDate = sdf.format(endDate);

        String query = "SELECT SUM(" + DatabaseContract.Incomes.COLUMN_AMOUNT + ") " +
                "FROM " + DatabaseContract.Incomes.TABLE_NAME + " " +
                "WHERE " + DatabaseContract.Incomes.COLUMN_USER_ID + " = ? " +
                "AND " + DatabaseContract.Incomes.COLUMN_CREATE_AT + " BETWEEN ? AND ?";

        String[] selectionArgs = {userId, strStartDate, strEndDate};

        Cursor cursor = db.rawQuery(query, selectionArgs);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    // Lấy tổng chi tiêu theo khoảng thời gian
    public double getTotalExpense(String userId, Date startDate, Date endDate) {
        // Định dạng ngày tháng theo đúng format trong database (yyyy-MM-dd)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String strStartDate = sdf.format(startDate);
        String strEndDate = sdf.format(endDate);

        String query = "SELECT SUM(" + DatabaseContract.Expenses.COLUMN_AMOUNT + ") " +
                "FROM " + DatabaseContract.Expenses.TABLE_NAME + " " +
                "WHERE " + DatabaseContract.Expenses.COLUMN_USER_ID + " = ? " +
                "AND " + DatabaseContract.Expenses.COLUMN_CREATE_AT + " BETWEEN ? AND ?";

        String[] selectionArgs = {userId, strStartDate, strEndDate};

        Cursor cursor = db.rawQuery(query, selectionArgs);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    // Các phương thức tiện ích
    public Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public Date[] getCurrentWeekRange() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        Date startDate = getStartOfDay(calendar.getTime());

        calendar.add(Calendar.DAY_OF_WEEK, 6);
        Date endDate = getEndOfDay(calendar.getTime());

        return new Date[]{startDate, endDate};
    }

    public Date[] getCurrentMonthRange() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = getStartOfDay(calendar.getTime());

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = getEndOfDay(calendar.getTime());

        return new Date[]{startDate, endDate};
    }
}