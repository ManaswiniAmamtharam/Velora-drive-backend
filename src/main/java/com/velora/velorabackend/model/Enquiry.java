
package com.velora.velorabackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "enquiries")
public class Enquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public Long buyerId;

    @Column(nullable = false)
    public Long sellerId;

    @Column(nullable = false)
    public Long vehicleId;

    public String phone;

    @Column(length = 2000)
    public String message;

    public LocalDateTime createdAt = LocalDateTime.now();

    public Enquiry() {}
}
