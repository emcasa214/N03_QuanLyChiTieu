package com.example.n03_quanlychitieu.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.utils.AuthenticationManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;


public class ForgotPassword extends AppCompatActivity {

    private TextInputEditText etNewPassword, etConfirmPassword;
    private ProgressBar progressBar;
    private View overlay;
    private String userEmail;
    private SignUp extend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Nhận email từ Intent
        userEmail = getIntent().getStringExtra("email");

        // Ánh xạ view
        etNewPassword = findViewById(R.id.new_password);
        etConfirmPassword = findViewById(R.id.confirm_password);
        progressBar = findViewById(R.id.progress_bar_forgot_password);
        overlay = findViewById(R.id.overlayForgotPassword);
        MaterialButton btnReset = findViewById(R.id.btn_reset_password);
        extend = new SignUp();

        btnReset.setOnClickListener(v -> resetPassword());
    }

    private void showLoading() {
        overlay.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideLoading() {
        overlay.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Password is required");
            return;
        }

        if (!extend.isValidPassword(newPassword)) {
            etNewPassword.setError("Password must contain at least:\n- a special character (!@#$%^&*()_+)\n- a capital letter (A-Z)\n- a number (0-9)");
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        showLoading();

        // Mã hóa mật khẩu mới
        String hashedPassword = extend.hashPassword(newPassword);

        // Thực hiện đổi mật khẩu trong database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.resetPassword(userEmail, hashedPassword, new DatabaseHelper.ResetPasswordCallback() {
            @Override
            public void onSuccess(int rowsAffected) {
                runOnUiThread(() -> {
                    hideLoading();

                    if (rowsAffected > 0) {
                        // Cập nhật thành công
                        Toast.makeText(ForgotPassword.this, "Password updated successfully", Toast.LENGTH_SHORT).show();

                        // Đăng xuất người dùng nếu đang đăng nhập
                        AuthenticationManager authManager = AuthenticationManager.getInstance(ForgotPassword.this);
                        if (authManager.getCurrentUser() != null &&
                                authManager.getCurrentUser().getEmail().equals(userEmail)) {
                            authManager.logout();
                        }

                        // Quay về màn hình đăng nhập
                        Intent intent = new Intent(ForgotPassword.this, LogIn.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ForgotPassword.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(ForgotPassword.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}