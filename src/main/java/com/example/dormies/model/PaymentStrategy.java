package com.example.dormies.model;

public interface PaymentStrategy {
    String processPayment(double amount);
}