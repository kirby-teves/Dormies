package com.example.dormies.Dormies;

public class Payment {
    private double amount;
    private String date;

    public Payment(double amount, String date) {
        this.amount = amount;
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String generateReceipt() {
        return "Receipt: Amount: $" + amount + " | Date: " + date;
    }
}
