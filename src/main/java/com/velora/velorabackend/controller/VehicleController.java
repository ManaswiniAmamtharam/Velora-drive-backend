
package com.velora.velorabackend.controller;

import com.velora.velorabackend.model.Vehicle;
import com.velora.velorabackend.repository.VehicleRepository;
import com.velora.velorabackend.repository.UserRepository;
import com.velora.velorabackend.model.User;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleRepository vehicles;
    private final UserRepository users;

    public VehicleController(
            VehicleRepository vehicles,
            UserRepository users) {
        this.vehicles = vehicles;
        this.users = users;
    }

    private Long currentUserId(HttpSession session) {
        Object id = session.getAttribute("userId");
        return id instanceof Long userId ? userId : null;
    }

    @GetMapping
    public List<Vehicle> getAll() {
        return vehicles.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        return vehicles.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/mine")
    public ResponseEntity<?> mine(HttpSession session) {
        Long userId = currentUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in."));
        }

        return ResponseEntity.ok(vehicles.findBySellerId(userId));
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody Vehicle vehicle,
            HttpSession session) {

        Long userId = currentUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message",
                            "Please log in to sell a vehicle."));
        }

        if (vehicle.brand == null || vehicle.brand.isBlank()
                || vehicle.model == null || vehicle.model.isBlank()
                || vehicle.type == null || vehicle.type.isBlank()
                || vehicle.price == null
                || vehicle.price.signum() <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Type, brand, model and a positive price are required."));
        }

        vehicle.id = null;
        vehicle.sellerId = userId;
        vehicle.createdAt = LocalDateTime.now();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vehicles.save(vehicle));
    }

    @PostMapping("/import-samples")
    public ResponseEntity<?> importSamples() {

        if (vehicles.count() > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message",
                            "Vehicles already exist. Import skipped to avoid duplicates."));
        }

        User seller = users.findAll().stream()
                .findFirst()
                .orElse(null);

        if (seller == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Register an account first, then import the sample vehicles."));
        }

        String[][] data = {
            {"SUV","Toyota","Innova Crysta","2021","1550000","Diesel","Automatic","42000","1","Hyderabad","White","/images/innova.jpg"},
            {"SUV","Hyundai","Creta","2022","1420000","Petrol","Manual","30000","1","Hyderabad","Grey","/images/creta.jpg"},
            {"Sedan","Honda","City","2020","950000","Petrol","Automatic","51000","2","Bangalore","Silver","/images/city.jpg"},
            {"Hatchback","Maruti","Baleno","2023","780000","Petrol","Manual","15000","1","Hyderabad","Blue","/images/baleno.jpg"},
            {"SUV","Mahindra","XUV700","2022","1850000","Diesel","Automatic","28000","1","Chennai","Black","/images/xuv700.jpg"},
            {"Bike","Royal Enfield","Classic 350","2021","170000","Petrol","Manual","18000","1","Hyderabad","Red","/images/classic350.jpg"},
            {"SUV","Kia","Seltos","2023","1490000","Petrol","Automatic","22000","1","Pune","White","/images/seltos.jpg"},
            {"Sedan","Skoda","Slavia","2022","1180000","Petrol","Manual","26000","1","Delhi","Grey","/images/slavia.jpg"},
            {"Hatchback","Tata","Altroz","2024","890000","CNG","Manual","8000","1","Hyderabad","Blue","/images/altroz.jpg"},
            {"SUV","MG","Hector","2021","1380000","Diesel","Automatic","41000","2","Mumbai","Black","/images/hector.jpg"},
            {"Bike","Yamaha","R15 V4","2023","185000","Petrol","Manual","9000","1","Hyderabad","Black","/images/r15v4.jpg"},
            {"Sedan","Volkswagen","Virtus","2024","1450000","Petrol","Automatic","12000","1","Bangalore","Red","/images/virtus.jpg"},
            {"SUV","Jeep","Compass","2022","1980000","Diesel","Automatic","35000","1","Hyderabad","Green","/images/compass.jpg"},
            {"Hatchback","Hyundai","i20","2021","760000","Petrol","Manual","34000","2","Chennai","Silver","/images/i20.jpg"},
            {"SUV","Tata","Nexon EV","2023","1650000","Electric","Automatic","14000","1","Hyderabad","White","/images/nexonev.jpg"},
            {"Bike","Bajaj","Pulsar NS200","2022","145000","Petrol","Manual","16000","1","Vizag","Blue","/images/ns200.jpg"},
            {"Sedan","Honda","Amaze","2023","870000","Petrol","CVT","18000","1","Hyderabad","White","/images/amaze.jpg"},
            {"SUV","Mahindra","Scorpio N","2024","2150000","Diesel","Automatic","9000","1","Delhi","Black","/images/scorpion.jpg"},
            {"Hatchback","Maruti","Swift","2022","650000","CNG","Manual","25000","1","Hyderabad","Red","/images/swift.jpg"},
            {"SUV","Toyota","Fortuner","2021","3250000","Diesel","Automatic","47000","1","Bangalore","Black","/images/fortuner.jpg"}
        };

        for (String[] row : data) {
            Vehicle v = new Vehicle();
            v.type = row[0];
            v.brand = row[1];
            v.model = row[2];
            v.year = Integer.valueOf(row[3]);
            v.price = new BigDecimal(row[4]);
            v.fuel = row[5];
            v.transmission = row[6];
            v.kilometers = Integer.valueOf(row[7]);
            v.owners = Integer.valueOf(row[8]);
            v.location = row[9];
            v.color = row[10];
            v.image = row[11];
            v.sellerId = seller.id;
            v.createdAt = LocalDateTime.now();

            vehicles.save(v);
        }

        return ResponseEntity.ok(Map.of(
                "message", "20 sample vehicles imported successfully.",
                "count", data.length
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            HttpSession session) {

        Long userId = currentUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in."));
        }

        Vehicle vehicle = vehicles.findById(id).orElse(null);

        if (vehicle == null) {
            return ResponseEntity.notFound().build();
        }

        if (vehicle.sellerId == null
                || !vehicle.sellerId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message",
                            "You can only delete your own listings."));
        }

        vehicles.delete(vehicle);
        return ResponseEntity.noContent().build();
    }
}
