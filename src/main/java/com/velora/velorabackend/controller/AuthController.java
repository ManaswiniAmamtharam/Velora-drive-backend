
package com.velora.velorabackend.controller;

import com.velora.velorabackend.model.User;
import com.velora.velorabackend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;

    private final BCryptPasswordEncoder encoder =
        new BCryptPasswordEncoder();

    public AuthController(UserRepository users) {
        this.users = users;
    }

    private Map<String, Object> publicUser(User user) {
        Map<String, Object> result = new HashMap<>();

        result.put("id", user.id);
        result.put("name", user.name);
        result.put("email", user.email);
        result.put("mobile", user.mobile);
        result.put("city", user.city);

        return result;
    }

    private Long currentUserId(HttpSession session) {
        Object id = session.getAttribute("userId");

        if (id instanceof Number) {
            return ((Number) id).longValue();
        }

        return null;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
        @RequestBody Map<String, String> body,
        HttpSession session
    ) {
        String name = body.getOrDefault("name", "").trim();
        String email = body.getOrDefault("email", "")
            .trim().toLowerCase();
        String password = body.getOrDefault("password", "");

        if (name.isBlank() || email.isBlank()
                || password.length() < 8) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message",
                    "Name, email and a password of at least 8 characters are required."
                ));
        }

        if (users.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                    "message",
                    "Email is already registered."
                ));
        }

        User user = new User();
        user.name = name;
        user.email = email;
        user.passwordHash = encoder.encode(password);

        user = users.save(user);

        session.setAttribute("userId", user.id);
        session.setAttribute("userName", user.name);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(publicUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
        @RequestBody Map<String, String> body,
        HttpSession session
    ) {
        String email = body.getOrDefault("email", "")
            .trim().toLowerCase();
        String password = body.getOrDefault("password", "");

        User user = users.findByEmailIgnoreCase(email)
            .orElse(null);

        if (user == null
                || !encoder.matches(password, user.passwordHash)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "message",
                    "Invalid email or password."
                ));
        }

        session.setAttribute("userId", user.id);
        session.setAttribute("userName", user.name);

        return ResponseEntity.ok(publicUser(user));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Long userId = currentUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Please log in."));
        }

        return users.findById(userId)
            .<ResponseEntity<?>>map(user ->
                ResponseEntity.ok(publicUser(user)))
            .orElseGet(() ->
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not found.")));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
        @RequestBody Map<String, String> body,
        HttpSession session
    ) {
        Long userId = currentUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Please log in."));
        }

        User user = users.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "User not found."));
        }

        String name = body.getOrDefault("name", "").trim();
        String mobile = body.getOrDefault("mobile", "").trim();
        String city = body.getOrDefault("city", "").trim();

        if (name.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Name is required."));
        }

        if (name.length() > 255) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Name is too long."));
        }

        if (mobile.length() > 20) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Mobile number is too long."));
        }

        if (city.length() > 100) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "City name is too long."));
        }

        user.name = name;
        user.mobile = mobile.isBlank() ? null : mobile;
        user.city = city.isBlank() ? null : city;

        user = users.save(user);

        session.setAttribute("userName", user.name);

        return ResponseEntity.ok(publicUser(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();

        return ResponseEntity.ok(
            Map.of("message", "Logged out successfully.")
        );
    }
}

