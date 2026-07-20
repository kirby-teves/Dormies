module com.example.dormies {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.java;


    opens com.example.dormies to javafx.fxml;
    exports com.example.dormies;
    opens com.example.dormies.Dormies to javafx.fxml;
    exports com.example.dormies.Dormies;
    exports com.example.dormies.Register;
    opens com.example.dormies.Register to javafx.fxml;
    exports com.example.dormies.Login;
    opens com.example.dormies.Login to javafx.fxml;
    exports com.example.dormies.Repositories;
    opens com.example.dormies.Repositories to javafx.fxml;
}