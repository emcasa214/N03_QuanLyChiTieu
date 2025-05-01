package com.example.n03_quanlychitieu.model;

public class Incomes {
    private String income_id;
    private double amount;
    private String description;
    private String create_at;
    private String user_id;
    private String category_id;
    public Incomes() {
    }

    public Incomes(String income_id, double amount, String description, String create_at, String user_id, String category_id) {
        this.income_id = income_id;
        this.amount = amount;
        this.description = description;
        this.create_at = create_at;
        this.user_id = user_id;
        this.category_id = category_id;
    }

    public String getIncome_id() {
        return income_id;
    }

    public void setIncome_id(String income_id) {
        this.income_id = income_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreate_at() {
        return create_at;
    }

    public void setCreate_at(String create_at) {
        this.create_at = create_at;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    @Override
    public String toString() {
        return "Incomes{" +
                "income_id='" + income_id + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", create_at='" + create_at + '\'' +
                ", user_id='" + user_id + '\'' +
                ", category_id='" + category_id + '\'' +
                '}';
    }
}
