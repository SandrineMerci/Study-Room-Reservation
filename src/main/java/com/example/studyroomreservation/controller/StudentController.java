package com.example.studyroomreservation.controller;

import com.example.studyroomreservation.model.*;
import com.example.studyroomreservation.service.BookingService;
import com.example.studyroomreservation.service.Storage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import java.util.List;
import java.util.stream.Collectors;

public class StudentController {

    @FXML private ChoiceBox<StudyRoom> roomChoice;
    @FXML private ChoiceBox<String> timeChoice;
    @FXML private Label statusLabel;
    @FXML private Label sessionStatus;
    @FXML private Label roomDetailsLabel;
    @FXML private Label availableSeatsLabel;
    @FXML private ListView<HBox> myBookingsList;

    private Student currentStudent;
    private BookingService bookingService = new BookingService();

    @FXML
    public void initialize() {
        setupEventListeners();
    }

    private void setupEventListeners() {
        roomChoice.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldRoom, newRoom) -> {
                    if (newRoom != null) {
                        updateTimeSlots(newRoom);
                        roomDetailsLabel.setText("Room: " + newRoom.getRoomCode() + " | Capacity: " + newRoom.getCapacity());
                    } else {
                        roomDetailsLabel.setText("Room: Not selected");
                    }
                }
        );

        timeChoice.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTime, newTime) -> {
                    if (newTime != null && roomChoice.getValue() != null) {
                        String slot = extractSlotTime(newTime);
                        int available = roomChoice.getValue().getAvailableSeats(slot);
                        availableSeatsLabel.setText("Available seats: " + available);
                    }
                }
        );
    }

    public void setCurrentStudent(Student student) {
        this.currentStudent = student;
        statusLabel.setText("Welcome, " + student.getName() + "!");
        sessionStatus.setText("Logged in as: " + student.getName() + " (" + student.getId() + ")");
        refreshData();
    }

    private void refreshData() {
        refreshRoomList();
        refreshMyBookings();
    }

    private void refreshRoomList() {
        Platform.runLater(() -> {
            roomChoice.getItems().clear();
            if (Storage.rooms.isEmpty()) {
                roomChoice.setDisable(true);
                return;
            }
            roomChoice.getItems().addAll(Storage.rooms);
            roomChoice.getSelectionModel().selectFirst();
            roomChoice.setDisable(false);
        });
    }

    private void updateTimeSlots(StudyRoom room) {
        Platform.runLater(() -> {
            timeChoice.getItems().clear();
            if (room.getTimeSlots().isEmpty()) {
                timeChoice.getItems().add("No time slots available");
                timeChoice.setDisable(true);
                return;
            }

            for (String slot : room.getTimeSlots()) {
                int available = room.getAvailableSeats(slot);
                String prefix = available > 0 ? "[Available] " : "[Full] ";
                timeChoice.getItems().add(prefix + slot + " (" + available + "/" + room.getCapacity() + ")");
            }
            timeChoice.setDisable(false);
        });
    }

    // FIXED: Only shows current student's bookings by filtering
    private void refreshMyBookings() {
        Platform.runLater(() -> {
            myBookingsList.getItems().clear();

            if (currentStudent == null) {
                HBox emptyBox = new HBox();
                Label emptyLabel = new Label("Not logged in");
                emptyLabel.setStyle("-fx-text-fill: #94a3b8;");
                emptyBox.getChildren().add(emptyLabel);
                myBookingsList.getItems().add(emptyBox);
                return;
            }

            // Clear student's existing bookings and rebuild from rooms
            currentStudent.getMyBookings().clear();

            // Scan all rooms and find bookings for this student
            for (StudyRoom room : Storage.rooms) {
                for (List<Booking> bookings : room.getBookingMap().values()) {
                    for (Booking booking : bookings) {
                        if (booking.getStudent().getId().equals(currentStudent.getId())) {
                            // Add to student's booking list
                            currentStudent.addBooking(booking);
                        }
                    }
                }
            }

            List<Booking> studentBookings = currentStudent.getMyBookings();

            if (studentBookings.isEmpty()) {
                HBox emptyBox = new HBox();
                Label emptyLabel = new Label("No bookings yet");
                emptyLabel.setStyle("-fx-text-fill: #94a3b8;");
                emptyBox.getChildren().add(emptyLabel);
                myBookingsList.getItems().add(emptyBox);
                return;
            }

            // Display only this student's bookings
            for (Booking b : studentBookings) {
                HBox bookingItem = createBookingItem(b);
                myBookingsList.getItems().add(bookingItem);
            }
        });
    }

    private HBox createBookingItem(Booking booking) {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setStyle("-fx-padding: 10; -fx-background-color: #f8fafc; -fx-background-radius: 8;");

        Label roomLabel = new Label(booking.getRoom().getRoomCode());
        roomLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 70; -fx-text-fill: #1e293b;");

        Label timeLabel = new Label(booking.getTimeSlot());
        timeLabel.setStyle("-fx-text-fill: #4361ee; -fx-font-weight: 500; -fx-min-width: 110;");

        Label dateLabel = new Label(booking.getFormattedBookingTime());
        dateLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");

        container.getChildren().addAll(roomLabel, timeLabel, dateLabel);

        return container;
    }

    private String extractSlotTime(String display) {
        String slot = display;
        int bracketIndex = display.indexOf("]");
        if (bracketIndex >= 0) {
            slot = display.substring(bracketIndex + 1).trim();
        }
        int parenIndex = slot.indexOf("(");
        if (parenIndex > 0) {
            slot = slot.substring(0, parenIndex).trim();
        }
        return slot;
    }

    @FXML
    public void bookRoom() {
        StudyRoom room = roomChoice.getValue();
        String timeDisplay = timeChoice.getValue();

        if (room == null) {
            showStatus("Please select a room", "error");
            return;
        }

        if (timeDisplay == null || timeDisplay.equals("No time slots available")) {
            showStatus("Please select a time slot", "error");
            return;
        }

        String timeSlot = extractSlotTime(timeDisplay);

        if (!room.isTimeSlotAvailable(timeSlot)) {
            showStatus("This time slot is now full", "error");
            updateTimeSlots(room);
            return;
        }

        try {
            Booking booking = new Booking(currentStudent, room, timeSlot);
            bookingService.bookRoom(booking);

            // Refresh the display
            refreshMyBookings();
            updateTimeSlots(room);
            showStatus("Successfully booked " + room.getRoomCode() + " at " + timeSlot, "success");

        } catch (Exception e) {
            showStatus(e.getMessage(), "error");
        }
    }

    @FXML
    public void cancelBooking() {
        int selectedIndex = myBookingsList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            showStatus("Please select a booking to cancel", "error");
            return;
        }

        HBox selectedItem = myBookingsList.getItems().get(selectedIndex);
        if (selectedItem.getChildren().size() < 2) {
            showStatus("Invalid selection", "error");
            return;
        }

        try {
            Label roomLabel = (Label) selectedItem.getChildren().get(0);
            Label timeLabel = (Label) selectedItem.getChildren().get(1);
            String roomCode = roomLabel.getText();
            String timeSlot = timeLabel.getText();

            StudyRoom room = Storage.rooms.stream()
                    .filter(r -> r.getRoomCode().equals(roomCode))
                    .findFirst()
                    .orElse(null);

            if (room != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Cancel Booking");
                confirm.setHeaderText("Confirm Cancellation");
                confirm.setContentText("Cancel booking for " + roomCode + " at " + timeSlot + "?");

                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    bookingService.cancelBooking(currentStudent, room, timeSlot);

                    // Refresh the display
                    refreshMyBookings();
                    updateTimeSlots(room);
                    showStatus("Booking cancelled successfully", "success");
                }
            }
        } catch (Exception e) {
            showStatus("Error cancelling: " + e.getMessage(), "error");
        }
    }

    private void showStatus(String message, String type) {
        Platform.runLater(() -> {
            String prefix = type.equals("success") ? "Success: " : "Error: ";
            statusLabel.setText(prefix + message);

            new Thread(() -> {
                try { Thread.sleep(3000); } catch (InterruptedException e) {}
                Platform.runLater(() -> {
                    if (statusLabel.getText().equals(prefix + message)) {
                        statusLabel.setText("Welcome, " + currentStudent.getName() + "!");
                    }
                });
            }).start();
        });
    }

    @FXML
    public void logout() {
        Storage.saveAllData();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to logout?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/studyroomreservation/LoginView.fxml")
                );
                Stage stage = (Stage) statusLabel.getScene().getWindow();
                Scene scene = new Scene(loader.load());

                // Load and apply CSS
                String css = getClass().getResource("/com/example/studyroomreservation/styles.css").toExternalForm();
                if (css != null) {
                    scene.getStylesheets().add(css);
                }

                stage.setScene(scene);
                stage.setTitle("Study Room Reservation System");
                stage.centerOnScreen();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}