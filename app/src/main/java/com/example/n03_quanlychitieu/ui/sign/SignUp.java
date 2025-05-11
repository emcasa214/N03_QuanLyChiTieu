package com.example.n03_quanlychitieu.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.model.Users;
import com.example.n03_quanlychitieu.ui.main.MainActivity;
import com.example.n03_quanlychitieu.utils.AuthenticationManager;

import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.UUID;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class SignUp extends AppCompatActivity {

    private final DatabaseHelper db = new DatabaseHelper(this);
    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private ProgressBar progressBar;

    private View overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Mapping
        etUsername = findViewById(R.id.username);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etConfirmPassword = findViewById(R.id.confirm);
        progressBar = findViewById(R.id.progress_bar);
        btnSignUp = findViewById(R.id.btnSignUp);
        overlay = findViewById(R.id.overlay_signup);

        btnSignUp.setOnClickListener(v -> {
            showLoading();
            new Handler().postDelayed(() -> {
                hideLoading();
                registerUser();
            }, 2000);
        });

        // Handle event

        textLogin();
    }

    // Animation
    // Khi cần hiển thị loading
    private void showLoading() {
        overlay.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    // Khi ẩn loading
    private void hideLoading() {
        overlay.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void registerUser() {
        // Get data from form
        String userID = UUID.randomUUID().toString();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate data
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            return;
        }

        if (username.length() <= 2) {
            etUsername.setError("Username must be at least 3 characters");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        if (!isValidPassword(password)) {
            etPassword.setError("Password must contain at least:\n- a special character (!@#$%^&*()_+)\n- a capital letter (A-Z)\n- a number (0-9)");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Check username/email
        db.checkUser(username, email, new DatabaseHelper.UserCheckCallback() {
            @Override
            public void onUsernameExists() {
                progressBar.setVisibility(View.GONE);
                btnSignUp.setEnabled(true);
                etUsername.setError("Username already exists");
                etUsername.requestFocus();
            }

            @Override
            public void onEmailExists() {
                progressBar.setVisibility(View.GONE);
                btnSignUp.setEnabled(true);
                etEmail.setError("Email already registered");
                etEmail.requestFocus();
            }

            @Override
            public void onAvailable() {
                // Tiến hành đăng ký user
                String hashPassword = hashPassword(password);
                db.addUserAsync(userID, username, email, hashPassword, new DatabaseHelper.UserCallback() {
                    @Override
                    public void onSuccess() {
                        handleAutoLogin(email, password);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        progressBar.setVisibility(View.GONE);
                        btnSignUp.setEnabled(true);
                        Toast.makeText(SignUp.this, "Registration Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                btnSignUp.setEnabled(true);
                Toast.makeText(SignUp.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleAutoLogin(String usernameOrEmail, String password) {
        db.getUserAsync(usernameOrEmail, password, new DatabaseHelper.GetUserCallback() {
            @Override
            public void onUserLoaded(Users user) {
                AuthenticationManager.getInstance(SignUp.this).saveLoginState(user);

                progressBar.setVisibility(View.GONE);
                btnSignUp.setEnabled(true);
                Toast.makeText(SignUp.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignUp.this, MainActivity.class));
                finish();
            }

            @Override
            public void onUserNotFound() {}

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                btnSignUp.setEnabled(true);
                Toast.makeText(SignUp.this, "Login Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignUp.this, BeginActivity.class));
            }
        });
    }

    private boolean isValidPassword(String password) {
        // Regex kiểm tra:
        // - Ít nhất 1 ký tự đặc biệt (!@#$%^&*()_+)
        // - Ít nhất 1 chữ hoa (A-Z)
        // - Ít nhất 1 số (0-9)
        // - Ít nhất 8 ký tự
        String passwordPattern =
                "^(?=.*[!@#$%^&*()_+])(?=.*[A-Z])(?=.*\\d).{8,}$";

        return password.matches(passwordPattern);
    }
    private String hashPassword(String rawPassword) {
        return BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
    }

    protected void textLogin() {
        TextView login = findViewById(R.id.changeLogin);
        Intent intent = new Intent(SignUp.this, LogIn.class);
        changeViewLogin(login, intent);
    }

    protected void changeViewLogin(TextView text, Intent intent) {

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });
    }

}