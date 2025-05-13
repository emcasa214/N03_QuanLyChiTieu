package com.example.n03_quanlychitieu.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.n03_quanlychitieu.db.DatabaseContract;
import com.example.n03_quanlychitieu.model.Categories;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private SQLiteDatabase db;

    public CategoryDAO(SQLiteDatabase db) {
        this.db = db;
    }

    private ContentValues toContentValues(Categories category) {
        ContentValues values = new ContentValues();
        values.put("category_id", category.getCategory_id());
        values.put("name", category.getName());
        values.put("icon", category.getIcon());
        values.put("color", category.getColor());
        values.put("type", category.getType());
        values.put("user_id", category.getUser_id()); // Thêm user_id
        return values;
    }

    public long insert(Categories category) {
        if (category == null) {
            return -1; // Trả về -1 nếu đối tượng null
        }
        ContentValues values = toContentValues(category);
        return db.insert("Categories", null, values);
    }
    // Lấy tất cả danh mục theo user_id
    public List<Categories> getAllCategories(String userId) {
        List<Categories> categories = new ArrayList<>();
        String selection = "user_id = ?";
        String[] selectionArgs = {userId};

        Cursor cursor = db.query(
                DatabaseContract.Categories.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                Categories category = new Categories(
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_CATEGORY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_ICON)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_COLOR)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_USER_ID))
                );
                categories.add(category);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return categories;
    }

    public List<Categories> getCategoriesByType(String type) {
        List<Categories> categories = new ArrayList<>();
        String selection = DatabaseContract.Categories.COLUMN_TYPE + " = ?";
        String[] selectionArgs = {type};

        Cursor cursor = db.query(
                DatabaseContract.Categories.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                Categories category = new Categories(
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_CATEGORY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_ICON)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_COLOR)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_USER_ID))
                );
                categories.add(category);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return categories;
    }

    public Categories getCategoryById(String categoryId) {
        String selection = DatabaseContract.Categories.COLUMN_CATEGORY_ID + " = ?";
        String[] selectionArgs = {categoryId};

        Cursor cursor = db.query(
                DatabaseContract.Categories.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            Categories category = new Categories(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_CATEGORY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_ICON)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_COLOR)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_TYPE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_USER_ID))
            );
            cursor.close();
            return category;
        }
        return null;
    }

    public int update(Categories category) {
        if (category == null || category.getCategory_id() == null) {
            return 0; // Trả về 0 nếu đối tượng null hoặc không có ID
        }
        ContentValues values = toContentValues(category);
        String selection = DatabaseContract.Categories.COLUMN_CATEGORY_ID + " = ?";
        String[] selectionArgs = {category.getCategory_id()};

        return db.update(
                DatabaseContract.Categories.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
    }

    public int delete(String categoryId) {
        String selection = DatabaseContract.Categories.COLUMN_CATEGORY_ID + " = ?";
        String[] selectionArgs = {categoryId};

        return db.delete(
                DatabaseContract.Categories.TABLE_NAME,
                selection,
                selectionArgs
        );
    }
    
}