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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentController {

    @FXML private ChoiceBox<StudyRoom> roomChoice;
    @FXML private ChoiceBox<String> dateChoice;
    @FXML private ChoiceBox<String> timeChoice;
    @FXML private Label statusLabel;
    @FXML private Label sessionStatus;
    @FXML private Label roomDetailsLabel;
    @FXML private Label availableSeatsLabel;
    @FXML private ListView<HBox> myBookingsList;

    private Student currentStudent;
    private BookingService bookingService = new BookingService();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd (EEEE)");

    @FXML
    public void initialize() {
        setupEventListeners();
    }

    private void setupEventListeners() {
        roomChoice.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldRoom, newRoom) -> {
                    if (newRoom != null) {
                        updateAvailableDates(newRoom);
                        roomDetailsLabel.setText("Room: " + newRoom.getRoomCode() + " | Capacity: " + newRoom.getCapacity());
                    } else {
                        roomDetailsLabel.setText("Room: Not selected");
                        dateChoice.getItems().clear();
                        timeChoice.getItems().clear();
                    }
                }
        );

        dateChoice.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldDate, newDate) -> {
                    if (newDate != null && roomChoice.getValue() != null) {
                        updateTimeSlots(roomChoice.getValue(), extractDateFromDisplay(newDate));
                    }
                }
        );

        timeChoice.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTime, newTime) -> {
                    if (newTime != null && roomChoice.getValue() != null && dateChoice.getValue() != null) {
                        String slot = extractSlotTime(newTime);
                        LocalDate date = extractDateFromDisplay(dateChoice.getValue());
                        int available = roomChoice.getValue().getAvailableSeats(date, slot);
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

    private void updateAvailableDates(StudyRoom room) {
        Platform.runLater(() -> {
            dateChoice.getItems().clear();
            timeChoice.getItems().clear();
            availableSeatsLabel.setText("Available seats: -");

            if (room.getAvailableDates().isEmpty()) {
                dateChoice.getItems().add("No available dates");
                dateChoice.setDisable(true);
                return;
            }

            for (LocalDate date : room.getAvailableDates()) {
                dateChoice.getItems().add(date.format(dateFormatter));
            }
            dateChoice.setDisable(false);
            dateChoice.getSelectionModel().selectFirst();
        });
    }

    private void updateTimeSlots(StudyRoom room, LocalDate selectedDate) {
        Platform.runLater(() -> {
            timeChoice.getItems().clear();

            if (room.getTimeSlots().isEmpty()) {
                timeChoice.getItems().add("No time slots available");
                timeChoice.setDisable(true);
                return;
            }

            for (String slot : room.getTimeSlots()) {
                int available = room.getAvailableSeats(selectedDate, slot);
                String prefix = available > 0 ? "[Available] " : "[Full] ";
                timeChoice.getItems().add(prefix + slot + " (" + available + "/" + room.getCapacity() + ")");
            }
            timeChoice.setDisable(false);
        });
    }

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

            // Sort by date
            studentBookings.sort((b1, b2) -> b1.getBookingDate().compareTo(b2.getBookingDate()));

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
        roomLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 60; -fx-text-fill: #1e293b;");

        String dateStr = booking.getBookingDate().format(dateFormatter);
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-text-fill: #059669; -fx-min-width: 150; -fx-font-weight: 500;");

        Label timeLabel = new Label(booking.getTimeSlot());
        timeLabel.setStyle("-fx-text-fill: #4361ee; -fx-font-weight: 500; -fx-min-width: 100;");

        Label bookedLabel = new Label("Booked: " + booking.getFormattedBookingTime());
        bookedLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10;");

        container.getChildren().addAll(roomLabel, dateLabel, timeLabel, bookedLabel);

        return container;
    }

    private LocalDate extractDateFromDisplay(String display) {
        String dateStr = display.split(" \\(")[0];
        return LocalDate.parse(dateStr);
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
        String dateDisplay = dateChoice.getValue();
        String timeDisplay = timeChoice.getValue();

        if (room == null) {
            showStatus("Please select a room", "error");
            return;
        }

        if (dateDisplay == null || dateDisplay.equals("No available dates")) {
            showStatus("Please select a date", "error");
            return;
        }

        if (timeDisplay == null || timeDisplay.equals("No time slots available")) {
            showStatus("Please select a time slot", "error");
            return;
        }

        LocalDate bookingDate = extractDateFromDisplay(dateDisplay);
        String timeSlot = extractSlotTime(timeDisplay);

        if (!room.isTimeSlotAvailable(bookingDate, timeSlot)) {
            showStatus("This time slot is now full on " + bookingDate.toString(), "error");
            updateTimeSlots(room, bookingDate);
            return;
        }

        try {
            Booking booking = new Booking(currentStudent, room, bookingDate, timeSlot);
            bookingService.bookRoom(booking);

            refreshMyBookings();
            updateTimeSlots(room, bookingDate);
            showStatus("Successfully booked " + room.getRoomCode() + " on " + bookingDate.toString() + " at " + timeSlot, "success");

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
        if (selectedItem.getChildren().size() < 3) {
            showStatus("Invalid selection", "error");
            return;
        }

        try {
            Label roomLabel = (Label) selectedItem.getChildren().get(0);
            Label dateLabel = (Label) selectedItem.getChildren().get(1);
            Label timeLabel = (Label) selectedItem.getChildren().get(2);

            String roomCode = roomLabel.getText();
            String dateStr = dateLabel.getText().split(" \\(")[0];
            String timeSlot = timeLabel.getText();

            StudyRoom room = Storage.rooms.stream()
                    .filter(r -> r.getRoomCode().equals(roomCode))
                    .findFirst()
                    .orElse(null);

            if (room != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Cancel Booking");
                confirm.setHeaderText("Confirm Cancellation");
                confirm.setContentText("Cancel booking for " + roomCode + " on " + dateStr + " at " + timeSlot + "?");

                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    LocalDate bookingDate = LocalDate.parse(dateStr);
                    bookingService.cancelBooking(currentStudent, room, bookingDate, timeSlot);
                    refreshMyBookings();
                    updateTimeSlots(room, bookingDate);
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
}