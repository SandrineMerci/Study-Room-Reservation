package com.example.studyroomreservation.model;

import com.example.studyroomreservation.service.Storage;

public class Admin extends Person {

    public Admin(String name, String id) {
        super(name, id);
    }

    @Override
    public void displayInfo() {
        System.out.println("Admin: " + name);
    }

    public StudyRoom createRoom(String code, int capacity) {
        // Check against global storage
        for (StudyRoom r : Storage.rooms) {
            if (r.getRoomCode().equalsIgnoreCase(code)) {
                throw new RuntimeException("Room code already exists!");
            }
        }

        StudyRoom room = new StudyRoom(code, capacity);
        Storage.rooms.add(room);
        return room;
    }

    public void addTimeSlot(StudyRoom room, String slot) {
        if (room == null) {
            throw new RuntimeException("Select a room first");
        }
        if (room.getTimeSlots().contains(slot)) {
            throw new RuntimeException("Time slot already exists!");
        }
        room.addTimeSlot(slot);
    }
}