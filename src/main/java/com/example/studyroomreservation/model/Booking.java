package com.example.studyroomreservation.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    private Student student;
    private StudyRoom room;
    private String timeSlot;
    private LocalDateTime bookingTime;

    public Booking(Student student, StudyRoom room, String timeSlot) {
        this.student = student;
        this.room = room;
        this.timeSlot = timeSlot;
        this.bookingTime = LocalDateTime.now();
    }

    // Getters
    public Student getStudent() { return student; }
    public StudyRoom getRoom() { return room; }
    public String getTimeSlot() { return timeSlot; }
    public LocalDateTime getBookingTime() { return bookingTime; }

    public String getFormattedBookingTime() {
        return bookingTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @Override
    public String toString() {
        return room.getRoomCode() + " | " + timeSlot + " | Booked: " + getFormattedBookingTime();
    }
}