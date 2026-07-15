module com.example.dormies {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.dormies to javafx.fxml;
    exports com.example.dormies;
}