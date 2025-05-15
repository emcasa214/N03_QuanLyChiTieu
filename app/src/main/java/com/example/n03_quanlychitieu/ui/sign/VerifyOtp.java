package com.example.n03_quanlychitieu.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.ui.main.MainActivity;
import com.example.n03_quanlychitieu.utils.EmailSender;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class VerifyOtp extends AppCompatActivity {

    private ProgressBar progress_bar_verify;
    private View overlay_verify;
    private TextInputEditText otpEditText;
    private String correctOtp;
    private String userEmail;
    private boolean verification;
    private EmailSender emailSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        correctOtp = intent.getStringExtra("otp");
        userEmail = intent.getStringExtra("email");
        verification = Boolean.parseBoolean(intent.getStringExtra("verification"));

        // Ánh xạ view
        progress_bar_verify = findViewById(R.id.progress_bar_verify);
        overlay_verify = findViewById(R.id.overlayVerify);
        otpEditText = findViewById(R.id.yourOtp);
        TextView resend_again = findViewById(R.id.resendOtp);
        MaterialButton verifyButton = findViewById(R.id.getOtp);
        emailSender = new EmailSender();

        // Xử lý sự kiện
        resend_again.setOnClickListener(v -> resendOtp());
        verifyButton.setOnClickListener(v -> verifyOtp());
    }

    private void showLoading() {
        overlay_verify.setVisibility(View.VISIBLE);
        progress_bar_verify.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideLoading() {
        overlay_verify.setVisibility(View.GONE);
        progress_bar_verify.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void verifyOtp() {
        String enteredOtp = otpEditText.getText().toString().trim();

        if (TextUtils.isEmpty(enteredOtp)) {
            otpEditText.setError("Please enter OTP");
            return;
        }

        showLoading();

        if (enteredOtp.equals(correctOtp)) {
            // OTP đúng, chuyển sang màn hình đổi mật khẩu
            if (!verification) {
                navigateToForgotPassword();
            } else {
                navigateToMain();
            }
        } else {
            hideLoading();
            otpEditText.setError("Invalid OTP");
            Toast.makeText(this, "The OTP you entered is incorrect", Toast.LENGTH_SHORT).show();
        }
    }

    // intent.putExtra để gửi dữ liệu cùng với màn hình chuyển động
    private void navigateToForgotPassword() {
        hideLoading();
        Intent intent = new Intent(this, ForgotPassword.class);
        intent.putExtra("email", userEmail);
        startActivity(intent);
        finish(); // Đóng màn hình verify để người dùng không quay lại bằng nút back
    }

    private void navigateToMain() {
        hideLoading();
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);
        finish(); // Đóng màn hình verify để người dùng không quay lại bằng nút back
    }

    private void resendOtp() {
        showLoading();
        // Generate 6-digit OTP

        String generatedOtp = String.valueOf(100000 + (int) (Math.random() * 900000));
        String subject = "Mã xác nhận từ Quản Lý Chi Tiêu";
        String body = "Mã xác nhận của bạn: " + generatedOtp;
        correctOtp = generatedOtp;

        new Thread(() -> {
            try {
                emailSender.sendEmail(userEmail, subject, body);
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(VerifyOtp.this,
                            "Đã gửi lại mã OTP thành công",
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(VerifyOtp.this, "Failed to send again OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}