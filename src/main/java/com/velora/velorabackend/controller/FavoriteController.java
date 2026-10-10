
package com.velora.velorabackend.controller;

import com.velora.velorabackend.model.Favorite;
import com.velora.velorabackend.model.Vehicle;
import com.velora.velorabackend.repository.FavoriteRepository;
import com.velora.velorabackend.repository.VehicleRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteRepository favorites;
    private final VehicleRepository vehicles;

    public FavoriteController(
            FavoriteRepository favorites,
            VehicleRepository vehicles) {
        this.favorites = favorites;
        this.vehicles = vehicles;
    }

    private Long currentUserId(HttpSession session) {
        Object id = session.getAttribute("userId");

        if (id instanceof Number) {
            return ((Number) id).longValue();
        }

        return null;
    }

    // Get complete vehicle details for the logged-in user's favorites
    @GetMapping
    public ResponseEntity<?> getFavorites(HttpSession session) {
        Long userId = currentUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in."));
        }

        List<Vehicle> favoriteVehicles = favorites
                .findByUserId(userId)
                .stream()
                .map(Favorite::getVehicleId)
                .map(vehicles::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();

        return ResponseEntity.ok(favoriteVehicles);
    }

    // Add a vehicle to favorites
    @PostMapping("/{vehicleId}")
    public ResponseEntity<?> addFavorite(
            @PathVariable Long vehicleId,
            HttpSession session) {

        Long userId = currentUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in."));
        }

        if (!vehicles.existsById(vehicleId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Vehicle not found."));
        }

        if (favorites.findByUserIdAndVehicleId(userId, vehicleId)
                .isEmpty()) {
            Favorite favorite = new Favorite();
            favorite.userId = userId;
            favorite.vehicleId = vehicleId;
            favorites.save(favorite);
        }

        return ResponseEntity.ok(
                Map.of("message", "Vehicle added to favorites.")
        );
    }

    // Remove a vehicle from favorites
    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<?> removeFavorite(
            @PathVariable Long vehicleId,
            HttpSession session) {

        Long userId = currentUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in."));
        }

        favorites.findByUserIdAndVehicleId(userId, vehicleId)
                .ifPresent(favorites::delete);

        return ResponseEntity.noContent().build();
    }
}
