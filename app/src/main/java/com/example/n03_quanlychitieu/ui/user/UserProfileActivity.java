package com.example.n03_quanlychitieu.ui.user;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.model.Users;
import com.example.n03_quanlychitieu.utils.AuthenticationManager;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.utils.EmailSender;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class UserProfileActivity extends AppCompatActivity {
    private TextView tvUsername, tvEmail;
    private ImageButton btnBack, btnEdit;
    private Button btnChangePw;
    private Users currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initViews();
        setupUserInfo();
        setupListeners();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        btnBack = findViewById(R.id.btnQuayLai);
//        btnEdit = findViewById(R.id.btnSua);
        btnChangePw = findViewById(R.id.btnMK);
        currentUser = AuthenticationManager.getInstance(this).getCurrentUser();
    }

    private void setupUserInfo() {
        if (currentUser != null) {
            tvUsername.setText(currentUser.getUsername());
            tvEmail.setText(currentUser.getEmail());
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
//        btnEdit.setOnClickListener(v -> {
//        });
        btnChangePw.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_changepassword, null);
        EditText edtCurrent = dialogView.findViewById(R.id.edtHT);
        EditText edtNew = dialogView.findViewById(R.id.edtNew);
        EditText edtConfirm = dialogView.findViewById(R.id.edtCF);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnSave.setOnClickListener(v -> {
            String currentPw = edtCurrent.getText().toString().trim();
            String newPw = edtNew.getText().toString().trim();
            String confirmPw = edtConfirm.getText().toString().trim();

            if (TextUtils.isEmpty(currentPw) || TextUtils.isEmpty(newPw) || TextUtils.isEmpty(confirmPw)) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPw.equals(confirmPw)) {
                edtConfirm.setError("Mật khẩu không khớp");
                return;
            }
            if (!checkCurrentPassword(currentPw)) {
                Toast.makeText(this, "Mật khẩu hiện tại không đúng.", Toast.LENGTH_SHORT).show();
                return;
            }

            // gửi mã xác nhận, sau đó lưu mật khẩu
            sendVerificationCode(currentUser.getEmail(), code -> {
                // callback sau khi xác nhận thành công
                saveNewPassword(newPw);
                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private boolean checkCurrentPassword(String currentPw) {
        // Lấy mật khẩu đã mã hóa từ cơ sở dữ liệu
        String stored = new DatabaseHelper(this).getPasswordForUser(currentUser.getUser_id());
        Log.d("pass", stored);

        // Kiểm tra nếu không tìm thấy mật khẩu
        if (stored == null || stored.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mật khẩu. Vui lòng thiết lập mật khẩu mới.", Toast.LENGTH_SHORT).show();
            return false; // Trả về false để ngăn người dùng tiếp tục
        }

        // So sánh mật khẩu hiện tại với mật khẩu đã mã hóa
        boolean isVerified = BCrypt.verifyer().verify(currentPw.toCharArray(), stored).verified;
        if (!isVerified) {
            Toast.makeText(this, "Mật khẩu hiện tại không đúng.", Toast.LENGTH_SHORT).show();
        }
        return isVerified;
    }
    private void sendVerificationCode(String email, VerificationCallback callback) {
        String code = String.valueOf(100000 + (int)(Math.random()*900000));
        String subject = "Mã xác nhận từ Quản Lý Chi Tiêu";
        String body = "Mã xác nhận của bạn: " + code;

        new Thread(() -> {
            try {
                EmailSender sender = new EmailSender();
                sender.sendEmail(email, subject, body);
                runOnUiThread(() -> showCodeDialog(code, callback));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Gửi email thất bại.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showCodeDialog(String correctCode, VerificationCallback callback) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Nhập mã xác nhận");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        b.setView(input);
        b.setCancelable(false);
        b.setPositiveButton("Xác nhận", (dialog, which) -> {
            if (correctCode.equals(input.getText().toString().trim())) {
                callback.onVerified(correctCode);
            } else {
                Toast.makeText(this, "Mã không đúng.", Toast.LENGTH_SHORT).show();
            }
        });
        b.setNegativeButton("Hủy", (d, w) -> d.dismiss());
        b.show();
    }

    private void saveNewPassword(String newPw) {
        String hashedPassword = BCrypt.withDefaults().hashToString(12, newPw.toCharArray());

        // Cập nhật mật khẩu trong cơ sở dữ liệu
        DatabaseHelper db = new DatabaseHelper(this);
        SQLiteDatabase sql = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password", hashedPassword);
        sql.update("Users", cv, "user_id = ?", new String[]{currentUser.getUser_id()});
        sql.close();

        // Cập nhật mật khẩu trong AuthenticationManager
        currentUser.setPassword(hashedPassword);
        AuthenticationManager.getInstance(this).saveLoginState(currentUser);
    }

    private interface VerificationCallback {
        void onVerified(String code);
    }
}
