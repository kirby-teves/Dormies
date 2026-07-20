package com.example.dormies.Repositories;

import com.example.dormies.Dormies.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomRepository implements Repository<Room> {
    @Override
    public List<Room> getAll() {
        List<Room> rooms = new ArrayList<>();
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM room")) {
                    while (rs.next()) {
                        rooms.add(new Room(rs.getString("room_number"), rs.getString("status")));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Room load failed: " + e.getMessage());
        }
        return rooms;
    }

    @Override
    public void add(Room room) {
        String sql = "INSERT INTO room (room_number, status) VALUES (?, ?)";
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, room.getRoomNumber());
                    pstmt.setString(2, room.getStatus());
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Add room failed: " + e.getMessage());
        }
    }

    @Override
    public void delete(String roomNumber) {
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM room WHERE room_number = ?")) {
                    pstmt.setString(1, roomNumber);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Delete room failed: " + e.getMessage());
        }
    }

    public void updateStatus(String roomNumber, String status) {
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement("UPDATE room SET status = ? WHERE room_number = ?")) {
                    pstmt.setString(1, status);
                    pstmt.setString(2, roomNumber);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Update room status failed: " + e.getMessage());
        }
    }
}