package com.example.n03_quanlychitieu.ui.sign;

import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.adapter.BudgetAdapter;
import com.example.n03_quanlychitieu.dao.BudgetDAO;
import com.example.n03_quanlychitieu.dao.CategoryDAO;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.model.Budgets;
import com.example.n03_quanlychitieu.model.Categories;
import com.example.n03_quanlychitieu.utils.AuthenticationManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SetBudgets extends AppCompatActivity implements BudgetAdapter.OnBudgetClickListener {

    private AuthenticationManager authManager;
    private BudgetDAO budgetDAO;
    private CategoryDAO categoryDAO;
    private String currentUserId;

    private TextInputEditText etBudgetName, etAmount, etStartDate, etEndDate, etDescription;
    private AutoCompleteTextView actvCategory;
    private MaterialButton btnSave;
    private RecyclerView rvBudgets;
    private View emptyView;

    private BudgetAdapter budgetAdapter;
    private List<Budgets> budgetsList = new ArrayList<>();
    private List<Categories> categories = new ArrayList<>();

    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        // Init auth
        authManager = AuthenticationManager.getInstance(this);
        if (!authManager.isUserLoggedIn()) {
            finish();
            return;
        }
        currentUserId = authManager.getCurrentUser().getUser_id();

        // Init db
//        DatabaseHelper dbHelper = new
    }

    @Override
    public void onBudgetClick(Budgets budget) {

    }

    @Override
    public void onBudgetLongClick(Budgets budget) {

    }
}
