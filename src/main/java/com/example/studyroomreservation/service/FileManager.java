package com.example.studyroomreservation.service;

import com.example.studyroomreservation.model.Booking;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class FileManager {

    private static final String FILE = "bookings.txt";

    public static void save(Booking b) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(FILE, true))) {

            w.write(b.getStudent().getName() + "," +
                    b.getStudent().getId() + "," +
                    b.getRoom().getRoomCode() + "," +
                    b.getTimeSlot());

            w.newLine();

        } catch (Exception e) {
            System.out.println("Save error");
        }
    }
}