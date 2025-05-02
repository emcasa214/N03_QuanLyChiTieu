package com.example.n03_quanlychitieu.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.ui.category.AddCategoryActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOpenCategory = findViewById(R.id.btnOpenCategory);
        btnOpenCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "Attempting to start AddCategoryActivity");
                    Intent intent = new Intent(MainActivity.this, AddCategoryActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Successfully started AddCategoryActivity");
                } catch (Exception e) {
                    Log.e(TAG, "Error starting AddCategoryActivity", e);
                    Toast.makeText(MainActivity.this, "Lỗi khi mở màn hình quản lý danh mục", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}