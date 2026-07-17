package com.example.dormies.Dormies;

public class Staff extends Person {

    public Staff(String name, String id) {
        super(name, id);
    }

    public void updateRequest(String requestId) {
        System.out.println("Staff " + getName() + " is processing request ID: " + requestId);
    }

    public void assignRoom(Room room, Tenant tenant) {
        if (room.isAvailable()) {
            tenant.setAssignedRoom(room);
            room.updateStatus("Occupied");
            System.out.println("Assigned room " + room.getRoomNumber() + " to " + tenant.getName());
        } else {
            System.out.println("Room " + room.getRoomNumber() + " is not available.");
        }
    }

    @Override
    public String getDetails() {
        return "Staff ID: " + getId() + ", Name: " + getName();
    }
}
