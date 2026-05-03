package com.example.studyroomreservation.service;

import com.example.studyroomreservation.model.*;
import com.example.studyroomreservation.exception.RoomFullException;
import java.time.LocalDate;

public class BookingService {

    public void bookRoom(Booking booking) {
        StudyRoom room = booking.getRoom();
        LocalDate bookingDate = booking.getBookingDate();
        String timeSlot = booking.getTimeSlot();
        Student student = booking.getStudent();

        // Validate inputs
        if (room == null) throw new RuntimeException("Invalid room");
        if (bookingDate == null) throw new RuntimeException("Select a date");
        if (bookingDate.isBefore(LocalDate.now())) throw new RuntimeException("Cannot book past dates");
        if (timeSlot == null || timeSlot.isEmpty()) throw new RuntimeException("Select a time slot");
        if (!room.getTimeSlots().contains(timeSlot)) throw new RuntimeException("Time slot not available for this room");

        // Check if student already booked
        String key = bookingDate.toString() + "|" + timeSlot;
        room.getBookingMap().putIfAbsent(key, new java.util.ArrayList<>());

        for (Booking b : room.getBookingMap().get(key)) {
            if (b.getStudent().getId().equals(student.getId())) {
                throw new RuntimeException("You have already booked this time slot on " + bookingDate.toString() + "!");
            }
        }

        // Check if full
        if (room.getBookingMap().get(key).size() >= room.getCapacity()) {
            throw new RoomFullException("Time slot is full on " + bookingDate.toString() + "! Max capacity: " + room.getCapacity());
        }

        // Add booking to room
        room.getBookingMap().get(key).add(booking);

        // Add booking to student
        student.addBooking(booking);

        // Save to storage
        Storage.saveRooms();
        Storage.saveStudents();

        System.out.println("Booking confirmed: " + student.getName() + " -> " +
                room.getRoomCode() + " on " + bookingDate.toString() + " at " + timeSlot);
    }

    public void cancelBooking(Student student, StudyRoom room, LocalDate bookingDate, String timeSlot) {
        // Remove from room
        room.cancelBooking(bookingDate, timeSlot, student.getId());

        // Remove from student's bookings
        student.getMyBookings().removeIf(b ->
                b.getRoom().equals(room) &&
                        b.getBookingDate().equals(bookingDate) &&
                        b.getTimeSlot().equals(timeSlot)
        );

        Storage.saveRooms();
        Storage.saveStudents();

        System.out.println("Booking cancelled: " + student.getName() + " -> " +
                room.getRoomCode() + " on " + bookingDate.toString() + " at " + timeSlot);
    }
}