package com.example.studyroomreservation.model;

import com.example.studyroomreservation.exception.RoomFullException;
import java.io.Serializable;
import java.util.*;

public class StudyRoom implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomCode;
    private int capacity;
    private List<String> timeSlots = new ArrayList<>();
    private Map<String, List<Booking>> bookingMap = new HashMap<>();

    public StudyRoom(String roomCode, int capacity) {
        this.roomCode = roomCode;
        this.capacity = capacity;
    }

    public void addTimeSlot(String slot) {
        if (!timeSlots.contains(slot)) {
            timeSlots.add(slot);
            // Auto-sort time slots for better UX
            timeSlots.sort((a, b) -> {
                try {
                    return a.compareTo(b);
                } catch (Exception e) {
                    return 0;
                }
            });
        }
    }

    public void removeTimeSlot(String slot) {
        timeSlots.remove(slot);
        bookingMap.remove(slot); // Remove associated bookings
    }

    public void addBooking(String timeSlot, Booking booking) {
        bookingMap.putIfAbsent(timeSlot, new ArrayList<>());
        List<Booking> list = bookingMap.get(timeSlot);

        // Check if student already booked this slot
        for (Booking b : list) {
            if (b.getStudent().getId().equals(booking.getStudent().getId())) {
                throw new RuntimeException("You have already booked this time slot!");
            }
        }

        if (list.size() >= capacity) {
            throw new RoomFullException("Time slot is full! Max capacity: " + capacity);
        }

        list.add(booking);
    }

    public void cancelBooking(String timeSlot, String studentId) {
        List<Booking> list = bookingMap.get(timeSlot);
        if (list != null) {
            list.removeIf(b -> b.getStudent().getId().equals(studentId));
            if (list.isEmpty()) {
                bookingMap.remove(timeSlot);
            }
        }
    }

    public boolean isTimeSlotAvailable(String timeSlot) {
        List<Booking> list = bookingMap.get(timeSlot);
        return list == null || list.size() < capacity;
    }

    public int getAvailableSeats(String timeSlot) {
        List<Booking> list = bookingMap.get(timeSlot);
        int booked = (list == null) ? 0 : list.size();
        return capacity - booked;
    }

    // Getters
    public String getRoomCode() { return roomCode; }
    public int getCapacity() { return capacity; }
    public List<String> getTimeSlots() { return timeSlots; }
    public Map<String, List<Booking>> getBookingMap() { return bookingMap; }

    public void setCapacity(int capacity) { this.capacity = capacity; }

    @Override
    public String toString() {
        return roomCode + " (Cap: " + capacity + ")";
    }
}