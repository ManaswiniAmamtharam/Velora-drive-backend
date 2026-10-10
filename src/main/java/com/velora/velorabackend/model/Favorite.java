
package com.velora.velorabackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "favorites",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "vehicle_id"}
    )
)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @Column(name = "vehicle_id", nullable = false)
    public Long vehicleId;

    public LocalDateTime createdAt = LocalDateTime.now();

    public Favorite() {}

public Long getVehicleId() {
    return vehicleId;
}

public Long getUserId() {
    return userId;
}
}