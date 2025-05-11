package com.example.n03_quanlychitieu.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.n03_quanlychitieu.R;

public class VerifyOtp extends AppCompatActivity {

    private ProgressBar progress_bar_verify;
    private View overlay_verify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        progress_bar_verify = findViewById(R.id.progress_bar_verify);
        overlay_verify = findViewById(R.id.overlayVerify);
        TextView resend_again = findViewById(R.id.resendOtp);

        resend_again.setOnClickListener(v -> ResendAgain());
    }

    private void ResendAgain() {
        overlay_verify.setVisibility(View.VISIBLE);
        progress_bar_verify.setVisibility(View.GONE);
        startActivity(new Intent(VerifyOtp.this, GetOtp.class));
    }
}
