
package com.velora.velorabackend.controller;

import com.velora.velorabackend.model.Enquiry;
import com.velora.velorabackend.model.Vehicle;
import com.velora.velorabackend.repository.EnquiryRepository;
import com.velora.velorabackend.repository.VehicleRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/enquiries")
public class EnquiryController {

    private final EnquiryRepository enquiries;
    private final VehicleRepository vehicles;

    public EnquiryController(
            EnquiryRepository enquiries,
            VehicleRepository vehicles) {
        this.enquiries = enquiries;
        this.vehicles = vehicles;
    }

    private Long currentUserId(HttpSession session) {
        Object id = session.getAttribute("userId");

        if (id instanceof Number) {
            return ((Number) id).longValue();
        }

        return null;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        Long buyerId = currentUserId(session);

        if (buyerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in."));
        }

        Long vehicleId;

        try {
            vehicleId = Long.valueOf(
                    String.valueOf(body.get("vehicleId")));
        } catch (Exception exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "A valid vehicleId is required."));
        }

        Vehicle vehicle = vehicles.findById(vehicleId).orElse(null);

        if (vehicle == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Vehicle not found."));
        }

        if (vehicle.sellerId == null ||
                vehicle.sellerId.equals(buyerId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "You cannot enquire about your own listing."));
        }

        String message = String.valueOf(
                body.getOrDefault("message", "")).trim();

        String phone = String.valueOf(
                body.getOrDefault("phone", "")).trim();

        if (message.isBlank() || message.length() > 2000) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Enter a message of up to 2000 characters."));
        }

        if (phone.isBlank() || phone.length() > 30) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Enter a valid phone number."));
        }

        Enquiry enquiry = new Enquiry();
        enquiry.buyerId = buyerId;
        enquiry.sellerId = vehicle.sellerId;
        enquiry.vehicleId = vehicleId;
        enquiry.message = message;
        enquiry.phone = phone;
        enquiry.createdAt = LocalDateTime.now();

        Enquiry savedEnquiry = enquiries.save(enquiry);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedEnquiry);
    }

    @GetMapping("/sent")
    public ResponseEntity<?> sent(HttpSession session) {
        Long userId = currentUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in."));
        }

        return ResponseEntity.ok(
                enquiries.findByBuyerIdOrderByCreatedAtDesc(userId));
    }

    @GetMapping("/received")
    public ResponseEntity<?> received(HttpSession session) {
        Long userId = currentUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in."));
        }

        return ResponseEntity.ok(
                enquiries.findBySellerIdOrderByCreatedAtDesc(userId));
    }
}
