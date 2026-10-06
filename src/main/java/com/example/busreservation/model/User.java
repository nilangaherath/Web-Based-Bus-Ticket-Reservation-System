package com.example.busreservation.model;

import jakarta.persistence.*;

/**
 * User Model - Represents all users in the system
 * Used by: ALL ROLES (Passenger, IT Support, Customer Service, Finance, Ticketing, Operation Manager)
 * Contains: User profile information, authentication details, and role-based access
 */
@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique user ID

    private String firstName; // User's first name
    private String lastName; // User's last name
    private String phone; // Contact phone number
    private String email; // Email address (used for login)
    private String password; // Encrypted password
    private String role; // User role (Passenger, IT Support, Customer Service, etc.)
    private String nic; // National Identity Card number

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
}
