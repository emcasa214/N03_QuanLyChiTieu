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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class GetOtp extends AppCompatActivity {

    private ProgressBar progress_bar_get_otp;
    private View overlayGetOtp;
    private TextInputEditText yourEmailEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_otp);

        progress_bar_get_otp = findViewById(R.id.progress_bar_get_otp);
        MaterialButton btn_get_otp = findViewById(R.id.getOtp);
        overlayGetOtp = findViewById(R.id.overlayGetOtp);
        yourEmailEditText = findViewById(R.id.yourEmail);
//        mAuth = FirebaseAuth.getInstance();

        // Xử lý sự kiện nút GET OTP
        btn_get_otp.setOnClickListener(v -> validateEmailAndGetOtp());
    }

    private void showLoading() {
        overlayGetOtp.setVisibility(View.VISIBLE);
        progress_bar_get_otp.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
//    // Khi ẩn loading
    private void hideLoading() {
        overlayGetOtp.setVisibility(View.GONE);
        progress_bar_get_otp.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void validateEmailAndGetOtp() {
        String email = yourEmailEditText.getText().toString().trim();

        // Check email
        if (TextUtils.isEmpty(email)) {
            yourEmailEditText.setError("Email is required");
            return;
        }

        showLoading();

        // CHeck email in firebase
//        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
//           if (task.isSuccessful() && task.getResult() != null && !task.getResult().getSignInMethods().isEmpty()) {
//               sendOtpToEmail(email); // Send OTP if email is valid
//           }else {
//               hideLoading();
//               Toast.makeText(this, "Email is invalid", Toast.LENGTH_SHORT).show();
//           }
//        }).addOnFailureListener(e -> {
//            hideLoading();
//            Toast.makeText(this, "Error connection", Toast.LENGTH_SHORT).show();
//        });
    }

    private void sendOtpToEmail(String email) {
        // Send email to reset password
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            hideLoading();
            if (task.isSuccessful()) {
                navigateToVerifyOtpScreen(email);
            }else {
                Toast.makeText(this, "Send OTP failess", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToVerifyOtpScreen(String email) {
        Intent intent = new Intent(this, VerifyOtp.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }
}
