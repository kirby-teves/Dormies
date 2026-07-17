package com.example.dormies.Register;

import com.example.dormies.App;
import com.example.dormies.Dormies.MySQLConnection;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterController {

    public TextField nameInput;
    public TextField idInput;
    public PasswordField passwordInput;

    public void handleRegister() {
        String name = nameInput.getText().trim();
        String id = idInput.getText().trim();
        String password = passwordInput.getText().trim();

        if (name.isEmpty() || id.isEmpty() || password.isEmpty()) {
            showPopup("Error", "Please fill in all fields.");
            return;
        }

        if (isIdExists(id)) {
            showPopup("Error", "A tenant with this ID/Username already exists.");
            return;
        }

        String sql = "INSERT INTO tenant (ID, Name, Password_hash, Assigned_room) VALUES (?, ?, ?, NULL)";
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                pstmt.setString(2, name);
                pstmt.setString(3, hashPassword(password));
                pstmt.executeUpdate();
                showPopup("Success", "Registration successful!");
            }
        } catch (SQLException e) {
            showPopup("Error", "Registration failed: " + e.getMessage());
        }
    }

    private boolean isIdExists(String id) {
        String sql = "SELECT 1 FROM tenant WHERE ID = ?";
        try (Connection conn = MySQLConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void handleBackToLogin(javafx.event.ActionEvent event) throws java.io.IOException {
        javafx.stage.Stage stage = (javafx.stage.Stage) nameInput.getScene().getWindow();
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(App.class.getResource("login-view.fxml"));
        javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
        stage.setScene(scene);
    }

    private void showPopup(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}