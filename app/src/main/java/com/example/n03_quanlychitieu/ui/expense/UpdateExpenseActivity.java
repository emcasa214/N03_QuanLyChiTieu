package com.example.n03_quanlychitieu.ui.expense;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.model.Budgets;
import com.example.n03_quanlychitieu.model.Categories;
import com.example.n03_quanlychitieu.utils.AuthenticationManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UpdateExpenseActivity extends AppCompatActivity {
    private static final String TAG = "UpdateExpenseActivity";
    private TextInputEditText etDate, etAmount, etDescription;
    private TextInputLayout tilDate, tilAmount, tilDescription;
    private Button btnUpdate;
    private ImageButton btnBack;
    private Spinner spinnerCategory, spinnerBudget;
    private DatabaseHelper databaseHelper;

    private String userId;
    private String expenseId;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_expense);

        databaseHelper = new DatabaseHelper(this);
        userId = AuthenticationManager.getInstance(this).getCurrentUser().getUser_id();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Sửa chi tiêu");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Ánh xạ
        tilDate        = findViewById(R.id.tilDate);
        tilAmount      = findViewById(R.id.tilAmount);
        tilDescription = findViewById(R.id.tilDescription);
        etDate         = findViewById(R.id.etDate);
        etAmount       = findViewById(R.id.etAmount);
        etDescription  = findViewById(R.id.etDescription);
        spinnerCategory= findViewById(R.id.spinnerCategory);
        spinnerBudget  = findViewById(R.id.spinnerBudget);
        btnUpdate      = findViewById(R.id.btnSave);
        btnBack        = findViewById(R.id.btnBack);

        // Nhận từ Intent
        Intent intent = getIntent();
        expenseId = getIntent().getStringExtra("expenseId");
        String originalDate = getIntent().getStringExtra("date");
        String amount = getIntent().getStringExtra("amount");
        String categoryId = getIntent().getStringExtra("categoryId");
        String description = getIntent().getStringExtra("description");
        String budgetId = getIntent().getStringExtra("budget");
        position = intent.getIntExtra("position", -1);

        etDate.setText(originalDate);
        etAmount.setText(amount);
        etDescription.setText(description);

        // Load spinners
        loadCategories();
        selectSpinnerItemById(spinnerCategory, categoryId);
        loadBudgets();
        selectSpinnerItemById(spinnerBudget, budgetId);

        // Date picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Back
        btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // Update
        btnUpdate.setOnClickListener(v -> {
            tilDate.setError(null);
            tilAmount.setError(null);
            tilDescription.setError(null);

            String newDate  = etDate.getText().toString().trim();
            String newAmount= etAmount.getText().toString().trim();
            String newDesc  = etDescription.getText().toString().trim();
            Categories selCat = (Categories) spinnerCategory.getSelectedItem();
            Budgets   selBud = (Budgets)   spinnerBudget.getSelectedItem();
            String newCatId = selCat != null ? selCat.getCategory_id() : null;
            String newBudId = selBud != null && !"none".equals(selBud.getBudget_id())
                    ? selBud.getBudget_id() : null;

            // Validate...
            if (newDate.isEmpty())      { tilDate.setError("Vui lòng chọn ngày"); return; }
            if (newAmount.isEmpty())    { tilAmount.setError("Vui lòng nhập số tiền"); return; }
            if (newCatId == null)       { Toast.makeText(this,"Chọn danh mục",Toast.LENGTH_SHORT).show(); return; }
            if (newDesc.isEmpty())      { tilDescription.setError("Nhập mô tả"); return; }

            try {
                double amtVal = Double.parseDouble(newAmount);
                if (amtVal<=0) { tilAmount.setError("Phải lớn hơn 0"); return; }

                SimpleDateFormat inFmt  = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat outFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String formattedDate = outFmt.format(inFmt.parse(newDate));

                btnUpdate.setEnabled(false);
                btnBack.setEnabled(false);

                Log.d(TAG, "Calling updateExpenseAsync with: "
                        + "expenseId=" + expenseId
                        + ", userId=" + userId
                        + ", amount=" + newAmount
                        + ", categoryId=" + newCatId
                        + ", date=" + formattedDate
                        + ", budgetId=" + newBudId
                );

                // *** GỌI updateExpenseAsync đúng thứ tự ***
                databaseHelper.updateExpenseAsync(
                        expenseId,              // 1. expenseId
                        userId,                 // 2. userId
                        newAmount,              // 3. amount
                        newCatId,               // 4. categoryId
                        newDesc,                // 5. description
                        formattedDate,          // 6. date
                        newBudId,               // 7. budgetId
                        new DatabaseHelper.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Intent result = new Intent();
                                result.putExtra("position",    position);
                                result.putExtra("expense_id",  expenseId);
                                result.putExtra("date",        newDate);
                                result.putExtra("amount",      newAmount);
                                result.putExtra("category",    newCatId);
                                result.putExtra("description", newDesc);
                                result.putExtra("budget",      newBudId);
                                setResult(RESULT_OK, result);
                                finish();
                            }
                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(UpdateExpenseActivity.this, "Lỗi: "+errorMessage, Toast.LENGTH_SHORT).show();
                                btnUpdate.setEnabled(true);
                                btnBack.setEnabled(true);
                            }
                        }
                );
            } catch (Exception e) {
                tilDate.setError("Định dạng ngày không hợp lệ");
                btnUpdate.setEnabled(true);
                btnBack.setEnabled(true);
            }
        });
    }

    private void loadCategories() {
        List<Categories> list = databaseHelper.getAllCategories(userId, "expense");
        if (list.isEmpty()) list.add(new Categories("none","Không có danh mục"));
        else               list.add(0, new Categories("none","Chọn danh mục"));
        ArrayAdapter<Categories> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(ad);
    }

    private void loadBudgets() {
        List<Budgets> list = databaseHelper.getAllBudgets(userId);
        if (list.isEmpty()) list.add(new Budgets("none","Không có ngân sách"));
        else               list.add(0, new Budgets("none","Chọn ngân sách"));
        ArrayAdapter<Budgets> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBudget.setAdapter(ad);
    }

    private <T> void selectSpinnerItemById(Spinner spinner, String id) {
        if (id==null) return;
        for (int i=0; i<spinner.getCount(); i++) {
            Object obj = spinner.getItemAtPosition(i);
            String itemId = obj instanceof Categories
                    ? ((Categories)obj).getCategory_id()
                    : obj instanceof Budgets
                    ? ((Budgets)obj).getBudget_id()
                    : null;
            if (id.equals(itemId)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (v,y,m,d) -> etDate.setText(String.format(Locale.getDefault(),"%02d/%02d/%04d",d,m+1,y)),
                c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
}
