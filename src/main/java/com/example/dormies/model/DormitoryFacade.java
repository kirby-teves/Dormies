package com.example.dormies.model;

import com.example.dormies.Repositories.*;
import java.util.List;

public class DormitoryFacade {
    private final RoomRepository roomRepo = new RoomRepository();
    private final TenantRepository tenantRepo = new TenantRepository();
    private final MaintenanceRequestRepository requestRepo = new MaintenanceRequestRepository();

    public List<Room> getAllRooms() {
        return roomRepo.getAll();
    }

    public List<Tenant> getAllTenantsWithRooms() {
        return tenantRepo.getAllWithRooms(roomRepo.getAll());
    }

    public List<MaintenanceRequest> getAllRequests() {
        return requestRepo.getAll();
    }

    public void addRoom(String roomNumber, String status) {
        roomRepo.add(new Room(roomNumber, status));
    }

    public void deleteRoom(String roomNumber) {
        roomRepo.delete(roomNumber);
    }

    public void removeTenant(Tenant t) {
        if (t.getAssignedRoom() != null) {
            roomRepo.updateStatus(t.getAssignedRoom().getRoomNumber(), "Available");
        }
        tenantRepo.delete(t.getId());
    }

    public void assignRoom(Tenant t, Room r) {
        tenantRepo.assignRoom(t.getId(), r.getRoomNumber());
        roomRepo.updateStatus(r.getRoomNumber(), "Occupied");
    }

    public void resolveRequest(String requestId) {
        requestRepo.updateStatus(requestId, "Resolved");
    }

    public void deleteRequest(String requestId) {
        requestRepo.delete(requestId);
    }

    public void submitRequest(String requestId, String tenantId, String description) {
        requestRepo.addRequest(requestId, tenantId, description);
    }
}