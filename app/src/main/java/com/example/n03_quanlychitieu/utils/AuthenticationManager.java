package com.example.n03_quanlychitieu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.n03_quanlychitieu.model.Users;

public class AuthenticationManager {
    private static AuthenticationManager instance;
    private SharedPreferences sharedPreferences;
    private Users currentUser;



    private AuthenticationManager(Context context) {
        sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
    }

    public static synchronized AuthenticationManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthenticationManager(context.getApplicationContext());
        }
        return instance;
    }

    // Gọi khi đăng nhập thành công
    public void saveLoginState(Users user) {
        this.currentUser = user;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", user.getUser_id());
        editor.putString("username", user.getUsername());
        editor.putString("email", user.getEmail());
        editor.putString("avatar_url", user.getAvatar_url());
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }

    // Kiểm tra trạng thái đăng nhập
    public boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean("is_logged_in", false);
    }

    // Lấy thông tin user đã đăng nhập
    public Users getCurrentUser() {
        if (currentUser == null && isUserLoggedIn()) {
            currentUser = new Users();
            currentUser.setUser_id(sharedPreferences.getString("user_id", null));
            currentUser.setUsername(sharedPreferences.getString("username", null));
            currentUser.setEmail(sharedPreferences.getString("email", null));
            currentUser.setAvatar_url(sharedPreferences.getString("avatar_url", null));
        }
        return currentUser;
    }

    // Đăng xuất
    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        currentUser = null;
    }

    // Cập nhật thông tin người dùng hiện tại
    public void setCurrentUser(Users user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", user.getUsername());
        editor.putString("email", user.getEmail());
        editor.putString("avatar_url", user.getAvatar_url());

        editor.apply();
    }

}
