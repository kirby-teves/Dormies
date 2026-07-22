package com.example.dormies.Dormies;

import com.example.dormies.App;
import com.example.dormies.Login.LoginController;
import com.example.dormies.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.util.UUID;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class DormiesController {

    private final ObservableList<Room> rooms = FXCollections.observableArrayList();
    private final ObservableList<Tenant> tenants = FXCollections.observableArrayList();
    private final ObservableList<MaintenanceRequest> requests = FXCollections.observableArrayList();
    private final ObservableList<Payment> payments = FXCollections.observableArrayList();

    private final Admin admin = new Admin("admin", "67");

    private final DormitoryFacade facade = new DormitoryFacade();

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
        if (LoginController.user_login instanceof Tenant) {
            loggedInTenant = (Tenant) LoginController.user_login;
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

    private <T> T findItemInList(ObservableList<T> list, Predicate<T> condition) {
        for (T item : list) {
            if (condition.test(item)) {
                return item;
            }
        }
        return null;
    }

    private void refreshAllFromDB() {
        new Thread(() -> {
            java.util.List<Room> tempRooms = facade.getAllRooms();
            java.util.List<Tenant> tempTenants = facade.getAllTenantsWithRooms();
            java.util.List<MaintenanceRequest> tempRequests = facade.getAllRequests();

            Platform.runLater(() -> {
                rooms.setAll(tempRooms);
                tenants.setAll(tempTenants);
                requests.setAll(tempRequests);
                if (requestList != null) {
                    refreshRequestList();
                }

                if (loggedInTenant != null) {
                    loggedInTenant = findItemInList(tenants, t -> t.getId().equals(loggedInTenant.getId()));
                    if (loggedInTenant != null) {
                        refreshTenantDetails(loggedInTenant);
                    } else {
                        handleLogout();
                    }
                }
            });
        }).start();
    }

    public void handleLogout() {
        try {javafx.stage.Stage stage = null;
            if (detailsText != null && detailsText.getScene() != null) {
                stage = (javafx.stage.Stage) detailsText.getScene().getWindow();
            } else if (reportText != null && reportText.getScene() != null) {
                stage = (javafx.stage.Stage) reportText.getScene().getWindow();
            } else if (requestList != null && requestList.getScene() != null) {
                stage = (javafx.stage.Stage) requestList.getScene().getWindow();
            }

            if (stage != null) {
                LoginController.user_login = null;
                SessionManager.clearSession();

                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(App.class.getResource("login-view.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                stage.setScene(scene);
            }
        } catch (java.io.IOException e) {
            System.out.println("Logout navigation failed: " + e.getMessage());
        }
    }

    public void handleAddRoom() {
        String roomNum = numInput.getText().trim();
        if (!roomNum.isEmpty()) {
            new Thread(() -> {
                try {
                    facade.addRoom(roomNum, statusCombo.getValue());
                    Platform.runLater(() -> {
                        numInput.clear();
                        showPopup("Success", "Room " + roomNum + " added.");
                        refreshAllFromDB();
                    });
                } catch (RuntimeException e) {
                    Platform.runLater(() -> showPopup("Error", "Failed to add room: " + e.getMessage()));
                }
            }).start();
        } else {
            showPopup("Error", "Enter a room number first.");
        }
    }

    public void handleDeleteRoom() {
        String roomNum = numInput.getText().trim();
        if (!roomNum.isEmpty()) {
            new Thread(() -> {
                try {
                    facade.deleteRoom(roomNum);
                    Platform.runLater(() -> {
                        numInput.clear();
                        showPopup("Success", "Room " + roomNum + " deleted.");
                        refreshAllFromDB();
                    });
                } catch (RuntimeException e) {
                    Platform.runLater(() -> showPopup("Error", "Failed to delete room: " + e.getMessage()));
                }
            }).start();
        } else {
            showPopup("Error", "Enter a room number to delete.");
        }
    }

    public void handleRemoveTenant() {
        Tenant t = tenantCombo.getValue();
        if (t != null) {
            new Thread(() -> {
                try {
                    facade.removeTenant(t);
                    Platform.runLater(() -> {
                        if (loggedInTenant != null && loggedInTenant.equals(t)) {
                            loggedInTenant = null;
                        }
                        showPopup("Success", "Tenant " + t.getName() + " removed.");
                        refreshAllFromDB();
                    });
                } catch (RuntimeException e) {
                    Platform.runLater(() -> showPopup("Error", "Failed to remove tenant: " + e.getMessage()));
                }
            }).start();
        } else {
            showPopup("Error", "Select a tenant first.");
        }
    }

    public void handleGenerateReport() {
        admin.viewReports();
        new Thread(() -> {
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
            Platform.runLater(() -> reportText.setText(reportData));
        }).start();
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
            new Thread(() -> {
                try {
                    facade.assignRoom(t, r);
                    Platform.runLater(() -> {
                        roomCombo.getSelectionModel().clearSelection();
                        staffTenantCombo.getSelectionModel().clearSelection();
                        showPopup("Success", "Room " + r.getRoomNumber() + " assigned to " + t.getName());
                        refreshAllFromDB();
                    });
                } catch (RuntimeException e) {
                    Platform.runLater(() -> showPopup("Error", "Assignment failed: " + e.getMessage()));
                }
            }).start();
        } else {
            showPopup("Error", "Select both a room and a tenant.");
        }
    }

    public void handleResolveRequest() {
        int selectedIndex = requestList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            MaintenanceRequest req = requests.get(selectedIndex);
            new Thread(() -> {
                try {
                    facade.resolveRequest(req.getRequestId());
                    Platform.runLater(() -> {
                        showPopup("Success", "Request resolved.");
                        refreshAllFromDB();
                    });
                } catch (RuntimeException e) {
                    Platform.runLater(() -> showPopup("Error", "Failed to resolve request: " + e.getMessage()));
                }
            }).start();
        } else {
            showPopup("Error", "Select a request from the list first.");
        }
    }

    public void handleDeleteRequest() {
        int selectedIndex = requestList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            MaintenanceRequest req = requests.get(selectedIndex);
            new Thread(() -> {
                try {
                    facade.deleteRequest(req.getRequestId());
                    Platform.runLater(() -> {
                        showPopup("Success", "Request deleted.");
                        refreshAllFromDB();
                    });
                } catch (RuntimeException e) {
                    Platform.runLater(() -> showPopup("Error", "Failed to delete request: " + e.getMessage()));
                }
            }).start();
        } else {
            showPopup("Error", "Select a request from the list first.");
        }
    }

    public void handleSubmitIssue() {
        Tenant t = loggedInTenant;
        String desc = descInput.getText().trim();
        if (t != null && !desc.isEmpty()) {
            String reqId = "REQ-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            new Thread(() -> {
                try {
                    facade.submitRequest(reqId, t.getId(), desc);
                    Platform.runLater(() -> {
                        descInput.clear();
                        showPopup("Success", "Request submitted. ID: " + reqId);
                        refreshAllFromDB();
                    });
                } catch (RuntimeException e) {
                    Platform.runLater(() -> showPopup("Error", "Submission failed: " + e.getMessage()));
                }
            }).start();
        } else {
            showPopup("Error", "Describe the issue first.");
        }
    }

    public void handlePayment() {
        Tenant t = loggedInTenant;
        try {
            double amt = Double.parseDouble(amtInput.getText().trim());
            if (t != null && amt > 0) {
                PaymentStrategy strategy = new CardPaymentStrategy();
                String receiptMessage = strategy.processPayment(amt);

                t.pay(amt);
                Payment lastPayment = t.getPayments().get(t.getPayments().size() - 1);
                payments.add(lastPayment);
                amtInput.clear();
                refreshTenantDetails(t);
                showPopup("Success", receiptMessage);
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