package com.example.studyroomreservation.controller;

import com.example.studyroomreservation.model.*;
import com.example.studyroomreservation.service.Storage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AdminController {

    @FXML private TextField roomCodeField;
    @FXML private TextField capacityField;
    @FXML private TextField timeSlotField;
    @FXML private DatePicker datePicker;
    @FXML private ChoiceBox<StudyRoom> roomChoice;
    @FXML private ListView<String> bookingList;
    @FXML private ListView<String> roomList;
    @FXML private ListView<String> timeSlotList;
    @FXML private ListView<String> availableDatesList;
    @FXML private Label statusLabel;
    @FXML private Label totalRoomsLabel;
    @FXML private Label totalBookingsLabel;

    private Admin currentAdmin;

    @FXML
    public void initialize() {
        refreshAllData();

        // Add listener for room selection
        roomChoice.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    if (newVal != null) {
                        refreshTimeSlotList(newVal);
                        refreshAvailableDatesList(newVal);
                    }
                }
        );

        // Configure date picker format
        datePicker.setConverter(new StringConverter<LocalDate>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date) {
                return date != null ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, formatter) : null;
            }
        });

        // Don't allow past dates for availability
        datePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    public void setCurrentAdmin(Admin admin) {
        this.currentAdmin = admin;
        updateStatus("Welcome, " + admin.getName() + "!");
    }

    private void refreshAllData() {
        refreshRoomList();
        refreshRoomChoice();
        updateStatistics();
        viewBookings();
    }

    private void refreshRoomList() {
        Platform.runLater(() -> {
            roomList.getItems().clear();
            if (Storage.rooms.isEmpty()) {
                roomList.getItems().add("No rooms available");
                return;
            }

            for (StudyRoom r : Storage.rooms) {
                roomList.getItems().add(r.getRoomCode() + " | Capacity: " + r.getCapacity());
            }
        });
    }

    private void refreshRoomChoice() {
        Platform.runLater(() -> {
            roomChoice.getItems().clear();
            roomChoice.getItems().addAll(Storage.rooms);
            if (!Storage.rooms.isEmpty()) {
                roomChoice.getSelectionModel().selectFirst();
            }
        });
    }

    private void refreshTimeSlotList(StudyRoom room) {
        Platform.runLater(() -> {
            timeSlotList.getItems().clear();
            if (room.getTimeSlots().isEmpty()) {
                timeSlotList.getItems().add("No time slots added yet");
                return;
            }

            for (String slot : room.getTimeSlots()) {
                timeSlotList.getItems().add(slot);
            }
        });
    }

    private void refreshAvailableDatesList(StudyRoom room) {
        Platform.runLater(() -> {
            availableDatesList.getItems().clear();
            if (room.getAvailableDates().isEmpty()) {
                availableDatesList.getItems().add("No available dates set");
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd (EEEE)");
            for (LocalDate date : room.getAvailableDates()) {
                availableDatesList.getItems().add(date.format(formatter));
            }
        });
    }

    private void updateStatistics() {
        int totalBookings = 0;
        for (StudyRoom r : Storage.rooms) {
            for (var entry : r.getBookingMap().entrySet()) {
                totalBookings += entry.getValue().size();
            }
        }

        totalRoomsLabel.setText("Total Rooms: " + Storage.rooms.size());
        totalBookingsLabel.setText("Total Bookings: " + totalBookings);
    }

    @FXML
    public void addRoom() {
        try {
            String code = roomCodeField.getText().trim().toUpperCase();
            if (code.isEmpty()) {
                showError("Room code required");
                return;
            }

            int capacity = Integer.parseInt(capacityField.getText().trim());
            if (capacity <= 0) {
                showError("Capacity must be positive");
                return;
            }

            // Check for duplicate
            for (StudyRoom r : Storage.rooms) {
                if (r.getRoomCode().equalsIgnoreCase(code)) {
                    showError("Room code already exists!");
                    return;
                }
            }

            StudyRoom room = currentAdmin.createRoom(code, capacity);
            Storage.saveRooms();

            refreshAllData();
            roomCodeField.clear();
            capacityField.clear();
            updateStatus("Room " + code + " created successfully!");

        } catch (NumberFormatException e) {
            showError("Please enter a valid number for capacity");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void addTimeSlot() {
        try {
            StudyRoom room = roomChoice.getValue();
            if (room == null) {
                showError("Select a room first");
                return;
            }

            String slot = timeSlotField.getText().trim();
            if (slot.isEmpty()) {
                showError("Time slot required");
                return;
            }

            if (room.getTimeSlots().contains(slot)) {
                showError("Time slot already exists!");
                return;
            }

            currentAdmin.addTimeSlot(room, slot);
            Storage.saveRooms();

            refreshTimeSlotList(room);
            timeSlotField.clear();
            updateStatus("Time slot added: " + slot);

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void addAvailableDate() {
        try {
            StudyRoom room = roomChoice.getValue();
            if (room == null) {
                showError("Select a room first");
                return;
            }

            LocalDate date = datePicker.getValue();
            if (date == null) {
                showError("Select a date");
                return;
            }

            if (room.isDateAvailable(date)) {
                showError("Date already available!");
                return;
            }

            room.addAvailableDate(date);
            Storage.saveRooms();
            refreshAvailableDatesList(room);
            updateStatus("Date added: " + date.toString());
            datePicker.setValue(null);

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void removeAvailableDate() {
        StudyRoom room = roomChoice.getValue();
        if (room == null) {
            showError("Select a room first");
            return;
        }

        String selected = availableDatesList.getSelectionModel().getSelectedItem();
        if (selected == null || selected.equals("No available dates set")) {
            showError("Select a date to remove");
            return;
        }

        // Extract date from string (format: "2024-01-01 (Monday)")
        String dateStr = selected.split(" \\(")[0];
        LocalDate date = LocalDate.parse(dateStr);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Remove date '" + date.toString() + "'?\nThis will also delete all bookings for this date.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            room.removeAvailableDate(date);
            Storage.saveRooms();
            refreshAvailableDatesList(room);
            updateStatus("Date removed: " + date.toString());
        }
    }

    @FXML
    public void viewBookings() {
        Platform.runLater(() -> {
            bookingList.getItems().clear();
            int total = 0;

            for (StudyRoom r : Storage.rooms) {
                bookingList.getItems().add("━━━━ " + r.getRoomCode() + " ━━━━");

                if (r.getBookingMap().isEmpty()) {
                    bookingList.getItems().add("  No bookings");
                    continue;
                }

                for (Map.Entry<String, List<Booking>> entry : r.getBookingMap().entrySet()) {
                    String[] keyParts = entry.getKey().split("\\|");
                    String date = keyParts[0];
                    String timeSlot = keyParts[1];

                    for (Booking b : entry.getValue()) {
                        bookingList.getItems().add(
                                "  Date: " + date + " | Time: " + timeSlot + " | Student: " + b.getStudent().getName() +
                                        " (" + b.getStudent().getId() + ")"
                        );
                        total++;
                    }
                }
            }

            if (total == 0) {
                bookingList.getItems().add("No bookings found");
            } else {
                bookingList.getItems().add(0, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                bookingList.getItems().add(0, "TOTAL BOOKINGS: " + total);
            }
        });
    }

    @FXML
    public void refreshData() {
        Storage.loadAllData();
        refreshAllData();
        updateStatus("Data refreshed from storage");
    }

    @FXML
    public void deleteRoom() {
        StudyRoom room = roomChoice.getValue();
        if (room == null) {
            showError("Select a room to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Room: " + room.getRoomCode());
        confirm.setContentText("This will delete all time slots, available dates, and bookings for this room.\n\nAre you sure?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Storage.rooms.remove(room);
            Storage.saveRooms();
            refreshAllData();
            updateStatus("Room deleted: " + room.getRoomCode());
        }
    }

    @FXML
    public void removeTimeSlot() {
        StudyRoom room = roomChoice.getValue();
        if (room == null) {
            showError("Select a room first");
            return;
        }

        String selected = timeSlotList.getSelectionModel().getSelectedItem();
        if (selected == null || selected.equals("No time slots added yet")) {
            showError("Select a time slot to remove");
            return;
        }

        String slot = selected;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Remove time slot '" + slot + "'?\nThis will also delete all bookings for this slot.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            room.removeTimeSlot(slot);
            Storage.saveRooms();
            refreshTimeSlotList(room);
            updateStatus("Time slot removed: " + slot);
        }
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            new Thread(() -> {
                try { Thread.sleep(3000); } catch (InterruptedException e) {}
                Platform.runLater(() -> {
                    if (statusLabel.getText().equals(message)) {
                        statusLabel.setText("Ready");
                    }
                });
            }).start();
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Error: " + message);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();

            new Thread(() -> {
                try { Thread.sleep(3000); } catch (InterruptedException e) {}
                Platform.runLater(() -> {
                    if (statusLabel.getText().equals("Error: " + message)) {
                        statusLabel.setText("Ready");
                    }
                });
            }).start();
        });
    }

    @FXML
    public void logout() {
        Storage.saveAllData();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/studyroomreservation/LoginView.fxml")
            );
            Stage stage = (Stage) roomCodeField.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            String css = getClass().getResource("/com/example/studyroomreservation/styles.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }
            stage.setScene(scene);
            stage.setTitle("Study Room System");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}