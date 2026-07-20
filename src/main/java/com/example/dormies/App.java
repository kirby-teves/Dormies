package com.example.dormies;

import com.example.dormies.Dormies.*;
import com.example.dormies.Dormies.Person;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Person sessionUser = SessionManager.loadSession();
        if (sessionUser != null) {
            com.example.dormies.Login.LoginController.user_login = sessionUser;
            String fxmlFile;
            if (sessionUser instanceof Admin) {
                fxmlFile = "admin-view.fxml";
            } else if (sessionUser instanceof Staff) {
                fxmlFile = "staff-view.fxml";
            } else {
                fxmlFile = "tenant-view.fxml";
            }
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxmlFile));
            Scene scene = new Scene(fxmlLoader.load(), 750, 550);
            stage.setTitle("Dormies!");
            stage.setScene(scene);
            stage.show();
        } else {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 450);
            stage.setTitle("Dormies Login!");
            stage.setScene(scene);
            stage.show();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}