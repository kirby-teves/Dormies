package com.example.dormies.Dormies;

import java.util.ArrayList;
import java.util.List;

public class Tenant extends Person implements Payable {
    private transient Room assignedRoom;
    private transient List<Payment> payments;
    private transient List<MaintenanceRequest> maintenanceRequests;

    public Tenant(String name, String id) {
        super(name, id);
        this.payments = new ArrayList<>();
        this.maintenanceRequests = new ArrayList<>();
    }

    public Room getAssignedRoom() {
        return assignedRoom;
    }

    public void setAssignedRoom(Room assignedRoom) {
        this.assignedRoom = assignedRoom;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public List<MaintenanceRequest> getMaintenanceRequests() {
        return maintenanceRequests;
    }

    public MaintenanceRequest submitMaintenance(String requestId, String description) {
        MaintenanceRequest request = new MaintenanceRequest(requestId, description);
        maintenanceRequests.add(request);
        return request;
    }

    public void viewRoom() {
        if (assignedRoom != null) {
            System.out.println("Assigned Room: " + assignedRoom.getRoomNumber() + " | Status: " + assignedRoom.getStatus());
        } else {
            System.out.println("No room currently assigned.");
        }
    }

    @Override
    public void pay(double amount) {
        Payment newPayment = new Payment(amount, "2026-07-15");
        payments.add(newPayment);
    }

    @Override
    public String getDetails() {
        String roomStr = (assignedRoom != null) ? assignedRoom.getRoomNumber() : "No Room";
        return "Tenant ID: " + getId() + ", Name: " + getName() + ", Room: " + roomStr;
    }
}
