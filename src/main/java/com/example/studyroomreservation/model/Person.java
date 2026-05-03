package com.example.studyroomreservation.model;

import java.io.Serializable;

public abstract class Person implements Serializable {
    protected String name;
    protected String id;

    public Person(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() { return name; }
    public String getId() { return id; }

    public void setName(String name) { this.name = name; }
    public void setId(String id) { this.id = id; }

    public abstract void displayInfo();

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}