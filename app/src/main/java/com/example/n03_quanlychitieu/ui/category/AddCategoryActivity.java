package com.example.n03_quanlychitieu.ui.category;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.dao.CategoryDAO;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.model.Categories;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddCategoryActivity extends AppCompatActivity {
    private EditText edtName;
    private EditText edtIcon;
    private EditText edtColor;
    private CheckBox cbIncome;
    private CheckBox cbExpense;
    private Button btnLuu;
    private ListView lvDanhMuc;
    private CategoryDAO categoryDAO;
    private ArrayAdapter<String> adapter;
    private List<String> categoryNames;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        categoryDAO = new CategoryDAO(dbHelper.getWritableDatabase());

        initializeViews();
        setupListView();
        setupCheckBoxes();
        setupButtonListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void initializeViews() {
        edtName = findViewById(R.id.edtName);
        edtIcon = findViewById(R.id.edtIcon);
        edtColor = findViewById(R.id.edtColor);
        cbIncome = findViewById(R.id.cbIncome);
        cbExpense = findViewById(R.id.cbExpense);
        btnLuu = findViewById(R.id.btnLuu);
        lvDanhMuc = findViewById(R.id.lvDanhMuc);
    }

    private void setupButtonListeners() {
        btnLuu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCategory();
            }
        });
    }

    private void setupCheckBoxes() {
        cbIncome.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbExpense.setChecked(false);
            }
        });

        cbExpense.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbIncome.setChecked(false);
            }
        });
    }

    private void setupListView() {
        categoryNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categoryNames);
        lvDanhMuc.setAdapter(adapter);
        refreshList();
    }

    private void saveCategory() {
        String name = edtName.getText().toString().trim();
        String icon = edtIcon.getText().toString().trim();
        String color = edtColor.getText().toString().trim();
        String type = "";

        if (cbIncome.isChecked()) {
            type = "income";
        } else if (cbExpense.isChecked()) {
            type = "expense";
        }

        if (name.isEmpty()) {
            edtName.setError("Vui lòng nhập tên danh mục");
            return;
        }
        if (type.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn loại danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        String categoryId = UUID.randomUUID().toString();
        Categories category = new Categories(categoryId, name, icon, color, type);
        long result = categoryDAO.insert(category);

        if (result != -1) {
            Toast.makeText(this, "Thêm danh mục thành công", Toast.LENGTH_SHORT).show();
            clearInputs();
            refreshList();
        } else {
            Toast.makeText(this, "Thêm danh mục thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearInputs() {
        edtName.setText("");
        edtIcon.setText("");
        edtColor.setText("");
        cbIncome.setChecked(false);
        cbExpense.setChecked(false);
    }

    private void refreshList() {
        List<Categories> categories = categoryDAO.getAllCategories();
        categoryNames.clear();
        for (Categories category : categories) {
            categoryNames.add(category.getName() + " (" + category.getType() + ")");
        }
        adapter.notifyDataSetChanged();
    }
}