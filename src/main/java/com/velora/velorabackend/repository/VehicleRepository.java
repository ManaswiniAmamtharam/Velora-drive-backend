
package com.velora.velorabackend.repository;

import com.velora.velorabackend.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findBySellerId(Long sellerId);
}
