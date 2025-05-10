package com.example.n03_quanlychitieu.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.model.Users;

import java.util.UUID;

public class SignUp extends AppCompatActivity {

    private DatabaseHelper db = new DatabaseHelper(this);
    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvLogin;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        textLogin();
    }

    protected void khoitao() {
        Users user = (Users) getIntent().getSerializableExtra("users");
        user_id = user.getUser_id();
        etUsername = findViewById(R.id.username);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etConfirmPassword = findViewById(R.id.confirm);


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

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

}