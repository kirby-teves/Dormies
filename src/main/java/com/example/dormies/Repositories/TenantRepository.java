package com.example.dormies.Repositories;

import com.example.dormies.Dormies.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TenantRepository implements Repository<Tenant> {
    @Override
    public List<Tenant> getAll() {
        List<Tenant> tenants = new ArrayList<>();
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM tenant")) {
                    while (rs.next()) {
                        tenants.add(new Tenant(rs.getString("name"), rs.getString("id")));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Tenant load failed: " + e.getMessage());
        }
        return tenants;
    }

    @Override
    public void add(Tenant tenant) {}

    public List<Tenant> getAllWithRooms(List<Room> rooms) {
        List<Tenant> tenants = new ArrayList<>();
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM tenant")) {
                    while (rs.next()) {
                        Tenant t = new Tenant(rs.getString("name"), rs.getString("id"));
                        String assignedRoomNum = rs.getString("assigned_room");
                        if (assignedRoomNum != null) {
                            for (Room r : rooms) {
                                if (r.getRoomNumber().equals(assignedRoomNum)) {
                                    t.setAssignedRoom(r);
                                    break;
                                }
                            }
                        }
                        tenants.add(t);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Tenant load failed: " + e.getMessage());
        }
        return tenants;
    }

    public void registerTenant(String id, String name, String passwordHash) {
        String sql = "INSERT INTO tenant (id, name, password_hash, assigned_room) VALUES (?, ?, ?, NULL)";
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, id);
                    pstmt.setString(2, name);
                    pstmt.setString(3, passwordHash);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Register failed: " + e.getMessage());
        }
    }

    public boolean isIdExists(String id) {
        String sql = "SELECT 1 FROM tenant WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, id);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        return rs.next();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void delete(String id) {
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM tenant WHERE id = ?")) {
                    pstmt.setString(1, id);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Delete tenant failed: " + e.getMessage());
        }
    }

    public void assignRoom(String tenantId, String roomNumber) {
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement("UPDATE tenant SET assigned_room = ? WHERE id = ?")) {
                    pstmt.setString(1, roomNumber);
                    pstmt.setString(2, tenantId);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Assign room failed: " + e.getMessage());
        }
    }

    public Tenant authenticate(String username, String passwordHash) {
        String sql = "SELECT * FROM tenant WHERE id = ? AND password_hash = ?";
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, passwordHash);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            return new Tenant(rs.getString("name"), rs.getString("id"));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Auth failed: " + e.getMessage());
        }
        return null;
    }
}