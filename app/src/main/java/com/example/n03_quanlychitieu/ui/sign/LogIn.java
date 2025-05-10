package com.example.n03_quanlychitieu.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.n03_quanlychitieu.R;

public class LogIn extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        textSignup();
    }

    protected void textSignup() {
        TextView signup = findViewById(R.id.changeSignup);
        Intent intent = new Intent(LogIn.this, SignUp.class);
        BeginActivity begin = new BeginActivity();
        changeViewSignup(signup, intent);
    }

    protected void changeViewSignup(TextView text, Intent intent) {

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }
}
