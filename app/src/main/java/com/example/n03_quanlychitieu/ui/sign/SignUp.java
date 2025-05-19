package com.example.n03_quanlychitieu.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.db.DatabaseHelper;

import com.example.n03_quanlychitieu.model.Users;
import com.example.n03_quanlychitieu.ui.main.MainActivity;
import com.example.n03_quanlychitieu.utils.AuthenticationManager;
import com.example.n03_quanlychitieu.utils.EmailSender;
import com.example.n03_quanlychitieu.utils.GoogleAuthHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.button.MaterialButton;

import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.UUID;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class SignUp extends AppCompatActivity implements GoogleAuthHelper.GoogleSignInListener {

    private final DatabaseHelper db = new DatabaseHelper(this);
    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private ProgressBar progressBar;
    private View overlay;
    private EmailSender emailSender;
    private GoogleAuthHelper googleAuthHelper;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Kêt nối Google Auth Helper
        googleAuthHelper = new GoogleAuthHelper(
                this,
                getString(R.string.default_web_client_id),
                this
        );

//      Initialize Activity Result Launcher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        googleAuthHelper.handleSignInResult(
                                GoogleSignIn.getSignedInAccountFromIntent(data)
                        );
                    }
                }
        );

        // Mapping
        etUsername = findViewById(R.id.username);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etConfirmPassword = findViewById(R.id.confirm);
        progressBar = findViewById(R.id.progress_bar);
        btnSignUp = findViewById(R.id.btnSignUp);
        overlay = findViewById(R.id.overlay_signup);
        emailSender = new EmailSender();

        btnSignUp.setOnClickListener(v -> {
            showLoading();
            new Handler().postDelayed(() -> {
                hideLoading();
                registerUser();
            }, 1000);
        });

        // Xử lý click nút Google
        MaterialButton btnGoogle = findViewById(R.id.btnGoogle);
        btnGoogle.setOnClickListener(v -> googleSignInLauncher.launch(googleAuthHelper.getSignInIntent()));

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

                generateAndSendOtp(userID, username, email, password);
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                btnSignUp.setEnabled(true);
                Toast.makeText(SignUp.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void generateAndSendOtp(String userID, String username, String email, String password) {
        // Generate 6-digit OTP
        showLoading();
        String generatedOtp = String.valueOf(100000 + (int) (Math.random() * 900000));
        String subject = "Mã xác nhận từ Quản Lý Chi Tiêu";
        String body = "Mã xác nhận của bạn: " + generatedOtp;

        new Thread(() -> {
            try {
                emailSender.sendEmail(email, subject, body);
                runOnUiThread(() -> {
                    hideLoading();
                    navigateToVerifyOtpScreen(userID, username, email, password, generatedOtp);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(SignUp.this, "Failed to send OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    public void navigateToVerifyOtpScreen(String userID, String username, String email, String password, String otp) {
        Intent intent = new Intent(this, VerifyOtp.class);
        intent.putExtra("email", email);
        intent.putExtra("otp", otp);
        intent.putExtra("userID", userID);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        startActivity(intent);
    }

    public boolean isValidPassword(String password) {
        // Regex kiểm tra:
        // - Ít nhất 1 ký tự đặc biệt (!@#$%^&*()_+)
        // - Ít nhất 1 chữ hoa (A-Z)
        // - Ít nhất 1 số (0-9)
        // - Ít nhất 8 ký tự
        String passwordPattern =
                "^(?=.*[!@#$%^&*()_+])(?=.*[A-Z])(?=.*\\d).{8,}$";

        return password.matches(passwordPattern);
    }

    public String hashPassword(String rawPassword) {
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

    @Override
    public void onSuccess(GoogleSignInAccount account) {
        showLoading();
        Log.d("GoogleSignIn", "Google sign-in successful: " + account.getEmail());
    }

    @Override
    public void onFailure(String errorMessage) {
        hideLoading();
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e("GoogleSignIn", "Error: " + errorMessage);
    }

    @Override
    public void onFirebaseAuthSuccess(String uid, GoogleSignInAccount account) {
    // Xử lý sau khi xác thực Firebase thành công
        db.checkEmail(account.getEmail(), new DatabaseHelper.EmailCallback() {
            @Override
            public void onSuccess(boolean emailExists) {
                if (emailExists) {
                    handleExistingUser(account);
                } else {
                    createGoogleUser(uid, account);
                }
            }

            @Override
            public void onError(String errorMessage) {
                hideLoading();
                Toast.makeText(SignUp.this, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void handleExistingUser(GoogleSignInAccount account) {
        db.getUserByEmail(account.getEmail(), new DatabaseHelper.GetUserByEmailCallback() {
            @Override
            public void onUserLoaded(Users user) {
                hideLoading();
                if (user.getPassword() == null || user.getPassword().isEmpty()) {
                    // User đã đăng ký bằng Google -> Cho phép đăng nhập
                    AuthenticationManager.getInstance(SignUp.this).saveLoginState(user);
                    startActivity(new Intent(SignUp.this, MainActivity.class));
                    finish();
                } else {
                    // User đăng ký bằng email -> Yêu cầu đăng nhập bằng password
                    Toast.makeText(SignUp.this,
                            "Email đã được đăng ký bằng mật khẩu. Vui lòng đăng nhập bằng mật khẩu",
                            Toast.LENGTH_SHORT).show();
                    googleAuthHelper.signOut(); // Đăng xuất Google
                }
            }

            @Override
            public void onUserNotFound() {
                hideLoading();
                Toast.makeText(SignUp.this,
                        "Không tìm thấy thông tin người dùng",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                hideLoading();
                Toast.makeText(SignUp.this,
                        "Lỗi hệ thống: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createGoogleUser(String userId, GoogleSignInAccount account) {
        String username = account.getGivenName() != null ? account.getGivenName() :
                account.getEmail().split("@")[0];
        String avatarUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;
        db.addGoogleUserAsync(userId, username, account.getEmail(), avatarUrl,
                new DatabaseHelper.UserCallback() {
                    @Override
                    public void onSuccess() {
                        Users user = new Users(
                                userId,
                                username,
                                account.getEmail(),
                                null, // Password null cho user Google
                                avatarUrl,
                                null

                        );

                        AuthenticationManager.getInstance(SignUp.this).saveLoginState(user);
                        hideLoading();
                        startActivity(new Intent(SignUp.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        hideLoading();
                        Toast.makeText(SignUp.this,
                                "Tạo tài khoản thất bại: " + errorMessage,
                                Toast.LENGTH_SHORT).show();
                        googleAuthHelper.signOut(); // Đăng xuất nếu có lỗi
                    }
                });
    }
}