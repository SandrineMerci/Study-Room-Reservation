package com.example.studyroomreservation.exception;

public class RoomFullException extends RuntimeException {
    public RoomFullException(String msg) {
        super(msg);
    }
}