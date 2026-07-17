package com.example.dormies.Dormies;

public class MaintenanceRequest {
    private String requestId;
    private String status; // e.g., "Pending", "In Progress", "Resolved"
    private String description;

    public MaintenanceRequest(String requestId, String description) {
        this.requestId = requestId;
        this.status = "Pending"; // default initial status
        this.description = description;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }
}
