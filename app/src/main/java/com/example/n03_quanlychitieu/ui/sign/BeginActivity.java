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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.n03_quanlychitieu.R;

public class BeginActivity extends AppCompatActivity {

    private LottieAnimationView animationView;
    private Button btnSignup, btnLogin;
    private View overlay;
    private boolean isProcessing = false;


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

//        btnSignup.setOnClickListener(v -> handleSignUp());
//        btnLogin.setOnClickListener(v -> handleLogIn());

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Hiển thị lại các nút và ẩn overlay/animation
        btnSignup.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.VISIBLE);
        overlay.setVisibility(View.GONE);
        animationView.setVisibility(View.GONE);
        animationView.cancelAnimation(); // Dừng animation nếu đang chạy
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hủy animation khi Activity bị tạm dừng
        if (animationView != null) {
            animationView.cancelAnimation();
        }
    }

    private void setupButtonListeners() {
        btnSignup.setOnClickListener(v -> {
            if (!isProcessing) {
                isProcessing = true;
                handleSignUp();
            }
        });

        btnLogin.setOnClickListener(v -> {
            if (!isProcessing) {
                isProcessing = true;
                handleLogIn();
            }
        });
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

    protected void handleSignUp() {
        Intent intent = new Intent(BeginActivity.this, SignUp.class);
        changeView(intent);
    }

    protected void handleLogIn() {
        Intent intent = new Intent(BeginActivity.this, LogIn.class);
        changeView(intent);
    }

    protected void changeView(Intent intent) {
        btnSignup.setVisibility(View.INVISIBLE);
        btnLogin.setVisibility(View.INVISIBLE);

        overlay.setVisibility(View.VISIBLE);
        animationView.setVisibility(View.VISIBLE);
        animationView.bringToFront();

        animationView.setAnimation(R.raw.loading_animation);
        animationView.playAnimation();
        animationView.setTag(intent);


        animationView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Intent currentIntent = (Intent) animationView.getTag();
                if (currentIntent != null && isProcessing) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        startActivity(currentIntent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        // Reset trạng thái sau khi chuyển màn hình
                        isProcessing = false;
                    }, 200);
                }
            }
        });
    }

}
