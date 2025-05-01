package com.example.n03_quanlychitieu.model;

public class Expenses {
    private String expense_id;
    private double amount;
    private String description;
    private String create_at;
    private String user_id;
    private String category_id;
    private String budget_id;
    public Expenses() {
    }
    public Expenses(String expense_id, double amount, String description, String create_at, String user_id, String category_id, String budget_id) {
        this.expense_id = expense_id;
        this.amount = amount;
        this.description = description;
        this.create_at = create_at;
        this.user_id = user_id;
        this.category_id = category_id;
        this.budget_id = budget_id;
    }

    public String getExpense_id() {
        return expense_id;
    }

    public void setExpense_id(String expense_id) {
        this.expense_id = expense_id;
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

    public String getBudget_id() {
        return budget_id;
    }

    public void setBudget_id(String budget_id) {
        this.budget_id = budget_id;
    }

    @Override
    public String toString() {
        return "Expenses{" +
                "expense_id='" + expense_id + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", create_at='" + create_at + '\'' +
                ", user_id='" + user_id + '\'' +
                ", category_id='" + category_id + '\'' +
                ", budget_id='" + budget_id + '\'' +
                '}';
    }
}
