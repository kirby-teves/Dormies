package com.example.dormies.Repositories;

import com.example.dormies.MySQLConnection;
import com.example.dormies.model.MaintenanceRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceRequestRepository implements Repository<MaintenanceRequest> {
    @Override
    public List<MaintenanceRequest> getAll() {
        List<MaintenanceRequest> requests = new ArrayList<>();
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM maintenance_request")) {
                    while (rs.next()) {
                        MaintenanceRequest req = new MaintenanceRequest(rs.getString("request_id"), rs.getString("description"));
                        req.updateStatus(rs.getString("status"));
                        requests.add(req);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Request load failed: " + e.getMessage());
        }
        return requests;
    }

    @Override
    public void add(MaintenanceRequest req) {}

    public void addRequest(String requestId, String tenantId, String description) {
        String sql = "INSERT INTO maintenance_request (request_id, tenant_id, description, status) VALUES (?, ?, ?, 'Pending')";
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, requestId);
                    pstmt.setString(2, tenantId);
                    pstmt.setString(3, description);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Add request failed: " + e.getMessage());
        }
    }

    @Override
    public void delete(String requestId) {
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM maintenance_request WHERE request_id = ?")) {
                    pstmt.setString(1, requestId);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Delete request failed: " + e.getMessage());
        }
    }

    public void updateStatus(String requestId, String status) {
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement("UPDATE maintenance_request SET status = ? WHERE request_id = ?")) {
                    pstmt.setString(1, status);
                    pstmt.setString(2, requestId);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Update request status failed: " + e.getMessage());
        }
    }
}