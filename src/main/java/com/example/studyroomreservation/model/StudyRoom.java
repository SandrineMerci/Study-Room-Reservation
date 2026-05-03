package com.example.studyroomreservation.model;

import com.example.studyroomreservation.exception.RoomFullException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

public class StudyRoom implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomCode;
    private int capacity;
    private List<String> timeSlots = new ArrayList<>();
    private List<LocalDate> availableDates = new ArrayList<>(); // Admin sets available dates
    private Map<String, List<Booking>> bookingMap = new HashMap<>(); // Key: "date|timeSlot"

    public StudyRoom(String roomCode, int capacity) {
        this.roomCode = roomCode;
        this.capacity = capacity;
    }

    public void addTimeSlot(String slot) {
        if (!timeSlots.contains(slot)) {
            timeSlots.add(slot);
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
        // Remove all bookings for this time slot across all dates
        List<String> keysToRemove = new ArrayList<>();
        for (String key : bookingMap.keySet()) {
            if (key.endsWith("|" + slot)) {
                keysToRemove.add(key);
            }
        }
        for (String key : keysToRemove) {
            bookingMap.remove(key);
        }
    }

    public void addAvailableDate(LocalDate date) {
        if (!availableDates.contains(date)) {
            availableDates.add(date);
            Collections.sort(availableDates);
        }
    }

    public void removeAvailableDate(LocalDate date) {
        availableDates.remove(date);
        // Remove all bookings for this date
        List<String> keysToRemove = new ArrayList<>();
        for (String key : bookingMap.keySet()) {
            if (key.startsWith(date.toString() + "|")) {
                keysToRemove.add(key);
            }
        }
        for (String key : keysToRemove) {
            bookingMap.remove(key);
        }
    }

    public boolean isDateAvailable(LocalDate date) {
        return availableDates.contains(date);
    }

    private String getBookingKey(LocalDate date, String timeSlot) {
        return date.toString() + "|" + timeSlot;
    }

    public void addBooking(LocalDate date, String timeSlot, Booking booking) {
        if (!availableDates.contains(date)) {
            throw new RuntimeException("This date is not available for booking!");
        }

        String key = getBookingKey(date, timeSlot);
        bookingMap.putIfAbsent(key, new ArrayList<>());
        List<Booking> list = bookingMap.get(key);

        // Check if student already booked this slot on this date
        for (Booking b : list) {
            if (b.getStudent().getId().equals(booking.getStudent().getId())) {
                throw new RuntimeException("You have already booked this time slot on this date!");
            }
        }

        if (list.size() >= capacity) {
            throw new RoomFullException("Time slot is full on this date! Max capacity: " + capacity);
        }

        list.add(booking);
    }

    public void cancelBooking(LocalDate date, String timeSlot, String studentId) {
        String key = getBookingKey(date, timeSlot);
        List<Booking> list = bookingMap.get(key);
        if (list != null) {
            list.removeIf(b -> b.getStudent().getId().equals(studentId));
            if (list.isEmpty()) {
                bookingMap.remove(key);
            }
        }
    }

    public boolean isTimeSlotAvailable(LocalDate date, String timeSlot) {
        if (!availableDates.contains(date)) return false;
        String key = getBookingKey(date, timeSlot);
        List<Booking> list = bookingMap.get(key);
        return list == null || list.size() < capacity;
    }

    public int getAvailableSeats(LocalDate date, String timeSlot) {
        if (!availableDates.contains(date)) return 0;
        String key = getBookingKey(date, timeSlot);
        List<Booking> list = bookingMap.get(key);
        int booked = (list == null) ? 0 : list.size();
        return capacity - booked;
    }

    public List<Booking> getBookingsForDate(LocalDate date) {
        List<Booking> result = new ArrayList<>();
        String datePrefix = date.toString() + "|";
        for (Map.Entry<String, List<Booking>> entry : bookingMap.entrySet()) {
            if (entry.getKey().startsWith(datePrefix)) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }

    // Getters
    public String getRoomCode() { return roomCode; }
    public int getCapacity() { return capacity; }
    public List<String> getTimeSlots() { return timeSlots; }
    public List<LocalDate> getAvailableDates() { return availableDates; }
    public Map<String, List<Booking>> getBookingMap() { return bookingMap; }

    public void setCapacity(int capacity) { this.capacity = capacity; }

    @Override
    public String toString() {
        return roomCode + " (Cap: " + capacity + ")";
    }
}