
package com.velora.velorabackend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String type;

    @Column(nullable = false)
    public String brand;

    @Column(nullable = false)
    public String model;

    @Column(name = "vehicle_year")
    public Integer year;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal price;

    public String fuel;
    public String transmission;
    public Integer kilometers;
    public Integer owners;
    public String location;
    public String color;

    @Column(length = 2000)
    public String image;

    @Column(length = 4000)
    public String description;

    @Column(nullable = false)
    public Long sellerId;

    public LocalDateTime createdAt = LocalDateTime.now();

    public Vehicle() {}
}
