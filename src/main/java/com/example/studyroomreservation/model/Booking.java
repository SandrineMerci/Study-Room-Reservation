package com.example.studyroomreservation.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    private Student student;
    private StudyRoom room;
    private LocalDate bookingDate;
    private String timeSlot;
    private LocalDateTime bookingTime;

    public Booking(Student student, StudyRoom room, LocalDate bookingDate, String timeSlot) {
        this.student = student;
        this.room = room;
        this.bookingDate = bookingDate;
        this.timeSlot = timeSlot;
        this.bookingTime = LocalDateTime.now();
    }

    // Getters
    public Student getStudent() { return student; }
    public StudyRoom getRoom() { return room; }
    public LocalDate getBookingDate() { return bookingDate; }
    public String getTimeSlot() { return timeSlot; }
    public LocalDateTime getBookingTime() { return bookingTime; }

    public String getFormattedBookingDate() {
        if (bookingDate == null) return "Date not set";
        return bookingDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getFormattedBookingTime() {
        if (bookingTime == null) return "Time not set";
        return bookingTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @Override
    public String toString() {
        return room.getRoomCode() + " | " + getFormattedBookingDate() + " | " + timeSlot + " | Booked: " + getFormattedBookingTime();
    }
}