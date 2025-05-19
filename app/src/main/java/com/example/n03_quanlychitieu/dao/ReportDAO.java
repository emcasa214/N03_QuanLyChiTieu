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

    // Lấy danh sách thu nhập theo khoảng thời gian
    public List<Incomes> getIncomesByTimeRange(String userId, Date startDate, Date endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String strStartDate = sdf.format(startDate);
        String strEndDate = sdf.format(endDate);

        List<Incomes> incomes = new ArrayList<>();
        String selection = DatabaseContract.Incomes.COLUMN_USER_ID + " = ? AND " +
                DatabaseContract.Incomes.COLUMN_CREATE_AT + " BETWEEN ? AND ?";

        String[] selectionArgs = {
                userId,
                strStartDate,
                strEndDate
        };

        Cursor cursor = db.query(
                DatabaseContract.Incomes.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseContract.Incomes.COLUMN_CREATE_AT + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                Incomes income = new Incomes(
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Incomes.COLUMN_INCOME_ID)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Incomes.COLUMN_AMOUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Incomes.COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Incomes.COLUMN_CREATE_AT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Incomes.COLUMN_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Incomes.COLUMN_CATEGORY_ID))
                );
                incomes.add(income);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return incomes;
    }

    // Lấy danh sách chi tiêu theo khoảng thời gian
    public List<Expenses> getExpensesByTimeRange(String userId, Date startDate, Date endDate) {
        List<Expenses> expenses = new ArrayList<>();
        String selection = DatabaseContract.Expenses.COLUMN_USER_ID + " = ? AND " +
                "datetime(" + DatabaseContract.Expenses.COLUMN_CREATE_AT + ") BETWEEN datetime(?) AND datetime(?)";
        String[] selectionArgs = {
                userId,
                String.valueOf(startDate.getTime()),
                String.valueOf(endDate.getTime())
        };

        Cursor cursor = db.query(
                DatabaseContract.Expenses.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseContract.Expenses.COLUMN_CREATE_AT + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                Expenses expense = new Expenses(
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Expenses.COLUMN_EXPENSE_ID)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Expenses.COLUMN_AMOUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Expenses.COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Expenses.COLUMN_CREATE_AT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Expenses.COLUMN_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Expenses.COLUMN_CATEGORY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Expenses.COLUMN_BUDGET_ID))
                );
                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenses;
    }

    // Lấy thống kê theo danh mục (sử dụng Map để nhóm dữ liệu)
    public Map<Categories, Double> getCategoryStats(String userId, Date startDate, Date endDate, String type) {
        Map<Categories, Double> stats = new HashMap<>();

        String tableName = type.equals("income") ? DatabaseContract.Incomes.TABLE_NAME : DatabaseContract.Expenses.TABLE_NAME;
        String amountColumn = type.equals("income") ? DatabaseContract.Incomes.COLUMN_AMOUNT : DatabaseContract.Expenses.COLUMN_AMOUNT;
        String categoryIdColumn = type.equals("income") ? DatabaseContract.Incomes.COLUMN_CATEGORY_ID : DatabaseContract.Expenses.COLUMN_CATEGORY_ID;
        String createAtColumn = type.equals("income") ? DatabaseContract.Incomes.COLUMN_CREATE_AT : DatabaseContract.Expenses.COLUMN_CREATE_AT;

        String query = "SELECT " + categoryIdColumn + ", SUM(" + amountColumn + ") as total " +
                "FROM " + tableName + " " +
                "WHERE " + DatabaseContract.Incomes.COLUMN_USER_ID + " = ? " +
                "AND datetime(" + createAtColumn + ") BETWEEN datetime(?) AND datetime(?) " +
                "GROUP BY " + categoryIdColumn;

        String[] selectionArgs = {
                userId,
                String.valueOf(startDate.getTime()),
                String.valueOf(endDate.getTime())
        };

        Cursor cursor = db.rawQuery(query, selectionArgs);
        while (cursor.moveToNext()) {
            String categoryId = cursor.getString(0);
            Categories category = categoryDAO.getCategoryById(categoryId);
            if (category != null) {
                stats.put(category, cursor.getDouble(1));
            }
        }
        cursor.close();
        return stats;
    }

    // Lấy thống kê theo thời gian (ngày/tuần/tháng)
    public Map<String, Double> getTimeStats(String userId, Date startDate, Date endDate, String type, String groupBy) {
        Map<String, Double> stats = new HashMap<>();

        String tableName = type.equals("income") ? DatabaseContract.Incomes.TABLE_NAME : DatabaseContract.Expenses.TABLE_NAME;
        String amountColumn = type.equals("income") ? DatabaseContract.Incomes.COLUMN_AMOUNT : DatabaseContract.Expenses.COLUMN_AMOUNT;
        String createAtColumn = type.equals("income") ? DatabaseContract.Incomes.COLUMN_CREATE_AT : DatabaseContract.Expenses.COLUMN_CREATE_AT;

        String dateFormat;
        switch (groupBy) {
            case "day": dateFormat = "%Y-%m-%d"; break;
            case "week": dateFormat = "%Y-%W"; break;
            case "month": dateFormat = "%Y-%m"; break;
            default: dateFormat = "%Y-%m-%d";
        }

        String query = "SELECT strftime('" + dateFormat + "', datetime(" + createAtColumn + "/1000, 'unixepoch')) as time_group, " +
                "SUM(" + amountColumn + ") as total " +
                "FROM " + tableName + " " +
                "WHERE " + DatabaseContract.Incomes.COLUMN_USER_ID + " = ? " +
                "AND datetime(" + createAtColumn + ") BETWEEN datetime(?) AND datetime(?) " +
                "GROUP BY time_group";

        String[] selectionArgs = {
                userId,
                String.valueOf(startDate.getTime()),
                String.valueOf(endDate.getTime())
        };

        Cursor cursor = db.rawQuery(query, selectionArgs);
        while (cursor.moveToNext()) {
            stats.put(cursor.getString(0), cursor.getDouble(1));
        }
        cursor.close();
        return stats;
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