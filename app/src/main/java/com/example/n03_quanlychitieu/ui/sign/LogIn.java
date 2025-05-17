package com.example.n03_quanlychitieu.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.model.Users;
import com.example.n03_quanlychitieu.ui.main.MainActivity;
import com.example.n03_quanlychitieu.utils.AuthenticationManager;

import com.google.android.material.button.MaterialButton;


public class LogIn extends AppCompatActivity {
    private final DatabaseHelper db = new DatabaseHelper(this);
    private EditText etUsernameOrEmail, etPasswordLogin;
    private TextView tvForgotPassword;
    private Button btnLogin;
    private ProgressBar progressBarLogin;
    private View overlay;
    private MaterialButton googleSignIn;
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        etUsernameOrEmail = findViewById(R.id.inforEorU);
        etPasswordLogin = findViewById(R.id.passwordLogin);
        btnLogin = findViewById(R.id.btnLogIn);
        tvForgotPassword = findViewById(R.id.forgotPassword);
        progressBarLogin = findViewById(R.id.progress_bar_login);
        overlay = findViewById(R.id.overlay_login);
        googleSignIn = findViewById(R.id.btn_google_signin);

        textSignup();
        tvForgotPassword.setOnClickListener(v -> ForgotPassword());

        btnLogin.setOnClickListener(v -> {
            showLoading();
            new Handler().postDelayed(() -> {
                hideLoading();
                loginUser();
            }, 2000);
        });

    }

    private void ForgotPassword() {
        try {
            showLoading(); // Sử dụng hàm có sẵn để thống nhất cách hiển thị loading

            // Chuyển sang màn hình OTP sau 1000ms để người dùng kịp thấy hiệu ứng loading
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(LogIn.this, GetOtp.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Xóa các activity cùng task phía trên
                startActivity(intent);
                finish(); // Đóng LoginActivity
                hideLoading(); // Đảm bảo ẩn loading nếu có lỗi
            }, 1000);

        } catch (Exception e) {
            hideLoading();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Animation
    // Khi cần hiển thị loading
    private void showLoading() {
        overlay.setVisibility(View.VISIBLE);
        progressBarLogin.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        googleSignIn.setEnabled(false);
    }

    private void hideLoading() {
        overlay.setVisibility(View.GONE);
        progressBarLogin.setVisibility(View.GONE);
        btnLogin.setEnabled(true);
        googleSignIn.setEnabled(true);
    }
    // Animation
    private void loginUser() {
        String usernameOrEmail = etUsernameOrEmail.getText().toString().trim();
        String passwordLogin = etPasswordLogin.getText().toString().trim();

        if (TextUtils.isEmpty(usernameOrEmail) || TextUtils.isEmpty(passwordLogin)) {
            Toast.makeText(this, "Please enter complete information", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị loading
        progressBarLogin.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        db.getUserAsync(usernameOrEmail, passwordLogin, new DatabaseHelper.GetUserCallback() {
            @Override
            public void onUserLoaded(Users user) {
                AuthenticationManager.getInstance(LogIn.this).saveLoginState(user);
                progressBarLogin.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(LogIn.this, "Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LogIn.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onUserNotFound() {
                progressBarLogin.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(LogIn.this, "Incorrect username/email or password", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                progressBarLogin.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(LogIn.this, "Login Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LogIn.this, BeginActivity.class));
                finish();
            }
        });
    }

    protected void textSignup() {
        TextView signup = findViewById(R.id.changeSignup);
        Intent intent = new Intent(LogIn.this, SignUp.class);
        changeViewSignup(signup, intent);
    }

    protected void changeViewSignup(TextView text, Intent intent) {
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });
    }
}
