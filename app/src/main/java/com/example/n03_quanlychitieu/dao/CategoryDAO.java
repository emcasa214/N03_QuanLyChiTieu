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
        values.put(DatabaseContract.Categories.COLUMN_CATEGORY_ID, category.getCategory_id());
        values.put(DatabaseContract.Categories.COLUMN_NAME, category.getName());
        values.put(DatabaseContract.Categories.COLUMN_ICON, category.getIcon());
        values.put(DatabaseContract.Categories.COLUMN_COLOR, category.getColor());
        values.put(DatabaseContract.Categories.COLUMN_TYPE, category.getType());
        return values;
    }

    public long insert(Categories category) {
        ContentValues values = toContentValues(category);
        return db.insert(DatabaseContract.Categories.TABLE_NAME, null, values);
    }

    public List<Categories> getAllCategories() {
        List<Categories> categories = new ArrayList<>();
        Cursor cursor = db.query(
                DatabaseContract.Categories.TABLE_NAME,
                null,
                null,
                null,
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
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_TYPE))
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
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_TYPE))
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
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Categories.COLUMN_TYPE))
            );
            cursor.close();
            return category;
        }
        return null;
    }

    public int update(Categories category) {
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