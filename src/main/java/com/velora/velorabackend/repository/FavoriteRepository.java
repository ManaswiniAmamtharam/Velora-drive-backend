
package com.velora.velorabackend.repository;

import com.velora.velorabackend.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserId(Long userId);
    Optional<Favorite> findByUserIdAndVehicleId(
        Long userId, Long vehicleId
    );
}
