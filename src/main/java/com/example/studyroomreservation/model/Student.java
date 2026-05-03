package com.example.studyroomreservation.model;

import java.util.ArrayList;
import java.util.List;

public class Student extends Person {
    private List<Booking> myBookings = new ArrayList<>();

    public Student(String name, String id) {
        super(name, id);
    }

    @Override
    public void displayInfo() {
        System.out.println("Student: " + name + " | ID: " + id);
    }

    public void addBooking(Booking booking) {
        myBookings.add(booking);
    }

    public void removeBooking(Booking booking) {
        myBookings.remove(booking);
    }

    public List<Booking> getMyBookings() {
        return myBookings;
    }
}