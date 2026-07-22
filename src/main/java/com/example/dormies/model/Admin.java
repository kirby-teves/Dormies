package com.example.dormies.model;

public class Admin extends Person {

    public Admin(String name, String id) {
        super(name, id);
    }

    public void addRoom(Room room) {
        System.out.println("Admin " + getName() + " added room " + room.getRoomNumber());
    }

    public void removeTenant(String tenantId) {
        System.out.println("Admin " + getName() + " removed tenant with ID: " + tenantId);
    }

    public void viewReports() {
        System.out.println("Displaying reports for admin " + getName());
    }

    @Override
    public String getDetails() {
        return "Admin ID: " + getId() + ", Name: " + getName();
    }
}
