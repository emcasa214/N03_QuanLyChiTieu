package com.example.n03_quanlychitieu.ui.sign;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.n03_quanlychitieu.R;

public class BeginActivity extends AppCompatActivity {

    private LottieAnimationView animationView;
    private Button btnSignup, btnLogin;
    private View overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);


        animationView = findViewById(R.id.lottie_animation);
        btnLogin = findViewById(R.id.mb_login);
        btnSignup = findViewById(R.id.mb_signup);
        overlay = findViewById(R.id.overlay_begin);

        gradientView();
        setupButtonListeners();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Hiển thị lại các nút và ẩn overlay/animation
        btnSignup.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.VISIBLE);
        overlay.setVisibility(View.GONE);
        animationView.setVisibility(View.GONE);
        animationView.cancelAnimation();
    }

    private void setupButtonListeners() {
        btnSignup.setOnClickListener(v -> navigateTo(SignUp.class));
        btnLogin.setOnClickListener(v -> navigateTo(LogIn.class));
    }

    protected void gradientView() {
        final TextView gradientText = findViewById(R.id.gradientTextView);

        // Wait for layout pass to get the correct width
        gradientText.post(new Runnable() {
            @Override
            public void run() {
                float width = gradientText.getWidth();

                Shader textShader = new LinearGradient(
                        0, 0, width, 0,
                        new int[]{
                                0xFFFF6699, // Pink
                                0xFFFF9966, // Orange
                                0xFFBA9DEF, // Yellow
                        },
                        null,
                        Shader.TileMode.CLAMP
                );
                gradientText.getPaint().setShader(textShader);
                gradientText.invalidate();
            }
        });
    }

    private void navigateTo(Class<?> destination) {
        // Hiển thị loading animation
        btnSignup.setVisibility(View.INVISIBLE);
        btnLogin.setVisibility(View.INVISIBLE);
        overlay.setVisibility(View.VISIBLE);
        animationView.setVisibility(View.VISIBLE);
        animationView.bringToFront();
        animationView.setAnimation(R.raw.loading_animation);
        animationView.playAnimation();

        // Chuyển trang sau khi animation kết thúc
        animationView.postDelayed(() -> {
            Intent intent = new Intent(BeginActivity.this, destination);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 2000); // Thời gian đủ để animation chạy
    }

    //    Nhấn back 2 lần để thoát ứng dung
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finishAffinity(); // Đóng hoàn toàn ứng dụng
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Nhấn BACK lần nữa để thoát", Toast.LENGTH_SHORT).show();

        // Reset cờ sau 3 giây
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                doubleBackToExitPressedOnce = false, 3000);
    }

}
