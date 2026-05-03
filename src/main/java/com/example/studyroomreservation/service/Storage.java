package com.example.studyroomreservation.service;

import com.example.studyroomreservation.model.*;

import java.io.*;
import java.util.*;

public class Storage {

    public static List<StudyRoom> rooms = new ArrayList<>();
    public static Map<String, Student> students = new HashMap<>();
    private static final String ROOMS_FILE = "rooms_data.ser";
    private static final String STUDENTS_FILE = "students_data.ser";
    private static final String BOOKINGS_FILE = "bookings_data.ser";

    // SAVE ALL DATA
    public static void saveAllData() {
        saveRooms();
        saveStudents();
        saveBookings();
    }

    // LOAD ALL DATA
    public static void loadAllData() {
        loadRooms();
        loadStudents();
        loadBookings();
        syncBookingsWithRooms();
    }

    // SAVE ROOMS with complete data
    public static void saveRooms() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ROOMS_FILE))) {
            oos.writeObject(rooms);
            System.out.println("Rooms saved: " + rooms.size());
        } catch (Exception e) {
            System.out.println("Room save error: " + e.getMessage());
        }
    }

    // LOAD ROOMS
    @SuppressWarnings("unchecked")
    public static void loadRooms() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ROOMS_FILE))) {
            rooms = (List<StudyRoom>) ois.readObject();
            System.out.println("Rooms loaded: " + rooms.size());
        } catch (FileNotFoundException e) {
            rooms = new ArrayList<>();
            System.out.println("No existing rooms file, starting fresh");
        } catch (Exception e) {
            rooms = new ArrayList<>();
            System.out.println("Error loading rooms: " + e.getMessage());
        }
    }

    // SAVE STUDENTS
    public static void saveStudents() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STUDENTS_FILE))) {
            oos.writeObject(students);
            System.out.println("Students saved: " + students.size());
        } catch (Exception e) {
            System.out.println("Student save error: " + e.getMessage());
        }
    }

    // LOAD STUDENTS
    @SuppressWarnings("unchecked")
    public static void loadStudents() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(STUDENTS_FILE))) {
            students = (Map<String, Student>) ois.readObject();
            System.out.println("Students loaded: " + students.size());
        } catch (FileNotFoundException e) {
            students = new HashMap<>();
            System.out.println("No existing students file, starting fresh");
        } catch (Exception e) {
            students = new HashMap<>();
            System.out.println("Error loading students: " + e.getMessage());
        }
    }

    // SAVE BOOKINGS (backup)
    public static void saveBookings() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BOOKINGS_FILE))) {
            Map<String, Map<String, List<Booking>>> allBookings = new HashMap<>();
            for (StudyRoom room : rooms) {
                allBookings.put(room.getRoomCode(), room.getBookingMap());
            }
            oos.writeObject(allBookings);
        } catch (Exception e) {
            System.out.println("Bookings save error: " + e.getMessage());
        }
    }

    // LOAD BOOKINGS
    @SuppressWarnings("unchecked")
    public static void loadBookings() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BOOKINGS_FILE))) {
            // Bookings are loaded through rooms, this is just backup
            System.out.println("Bookings backup loaded");
        } catch (Exception e) {
            System.out.println("No existing bookings file");
        }
    }

    // Sync student references in bookings
    private static void syncBookingsWithRooms() {
        for (StudyRoom room : rooms) {
            for (List<Booking> bookings : room.getBookingMap().values()) {
                for (Booking b : bookings) {
                    Student s = students.get(b.getStudent().getId());
                    if (s != null) {
                        // This ensures student has reference to booking
                        if (!s.getMyBookings().contains(b)) {
                            s.addBooking(b);
                        }
                    }
                }
            }
        }
    }
}