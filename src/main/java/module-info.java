module com.example.studyroomreservation {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.studyroomreservation to javafx.fxml;
    opens com.example.studyroomreservation.controller to javafx.fxml;
    opens com.example.studyroomreservation.model to javafx.base;

    exports com.example.studyroomreservation;
    exports com.example.studyroomreservation.controller;
    exports com.example.studyroomreservation.model;
}