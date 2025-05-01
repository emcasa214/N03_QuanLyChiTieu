package com.example.n03_quanlychitieu.model;

public class Categories {
    private String category_id;
    private String name;
    private String icon;
    private String color;
    private String type;

    public Categories() {
    }
    public Categories(String category_id, String name, String icon, String color, String type) {
        this.category_id = category_id;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.type = type;
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

    @Override
    public String toString() {
        return "Categories{" +
                "category_id='" + category_id + '\'' +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", color='" + color + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
