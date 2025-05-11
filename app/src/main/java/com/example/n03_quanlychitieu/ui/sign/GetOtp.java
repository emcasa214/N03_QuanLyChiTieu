package com.example.n03_quanlychitieu.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import com.example.n03_quanlychitieu.R;
import com.google.android.material.button.MaterialButton;

public class GetOtp extends AppCompatActivity {

    private ProgressBar progress_bar_get_otp;
    private View overlayGetOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_otp);

        progress_bar_get_otp = findViewById(R.id.progress_bar_get_otp);
        MaterialButton btn_get_otp = findViewById(R.id.getOtp);
        overlayGetOtp = findViewById(R.id.overlayGetOtp);

        btn_get_otp.setOnClickListener(v -> get_otp());
    }

    private void get_otp() {
        overlayGetOtp.setVisibility(View.VISIBLE);
        progress_bar_get_otp.setVisibility(View.GONE);
        startActivity(new Intent(GetOtp.this, VerifyOtp.class));
    }
}
