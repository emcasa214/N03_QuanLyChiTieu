package com.example.n03_quanlychitieu.ui.user;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.n03_quanlychitieu.R;
import com.example.n03_quanlychitieu.db.DatabaseContract;
import com.example.n03_quanlychitieu.model.Users;
import com.example.n03_quanlychitieu.ui.main.MainActivity;
import com.example.n03_quanlychitieu.ui.sign.BeginActivity;
import com.example.n03_quanlychitieu.utils.AuthenticationManager;
import com.example.n03_quanlychitieu.db.DatabaseHelper;
import com.example.n03_quanlychitieu.utils.EmailSender;

import java.io.IOException;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class UserProfileActivity extends AppCompatActivity {
    DatabaseHelper db = new DatabaseHelper(this);
    private TextView tvUsername, tvEmail;
    private ImageButton btnBack, btnEdit;
    private Button btnChangePw, btnDangXuat;
    private Users currentUser;
    AuthenticationManager auth;

    //component cua changeinfo
    EditText edChangeGmail, edChangeName;
    Button btnGui, btnHuy;

    //component cua confirm pass
    EditText edPassConfirmInfo;
    Button btnLuuConfirm, btnHuyConfirm;
    String id;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageButton btnEditAvatar;
    private ImageView imgAvatar;
    private Uri avatarUri;
    private Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initViews();
        refreshUserInfo();
        setupListeners();

        btnEditAvatar.setOnClickListener(v -> openImageChooser());


        onResume();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        btnBack = findViewById(R.id.btnQuayLai);
        btnEdit = findViewById(R.id.btnSua);
        btnChangePw = findViewById(R.id.btnMK);
        btnDangXuat = findViewById(R.id.btnDangXuat);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        imgAvatar = findViewById(R.id.imgAvatar);
        currentUser = AuthenticationManager.getInstance(this).getCurrentUser();
        id = currentUser.getUser_id();
    }
    private void refreshUserInfo() {
        currentUser = db.getUserById(id);
        AuthenticationManager.getInstance(this).saveLoginState(currentUser);
        setupUserInfo();
    }
    private void setupUserInfo() {
        try {
            if (currentUser != null) {
                tvUsername.setText(currentUser.getUsername());
//                tvEmail.setText(currentUser.getEmail());
//                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(currentUser.getAvatar_url()));
//                imgAvatar.setImageBitmap(bitmap);
                Glide.with(this)
                        .load(currentUser.getAvatar_url())
                        .placeholder(R.drawable.ic_default_avatar) // Ảnh tạm thời
                        .error(R.drawable.ic_default_avatar)       // Ảnh lỗi
                        .into(imgAvatar);
                tvEmail.setText("Hello");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnEdit.setOnClickListener(v -> showChangeInfo());

        btnChangePw.setOnClickListener(v -> showChangePasswordDialog());

        auth = AuthenticationManager.getInstance(UserProfileActivity.this);
        btnDangXuat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auth.isUserLoggedIn()) {
                    auth.logout();
                    startActivity(new Intent(UserProfileActivity.this, BeginActivity.class));
                }
            }
        });
    }
    private void showChangeInfo(){
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_changeinfo, null);
        edChangeName = dialogView.findViewById(R.id.edChangeName);
        edChangeGmail = dialogView.findViewById(R.id.edChangeGmail);
        btnGui = dialogView.findViewById(R.id.btnGui);
        btnHuy = dialogView.findViewById(R.id.btnHuy);
        edChangeName.setText(currentUser.getUsername());
        edChangeGmail.setText(currentUser.getEmail());
        edChangeName.requestFocus();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        btnGui.setOnClickListener(v -> {
            String name = edChangeName.getText().toString().trim();
            String gmail = edChangeGmail.getText().toString().trim();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(gmail)) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (name.length() < 3) {
                Toast.makeText(this, "Tên phải có ít nhất 3 ký tự.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(gmail).matches()) {
                Toast.makeText(this, "Email không hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }
            db.checkUserForUpdate(currentUser.getUser_id(), name, gmail, new DatabaseHelper.UserCheckCallback() {
                @Override
                public void onUsernameExists() {
                    Toast.makeText(UserProfileActivity.this, "Tên người dùng đã tồn tại.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onEmailExists() {
                    Toast.makeText(UserProfileActivity.this, "Email đã tồn tại.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAvailable() {
                    // Nếu không bị trùng, hiển thị dialog xác nhận mật khẩu
                    showPasswordConfirmationDialog(name, gmail, dialog);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(UserProfileActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        });
        btnHuy.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showPasswordConfirmationDialog(String name, String gmail, AlertDialog dialog) {
        View passview = LayoutInflater.from(this).inflate(R.layout.dialog_confirmpassword, null);
        edPassConfirmInfo = passview.findViewById(R.id.edPassConfirmInfo);
        btnLuuConfirm = passview.findViewById(R.id.btnLuuConfirm);
        btnHuyConfirm = passview.findViewById(R.id.btnHuyConfirm);
        AlertDialog passdialog = new AlertDialog.Builder(this)
                .setView(passview)
                .setCancelable(false)
                .create();
        btnLuuConfirm.setOnClickListener(v1 -> {

            String pass = edPassConfirmInfo.getText().toString().trim();

            if (TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!checkCurrentPassword(pass)) {
                Toast.makeText(this, "Mật khẩu hiện tại không đúng.", Toast.LENGTH_SHORT).show();
                return;
            }
            Boolean check = db.updateUser(id, name, gmail);
            if (check) {
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                passdialog.dismiss();
                refreshUserInfo();
            }
            else {
                Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                return;
            }
        });
        btnHuyConfirm.setOnClickListener(v12 -> passdialog.dismiss());
        passdialog.show();
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
            if (!isValidPassword(newPw)) {
                edtNew.setError("Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
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

    private boolean isValidPassword(String password) {
        // Regex kiểm tra:
        // - Ít nhất 1 ký tự đặc biệt (!@#$%^&*()_+)
        // - Ít nhất 1 chữ hoa (A-Z)
        // - Ít nhất 1 số (0-9)
        // - Ít nhất 8 ký tự
        String passwordPattern =
                "^(?=.*[!@#$%^&*()_+])(?=.*[A-Z])(?=.*\\d).{8,}$";

        return password.matches(passwordPattern);
    }
    private interface VerificationCallback {
        void onVerified(String code);
    }
    @Override
    protected void onResume() {
        super.onResume();
        refreshUserInfo(); // Cập nhật thông tin người dùng và giao diện
    }

    // Thay đổi avatar
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh đại diện"), PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            avatarUri = data.getData();
            updateAvatar(avatarUri);
        }
    }
    private void updateAvatar(Uri uri) {
        // Hiển thị ảnh mới
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            imgAvatar.setImageBitmap(bitmap);
            // Lưu avatar vào SharedPreferences
            saveAvatarToPreferences(uri.toString());
            // Cập nhật avatar vào database
            updateAvatarInDatabase(uri.toString());
        } catch (IOException e) {
            Toast.makeText(this, "Lỗi khi tải ảnh", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void saveAvatarToPreferences(String uriString) {
        SharedPreferences.Editor editor = getSharedPreferences("auth_prefs", MODE_PRIVATE).edit();
        editor.putString("avatar_url", uriString);
        editor.apply();

        // Cập nhật currentUser
        if (currentUser != null) {
            currentUser.setAvatar_url(uriString);
            auth.setCurrentUser(currentUser);
        }
    }
    private void updateAvatarInDatabase(String uriString) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Users.COLUMN_AVATAR_URL, uriString);

        int rowsAffected = db.update(
                DatabaseContract.Users.TABLE_NAME,
                values,
                DatabaseContract.Users.COLUMN_USER_ID + " = ?",
                new String[]{currentUser.getUser_id()}
        );

        db.close();

        if (rowsAffected > 0) {
            Toast.makeText(this, "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Cập nhật ảnh đại diện thất bại", Toast.LENGTH_SHORT).show();
        }
    }
}
