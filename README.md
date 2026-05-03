#  Study Room Reservation System

##  Overview

The **Study Room Reservation System** is a JavaFX-based desktop application that allows students to reserve study rooms based on available time slots, while administrators manage rooms and schedules.

This system demonstrates core software engineering concepts including **Object-Oriented Programming (OOP)**, **JavaFX UI design**, **file persistence**, and **modular architecture**.

---

##  Features

###  Admin Features

* Add new study rooms with capacity
* Prevent duplicate room codes
* Add time slots to rooms
* Prevent duplicate time slots per room
* View all bookings
* Persist rooms and time slots to file
* Logout functionality

###  Student Features

* View available study rooms
* Select available time slots
* Book a room
* Prevent booking when room is full
* Booking confirmation with feedback

---

##  System Architecture

The project follows a layered structure:

```
com.example.studyroomreservation
│
├── controller     # JavaFX Controllers (UI Logic)
├── model          # Core domain classes (OOP)
├── service        # Business logic & persistence
├── exception      # Custom exceptions
└── resources      # FXML UI files
```

---

##  Core Concepts Used

* **Encapsulation, Inheritance, Polymorphism**
* **JavaFX (FXML + Controllers)**
* **Collections (List, Map)**
* **Exception Handling**
* **File I/O for persistence**
* **MVC Design Pattern**

---

##  Key Classes

### 🔹 Model Layer

* `Person` → Abstract base class
* `Student` → Books rooms
* `Admin` → Manages rooms and slots
* `StudyRoom` → Stores capacity, slots, bookings
* `Booking` → Represents a reservation

### 🔹 Service Layer

* `AuthService` → Handles login logic
* `BookingService` → Handles booking operations
* `Storage` → Handles file persistence

---

##  Data Persistence

* Rooms and time slots are stored using file-based storage
* Bookings are saved to a text file (`bookings.txt`)
* Data is reloaded when the application starts

---

##  User Interface

Built using **JavaFX FXML**, with separate views:

* `LoginView.fxml`
* `AdminView.fxml`
* `StudentView.fxml`

---

##  Authentication

* Admin login is predefined:

  ```
  ID: A001
  ```
* Any other ID is treated as a student

---

##  How to Run

###  Prerequisites

* Java JDK 21
* Maven
* JavaFX SDK (configured via Maven)

###  Run using Maven

```bash
mvn clean javafx:run
```

---

##  Known Limitations

* No database (file-based storage only)
* No password authentication
* No multi-user session handling
* UI styling is basic


---

##  Future Improvements

* Replace file storage with a database (PostgreSQL / MySQL)
* Add full authentication system (username/password)
* Improve UI with CSS styling
* Add booking history per student
* Prevent duplicate bookings by same student
* Add search and filtering features
* Use TableView instead of ListView

---

##  Author

Sandrine Marie Merci
– Study Room Reservation

---

