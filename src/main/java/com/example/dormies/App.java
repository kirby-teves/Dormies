package com.example.dormies;

import com.example.dormies.Dormies.Tenant;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static com.example.dormies.Login.LoginController.user_login;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("tenant.ser")
        )) {
            user_login = (Tenant) ois.readObject();
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("tenant-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 750, 550);
            stage.setTitle("Dormies!");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 450);
            stage.setTitle("Dormies - Login!");
            stage.setScene(scene);
            stage.show();
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found or is changed to a newer version");
        }
    }

}