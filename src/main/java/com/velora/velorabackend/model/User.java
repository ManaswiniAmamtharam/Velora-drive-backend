
package com.velora.velorabackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false, unique = true)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Column(length = 20)
    public String mobile;

    @Column(length = 100)
    public String city;

    public LocalDateTime createdAt = LocalDateTime.now();

    public User() {}
}

