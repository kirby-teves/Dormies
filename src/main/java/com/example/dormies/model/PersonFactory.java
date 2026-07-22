package com.example.dormies.model;

public class PersonFactory {
    public static Person createPerson(String role, String name, String id) {
        if (role == null) return null;
        return switch (role.toUpperCase()) {
            case "ADMIN" -> new Admin(name, id);
            case "TENANT" -> new Tenant(name, id);
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }
}