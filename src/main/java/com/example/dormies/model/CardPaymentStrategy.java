package com.example.dormies.model;

public class CardPaymentStrategy implements PaymentStrategy {
    @Override
    public String processPayment(double amount) {
        return "Processed payment of $" + amount + " securely via Credit/Debit Card.";
    }
}