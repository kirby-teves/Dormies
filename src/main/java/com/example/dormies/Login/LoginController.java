package com.example.dormies.Login;

import com.example.dormies.App;
import com.example.dormies.Dormies.MySQLConnection;
import com.example.dormies.Dormies.Tenant;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginController {
    public static Tenant user_login;
    public TextField tfUsername;
    public PasswordField pfPassword;
    public Label lblError;
    static boolean isDark = false;

    public void onDarkModeClicked() {
        Scene scene = lblError.getScene();
        java.net.URL cssResource = App.class.getResource("dark.css");
        if (cssResource == null) {
            System.out.println("Error: dark.css not found in the App package folder.");
            return;
        }
        String darkStylePath = cssResource.toExternalForm();
        if (!isDark) {
            scene.getStylesheets().add(darkStylePath);
        } else {
            scene.getStylesheets().remove(darkStylePath);
        }
        isDark = !isDark;
    }

    public void onSignInClicked() throws IOException {
        boolean success = false;
        String username = tfUsername.getText().trim();
        String password = pfPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Please fill in all fields");
            return;
        }

        if (username.equals("admin") && password.equals("67")) {
            lblError.setText("Successfully logged in");
            success = true;
            Stage stage = (Stage) lblError.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("admin-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            return;
        }

        if (username.equals("staff") && password.equals("420")) {
            lblError.setText("Successfully logged in");
            success = true;
            Stage stage = (Stage) lblError.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("staff-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            return;
        }

        String hashedPassword = hashPassword(password);

        try (Connection c = MySQLConnection.getConnection();
             Statement stmt = c.createStatement()) {
            if (c == null) return;
            String query = "SELECT * FROM tenant";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String passHash = rs.getString("password_hash");

                if (id.equalsIgnoreCase(username) && passHash.equals(hashedPassword)) {
                    lblError.setText("Successfully logged in");
                    Tenant tenant = new Tenant(name, id);
                    try (ObjectOutputStream oos = new ObjectOutputStream(
                            new FileOutputStream("tenant.ser")
                    )) {
                        oos.writeObject(tenant);
                    } catch (IOException e) {
                        System.out.println("File cannot be saved");
                    }
                    user_login = tenant;
                    success = true;
                    Stage stage = (Stage) lblError.getScene().getWindow();
                    FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("tenant-view.fxml"));
                    Scene scene = new Scene(fxmlLoader.load());
                    stage.setScene(scene);
                    break;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }

        if (!success) {
            lblError.setText("Invalid username or password");
        }
    }

    public void onPasswordKeyPressed(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            onSignInClicked();
        }
    }

    public void onRegisterClicked(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) lblError.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("register-view.fxml"));
            stage.setTitle("Dormies - register!");
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
}