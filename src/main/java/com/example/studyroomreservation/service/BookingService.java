package com.example.studyroomreservation.service;

import com.example.studyroomreservation.model.*;
import com.example.studyroomreservation.exception.RoomFullException;

public class BookingService {

    public void bookRoom(Booking booking) {
        StudyRoom room = booking.getRoom();
        String timeSlot = booking.getTimeSlot();
        Student student = booking.getStudent();

        // Validate inputs
        if (room == null) throw new RuntimeException("Invalid room");
        if (timeSlot == null || timeSlot.isEmpty()) throw new RuntimeException("Select a time slot");
        if (!room.getTimeSlots().contains(timeSlot)) throw new RuntimeException("Time slot not available for this room");

        // Check capacity
        room.getBookingMap().putIfAbsent(timeSlot, new java.util.ArrayList<>());

        // Check if student already booked
        for (Booking b : room.getBookingMap().get(timeSlot)) {
            if (b.getStudent().getId().equals(student.getId())) {
                throw new RuntimeException("You have already booked this time slot!");
            }
        }

        // Check if full
        if (room.getBookingMap().get(timeSlot).size() >= room.getCapacity()) {
            throw new RoomFullException("Time slot is full! Max capacity: " + room.getCapacity());
        }

        // Add booking to room
        room.getBookingMap().get(timeSlot).add(booking);

        // Add booking to student
        student.addBooking(booking);

        // Save to storage
        Storage.saveRooms();
        Storage.saveStudents();

        System.out.println("Booking confirmed: " + student.getName() + " -> " +
                room.getRoomCode() + " at " + timeSlot);
    }

    public void cancelBooking(Student student, StudyRoom room, String timeSlot) {
        // Remove from room
        room.cancelBooking(timeSlot, student.getId());

        // Remove from student's bookings
        student.getMyBookings().removeIf(b ->
                b.getRoom().equals(room) && b.getTimeSlot().equals(timeSlot)
        );

        Storage.saveRooms();
        Storage.saveStudents();

        System.out.println("Booking cancelled: " + student.getName() + " -> " +
                room.getRoomCode() + " at " + timeSlot);
    }
}