package com.example.n03_quanlychitieu.model;

public class Users {
    private String user_id;
    private String username;
    private String email;
    private String password;
    private String avatar_url;
    private String created_at;

    public Users() {
    }
    public Users(String user_id, String username, String email, String password, String avatar_url, String created_at) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.avatar_url = avatar_url;
        this.created_at = created_at;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Users{" +
                "user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", avatar_url='" + avatar_url + '\'' +
                ", created_at='" + created_at + '\'' +
                '}';
    }
}
