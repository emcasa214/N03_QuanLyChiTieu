package com.example.n03_quanlychitieu.ui.sign;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.n03_quanlychitieu.R;

public class BeginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);

        gradientView();
        handleSignUp();
        handleLogIn();
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
        Button btnSignup = findViewById(R.id.mb_signup);
        Intent intent = new Intent(BeginActivity.this, SignUp.class);
        changeView(btnSignup, intent);
    }

    protected void handleLogIn() {
        Button btnLogin = findViewById(R.id.mb_login);
        Intent intent = new Intent(BeginActivity.this, LogIn.class);
        changeView(btnLogin, intent);
    }

    protected void changeView(Button btn, Intent intent) {
        LottieAnimationView animationView = findViewById(R.id.lottie_animation);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationView.setVisibility(View.VISIBLE);
                animationView.bringToFront();

                animationView.setAnimation(R.raw.loading_animation);
                animationView.setProgress(0f); // Reset về đầu
                animationView.playAnimation();

                animationView.addAnimatorListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                });
            }
        });
    }

}
