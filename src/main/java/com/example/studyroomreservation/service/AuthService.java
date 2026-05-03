package com.example.studyroomreservation.service;

import com.example.studyroomreservation.model.*;

public class AuthService {

    public Student registerStudent(String name, String id) {
        // Check if student already exists
        if (Storage.students.containsKey(id)) {
            throw new RuntimeException("Student ID already exists!");
        }

        Student student = new Student(name, id);
        Storage.students.put(id, student);
        Storage.saveStudents();
        return student;
    }

    public Person login(String id, String name) {
        // Check for admin
        if (id.equals("A001") && name.equalsIgnoreCase("Admin")) {
            return new Admin("Admin", "A001");
        }

        // Check for student
        Student student = Storage.students.get(id);
        if (student != null && student.getName().equals(name)) {
            return student;
        }

        return null;
    }

    public boolean isAdmin(String id) {
        return "A001".equals(id);
    }
}