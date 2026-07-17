package com.example.dormies.Dormies;

import com.example.dormies.App;
import com.example.dormies.Login.LoginController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.sql.*;
import java.util.UUID;

public class DormiesController {

    private final ObservableList<Room> rooms = FXCollections.observableArrayList();
    private final ObservableList<Tenant> tenants = FXCollections.observableArrayList();
    private final ObservableList<MaintenanceRequest> requests = FXCollections.observableArrayList();
    private final ObservableList<Payment> payments = FXCollections.observableArrayList();

    private final Admin admin = new Admin("admin", "67");
    private final Staff staff = new Staff("staff", "420");

    private Tenant loggedInTenant = null;

    public TextField numInput;
    public ComboBox<String> statusCombo;
    public ComboBox<Tenant> tenantCombo;
    public TextArea reportText;

    public ComboBox<Room> roomCombo;
    public ComboBox<Tenant> staffTenantCombo;
    public ListView<String> requestList;

    public TextArea detailsText;
    public TextField descInput;
    public TextField amtInput;

    public void initialize() {
        if (LoginController.user_login != null) {
            loggedInTenant = LoginController.user_login;
        }

        refreshAllFromDB();

        if (statusCombo != null) {
            statusCombo.setItems(FXCollections.observableArrayList("Available", "Under Maintenance"));
            statusCombo.getSelectionModel().selectFirst();
        }
        if (tenantCombo != null) {
            tenantCombo.setItems(tenants);
        }
    }

    private void refreshAllFromDB() {
        loadRoomsFromDB();
        loadTenantsFromDB();
        loadRequestsFromDB();

        if (loggedInTenant != null) {
            Tenant freshTenant = null;
            for (Tenant t : tenants) {
                if (t.getId().equals(loggedInTenant.getId())) {
                    freshTenant = t;
                    break;
                }
            }
            loggedInTenant = freshTenant;
            if (loggedInTenant != null) {
                refreshTenantDetails(loggedInTenant);
            } else {
                handleLogout();
            }
        }
    }

    private void loadRoomsFromDB() {
        rooms.clear();
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn == null) return;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM rooms")) {
                while (rs.next()) {
                    rooms.add(new Room(rs.getString("room_number"), rs.getString("status")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTenantsFromDB() {
        tenants.clear();
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn == null) return;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM tenants")) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRequestsFromDB() {
        requests.clear();
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn == null) return;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM maintenance_requests")) {
                while (rs.next()) {
                    MaintenanceRequest req = new MaintenanceRequest(rs.getString("request_id"), rs.getString("description"));
                    req.updateStatus(rs.getString("status"));
                    requests.add(req);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleLogout() {
        try {
            javafx.stage.Stage stage = null;
            if (detailsText != null && detailsText.getScene() != null) {
                stage = (javafx.stage.Stage) detailsText.getScene().getWindow();
            } else if (reportText != null && reportText.getScene() != null) {
                stage = (javafx.stage.Stage) reportText.getScene().getWindow();
            } else if (requestList != null && requestList.getScene() != null) {
                stage = (javafx.stage.Stage) requestList.getScene().getWindow();
            }

            if (stage != null) {
                LoginController.user_login = null;
                java.io.File file = new java.io.File("tenant.ser");
                if (file.exists()) {
                    file.delete();
                }

                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(App.class.getResource("login-view.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                stage.setScene(scene);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void handleAddRoom() {
        String roomNum = numInput.getText().trim();
        if (!roomNum.isEmpty()) {
            String sql = "INSERT INTO rooms (room_number, status) VALUES (?, ?)";
            try (Connection conn = MySQLConnection.getConnection()) {
                if (conn == null) return;
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, roomNum);
                    pstmt.setString(2, statusCombo.getValue());
                    pstmt.executeUpdate();
                    numInput.clear();
                    showPopup("Success", "Room " + roomNum + " added.");
                }
            } catch (SQLException e) {
                showPopup("Error", "Failed to add room: " + e.getMessage());
                return;
            }
            refreshAllFromDB();
        } else {
            showPopup("Error", "Enter a room number first.");
        }
    }

    public void handleDeleteRoom() {
        String roomNum = numInput.getText().trim();
        if (!roomNum.isEmpty()) {
            try (Connection conn = MySQLConnection.getConnection()) {
                if (conn == null) return;
                try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM rooms WHERE room_number = ?")) {
                    pstmt.setString(1, roomNum);
                    int deleted = pstmt.executeUpdate();
                    if (deleted > 0) {
                        numInput.clear();
                        showPopup("Success", "Room " + roomNum + " deleted.");
                    } else {
                        showPopup("Error", "Room not found.");
                    }
                }
            } catch (SQLException e) {
                showPopup("Error", "Failed to delete room: " + e.getMessage());
                return;
            }
            refreshAllFromDB();
        } else {
            showPopup("Error", "Enter a room number to delete.");
        }
    }

    public void handleRemoveTenant() {
        Tenant t = tenantCombo.getValue();
        if (t != null) {
            try (Connection conn = MySQLConnection.getConnection()) {
                if (conn == null) return;
                if (t.getAssignedRoom() != null) {
                    try (PreparedStatement pstmtRoom = conn.prepareStatement("UPDATE rooms SET status = 'Available' WHERE room_number = ?")) {
                        pstmtRoom.setString(1, t.getAssignedRoom().getRoomNumber());
                        pstmtRoom.executeUpdate();
                    }
                }
                try (PreparedStatement pstmtTenant = conn.prepareStatement("DELETE FROM tenants WHERE id = ?")) {
                    pstmtTenant.setString(1, t.getId());
                    pstmtTenant.executeUpdate();
                }
            } catch (SQLException e) {
                showPopup("Error", "Failed to remove tenant: " + e.getMessage());
                return;
            }
            if (loggedInTenant != null && loggedInTenant.equals(t)) {
                loggedInTenant = null;
            }
            refreshAllFromDB();
            showPopup("Success", "Tenant " + t.getName() + " removed.");
        } else {
            showPopup("Error", "Select a tenant first.");
        }
    }

    public void handleGenerateReport() {
        admin.viewReports();
        long totalRooms = rooms.size();
        long freeRooms = rooms.stream().filter(Room::isAvailable).count();
        long totalTenants = tenants.size();
        double totalRevenue = payments.stream().mapToDouble(Payment::getAmount).sum();
        long openIssues = requests.stream().filter(r -> "Pending".equals(r.getStatus())).count();

        String reportData = String.format(
                """
                        --- DORM SYSTEM STATUS ---
                        Total Rooms: %d
                        Available Rooms: %d
                        Total Tenants: %d
                        Total Revenue: $%.2f
                        Active Maintenance Requests: %d
                        """,
                totalRooms, freeRooms, totalTenants, totalRevenue, openIssues
        );
        reportText.setText(reportData);
    }

    public void handleRoomComboShowing() {
        roomCombo.setItems(FXCollections.observableArrayList(
                rooms.stream().filter(Room::isAvailable).toList()
        ));
    }

    public void handleTenantComboShowing() {
        staffTenantCombo.setItems(FXCollections.observableArrayList(
                tenants.stream().filter(t -> t.getAssignedRoom() == null).toList()
        ));
    }

    public void handleAssignRoom() {
        Room r = roomCombo.getValue();
        Tenant t = staffTenantCombo.getValue();
        if (r != null && t != null) {
            try (Connection conn = MySQLConnection.getConnection()) {
                if (conn == null) return;
                try (PreparedStatement pstmtTenant = conn.prepareStatement("UPDATE tenants SET assigned_room = ? WHERE id = ?")) {
                    pstmtTenant.setString(1, r.getRoomNumber());
                    pstmtTenant.setString(2, t.getId());
                    pstmtTenant.executeUpdate();
                }
                try (PreparedStatement pstmtRoom = conn.prepareStatement("UPDATE rooms SET status = 'Occupied' WHERE room_number = ?")) {
                    pstmtRoom.setString(1, r.getRoomNumber());
                    pstmtRoom.executeUpdate();
                }
                roomCombo.getSelectionModel().clearSelection();
                staffTenantCombo.getSelectionModel().clearSelection();
                showPopup("Success", "Room " + r.getRoomNumber() + " assigned to " + t.getName());
            } catch (SQLException e) {
                showPopup("Error", "Assignment failed: " + e.getMessage());
                return;
            }
            refreshAllFromDB();
        } else {
            showPopup("Error", "Select both a room and a tenant.");
        }
    }

    public void handleResolveRequest() {
        int selectedIndex = requestList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            MaintenanceRequest req = requests.get(selectedIndex);
            try (Connection conn = MySQLConnection.getConnection()) {
                if (conn == null) return;
                try (PreparedStatement pstmt = conn.prepareStatement("UPDATE maintenance_requests SET status = 'Resolved' WHERE request_id = ?")) {
                    pstmt.setString(1, req.getRequestId());
                    pstmt.executeUpdate();
                    showPopup("Success", "Request resolved.");
                }
            } catch (SQLException e) {
                showPopup("Error", "Failed to resolve request: " + e.getMessage());
                return;
            }
            refreshAllFromDB();
        } else {
            showPopup("Error", "Select a request from the list first.");
        }
    }

    public void handleDeleteRequest() {
        int selectedIndex = requestList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            MaintenanceRequest req = requests.get(selectedIndex);
            try (Connection conn = MySQLConnection.getConnection()) {
                if (conn == null) return;
                try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM maintenance_requests WHERE request_id = ?")) {
                    pstmt.setString(1, req.getRequestId());
                    pstmt.executeUpdate();
                    showPopup("Success", "Request deleted.");
                }
            } catch (SQLException e) {
                showPopup("Error", "Failed to delete request: " + e.getMessage());
                return;
            }
            refreshAllFromDB();
        } else {
            showPopup("Error", "Select a request from the list first.");
        }
    }

    public void handleSubmitIssue() {
        Tenant t = loggedInTenant;
        String desc = descInput.getText().trim();
        if (t != null && !desc.isEmpty()) {
            String reqId = "REQ-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            String sql = "INSERT INTO maintenance_requests (request_id, tenant_id, description, status) VALUES (?, ?, ?, 'Pending')";
            try (Connection conn = MySQLConnection.getConnection()) {
                if (conn == null) return;
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, reqId);
                    pstmt.setString(2, t.getId());
                    pstmt.setString(3, desc);
                    pstmt.executeUpdate();
                    descInput.clear();
                    showPopup("Success", "Request submitted. ID: " + reqId);
                }
            } catch (SQLException e) {
                showPopup("Error", "Submission failed: " + e.getMessage());
                return;
            }
            refreshAllFromDB();
        } else {
            showPopup("Error", "Describe the issue first.");
        }
    }

    public void handlePayment() {
        Tenant t = loggedInTenant;
        try {
            double amt = Double.parseDouble(amtInput.getText().trim());
            if (t != null && amt > 0) {
                t.pay(amt);
                Payment lastPayment = t.getPayments().get(t.getPayments().size() - 1);
                payments.add(lastPayment);
                amtInput.clear();
                showPopup("Success", lastPayment.generateReceipt());
            } else {
                showPopup("Error", "Amount must be greater than zero.");
            }
        } catch (NumberFormatException ex) {
            showPopup("Error", "Enter a valid number.");
        }
    }

    private void refreshTenantDetails(Tenant t) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- TENANT PROFILE ---\n");
        sb.append(t.getDetails()).append("\n");

        Room r = t.getAssignedRoom();
        if (r != null) {
            sb.append(String.format("Assigned Room: %s (Status: %s)\n", r.getRoomNumber(), r.getStatus()));
        } else {
            sb.append("Assigned Room: None\n");
        }

        sb.append("Payments Completed: ").append(t.getPayments().size()).append("\n");
        sb.append("Pending Requests: ").append(
                t.getMaintenanceRequests().stream().filter(req -> "Pending".equals(req.getStatus())).count()
        ).append("\n");

        detailsText.setText(sb.toString());
    }

    private void refreshRequestList() {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (MaintenanceRequest r : requests) {
            items.add(String.format("[%s] Status: %s - %s", r.getRequestId(), r.getStatus(), r.getDescription()));
        }
        requestList.setItems(items);
    }

    private void showPopup(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}