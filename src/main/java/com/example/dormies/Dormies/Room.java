package com.example.dormies.Dormies;

public class Room {
    private String roomNumber;
    private String status;

    public Room(String roomNumber, String status) {
        this.roomNumber = roomNumber;
        this.status = status;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getStatus() {
        return status;
    }

    public boolean isAvailable() {
        return "Available".equalsIgnoreCase(status);
    }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber;
    }
}

