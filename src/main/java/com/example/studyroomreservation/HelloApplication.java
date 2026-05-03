package com.example.studyroomreservation;

import com.example.studyroomreservation.service.Storage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load all data first
        Storage.loadAllData();

        // Load FXML
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/studyroomreservation/LoginView.fxml")
        );

        Scene scene = new Scene(loader.load());

        // Load CSS - This is critical for consistent styling
        String css = getClass().getResource("/com/example/studyroomreservation/styles.css").toExternalForm();
        if (css != null) {
            scene.getStylesheets().add(css);
            System.out.println("CSS loaded successfully");
        }

        // Configure stage
        stage.setTitle("Study Room Reservation System");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.centerOnScreen();
        stage.show();

        // Save on close
        stage.setOnCloseRequest(e -> {
            Storage.saveAllData();
            System.out.println("Data saved on exit");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}