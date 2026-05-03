package com.example.studyroomreservation.controller;

import com.example.studyroomreservation.model.*;
import com.example.studyroomreservation.service.AuthService;
import com.example.studyroomreservation.service.Storage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField nameField;
    @FXML private TextField idField;
    @FXML private Label messageLabel;

    private AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        Storage.loadAllData();
        nameField.textProperty().addListener((obs, old, val) -> clearMessage());
        idField.textProperty().addListener((obs, old, val) -> clearMessage());
    }

    private void clearMessage() {
        messageLabel.setText("");
    }

    @FXML
    public void login() {
        String name = nameField.getText().trim();
        String id = idField.getText().trim();

        if (name.isEmpty() || id.isEmpty()) {
            showMessage("Please enter both name and ID", "red");
            return;
        }

        Person user = authService.login(id, name);

        if (user == null) {
            showMessage("Invalid credentials. Please register first or check your ID/Name", "red");
            return;
        }

        if (user instanceof Admin) {
            loadAdminView((Admin) user);
        } else if (user instanceof Student) {
            // Clear any previous bookings and reload fresh
            Student student = (Student) user;
            student.getMyBookings().clear();
            loadStudentView(student);
        }
    }

    @FXML
    public void register() {
        String name = nameField.getText().trim();
        String id = idField.getText().trim();

        if (name.isEmpty() || id.isEmpty()) {
            showMessage("Please enter both name and ID", "red");
            return;
        }

        if (id.equals("A001")) {
            showMessage("Cannot register with Admin ID", "red");
            return;
        }

        try {
            Student student = authService.registerStudent(name, id);
            showMessage("Registration successful! You can now login.", "green");
            nameField.clear();
            idField.clear();
        } catch (RuntimeException e) {
            showMessage(e.getMessage(), "red");
        }
    }

    private void loadAdminView(Admin admin) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/studyroomreservation/AdminView.fxml")
            );
            Stage stage = (Stage) nameField.getScene().getWindow();
            Scene scene = new Scene(loader.load());

            String css = getClass().getResource("/com/example/studyroomreservation/styles.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            AdminController controller = loader.getController();
            controller.setCurrentAdmin(admin);

            stage.setScene(scene);
            stage.setTitle("Admin Dashboard - Study Room System");
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error loading admin view: " + e.getMessage(), "red");
        }
    }

    private void loadStudentView(Student student) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/studyroomreservation/StudentView.fxml")
            );
            Stage stage = (Stage) nameField.getScene().getWindow();
            Scene scene = new Scene(loader.load());

            String css = getClass().getResource("/com/example/studyroomreservation/styles.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            StudentController controller = loader.getController();
            controller.setCurrentStudent(student);

            stage.setScene(scene);
            stage.setTitle("Student Dashboard - " + student.getName());
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error loading student view: " + e.getMessage(), "red");
        }
    }

    private void showMessage(String msg, String color) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: " + color + ";");
    }
}