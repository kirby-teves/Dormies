package com.example.dormies.Login;

import com.example.dormies.App;
import com.example.dormies.Dormies.*;
import com.example.dormies.Repositories.TenantRepository;
import com.example.dormies.Dormies.Person;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

public class LoginController {
    public static Person user_login;
    public TextField tfUsername;
    public PasswordField pfPassword;
    public Label lblError;
    static boolean isDark = false;

    private final TenantRepository tenantRepository = new TenantRepository();

    public void onDarkModeClicked() {
        Scene scene = lblError.getScene();
        String darkStylePath = Objects.requireNonNull(App.class.getResource("dark.css")).toExternalForm();
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
            Admin adminUser = new Admin("admin", "67");
            SessionManager.saveSession(adminUser);
            user_login = adminUser;
            Stage stage = (Stage) lblError.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("admin-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            return;
        }

        if (username.equals("staff") && password.equals("420")) {
            lblError.setText("Successfully logged in");
            success = true;
            Staff staffUser = new Staff("staff", "420");
            SessionManager.saveSession(staffUser);
            user_login = staffUser;
            Stage stage = (Stage) lblError.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("staff-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            return;
        }

        String hashedPassword = hashPassword(password);
        Tenant matched = tenantRepository.authenticate(username, hashedPassword);

        if (matched != null) {
            lblError.setText("Successfully logged in");
            SessionManager.saveSession(matched);
            user_login = matched;
            success = true;
            Stage stage = (Stage) lblError.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("tenant-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
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