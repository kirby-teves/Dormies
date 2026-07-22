package com.example.dormies.model;

public class CashPaymentStrategy implements PaymentStrategy {
    @Override
    public String processPayment(double amount) {
        return "Processed payment of $" + amount + " in Cash at the front desk.";
    }
}