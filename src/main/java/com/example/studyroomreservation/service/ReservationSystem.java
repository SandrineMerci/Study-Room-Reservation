package com.example.studyroomreservation.service;

import com.example.studyroomreservation.model.StudyRoom;
import java.util.ArrayList;
import java.util.List;

public class ReservationSystem {

    private static ReservationSystem instance;

    private List<StudyRoom> rooms = new ArrayList<>();

    private ReservationSystem() {}

    public static ReservationSystem getInstance() {
        if (instance == null) {
            instance = new ReservationSystem();
        }
        return instance;
    }

    public List<StudyRoom> getRooms() {
        return rooms;
    }

    public StudyRoom addRoom(String code, int capacity) {
        StudyRoom room = new StudyRoom(code, capacity);
        rooms.add(room);
        return room;
    }

    public StudyRoom findRoom(String code) {
        for (StudyRoom r : rooms) {
            if (r.getRoomCode().equals(code)) return r;
        }
        return null;
    }
}