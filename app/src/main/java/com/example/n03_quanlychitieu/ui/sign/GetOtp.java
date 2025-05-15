package com.example.n03_quanlychitieu.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.utils.EmailSender;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;

public class GetOtp extends AppCompatActivity {

    private ProgressBar progress_bar_get_otp;
    private View overlayGetOtp;
    private TextInputEditText yourEmailEditText;
    private EmailSender emailSender;
    private String generatedOtp;
    private DatabaseHelper emailReal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_otp);

        progress_bar_get_otp = findViewById(R.id.progress_bar_get_otp);
        MaterialButton btn_get_otp = findViewById(R.id.getOtp);
        overlayGetOtp = findViewById(R.id.overlayGetOtp);
        yourEmailEditText = findViewById(R.id.yourEmail);
        emailSender = new EmailSender();
        emailReal = new DatabaseHelper(GetOtp.this);

        if (getIntent().hasExtra("email")) {
            String email = getIntent().getStringExtra("email");
            yourEmailEditText.setText(email);
        }

        btn_get_otp.setOnClickListener(v -> validateEmailAndGetOtp());
    }

    private void showLoading() {
        overlayGetOtp.setVisibility(View.VISIBLE);
        progress_bar_get_otp.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideLoading() {
        overlayGetOtp.setVisibility(View.GONE);
        progress_bar_get_otp.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void validateEmailAndGetOtp() {
        String email = yourEmailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            yourEmailEditText.setError("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            yourEmailEditText.setError("Please enter a valid email");
            return;
        }

        emailReal.checkEmail(email, new DatabaseHelper.EmailCallback() {
            @Override
            public void onSuccess(boolean emailExists) {
                showLoading();
                new Handler().postDelayed(() -> {
                    if (emailExists) {
                        generateAndSendOtp(email);
                    } else {
                        hideLoading();
                        Toast.makeText(GetOtp.this, "Don't have email in app" , Toast.LENGTH_SHORT).show();
                        yourEmailEditText.setText("");
                    }
                }, 2000);
            }

            @Override
            public void onError(String errorMessage) {
                hideLoading();
                Toast.makeText(GetOtp.this, "Have Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(GetOtp.this, BeginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

    }

    private void generateAndSendOtp(String email) {
        // Generate 6-digit OTP

        generatedOtp = String.valueOf(100000 + (int)(Math.random()*900000));
        String subject = "Mã xác nhận từ Quản Lý Chi Tiêu";
        String body = "Mã xác nhận của bạn: " + generatedOtp;

        new Thread(() -> {
            try {
                emailSender.sendEmail(email, subject, body);
                runOnUiThread(() -> {
                    hideLoading();
                    navigateToVerifyOtpScreen(email, generatedOtp, false);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(GetOtp.this, "Failed to send OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // chuyển dữ liệu sang Verify OTP
    public void navigateToVerifyOtpScreen(String email, String otp, boolean verification) {
        Intent intent = new Intent(this, VerifyOtp.class);
        intent.putExtra("email", email);
        intent.putExtra("otp", otp);
        intent.putExtra("verification", verification);
        startActivity(intent);
    }
}