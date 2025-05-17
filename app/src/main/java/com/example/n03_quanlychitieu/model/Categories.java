package com.example.n03_quanlychitieu.model;

import java.io.Serializable;

public class Categories implements Serializable {
    private String category_id;
    private String name;
    private String icon;
    private String color;
    private String type;
    private String user_id;

    public Categories() {
    }
    public Categories(String category_id, String name, String icon, String color, String type, String user_id) {
        this.category_id = category_id;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.type = type;
        this.user_id = user_id;
    }

    public Categories(String id, String name){
        this.category_id = id;
        this.name = name;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    // Getter và Setter cho user_id
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
//    @Override
//    public String toString() {
//        return "Categories{" +
//                "category_id='" + category_id + '\'' +
//                ", name='" + name + '\'' +
//                ", icon='" + icon + '\'' +
//                ", color='" + color + '\'' +
//                ", type='" + type + '\'' +
//                ", user_id='" + user_id + '\'' +
//                '}';
//    }
    @Override
    public String toString() {
        return name; // để Spinner hiển thị tên ngân sách
    }
}
