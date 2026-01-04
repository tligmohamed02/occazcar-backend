package com.occazcar.controller;

import com.occazcar.dto.VehicleRequest;
import com.occazcar.dto.VehicleResponse;
import com.occazcar.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<?> createVehicle(@Valid @RequestBody VehicleRequest request,
                                           Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            VehicleResponse response = vehicleService.createVehicle(request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<VehicleResponse>> searchVehicles(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear) {

        List<VehicleResponse> vehicles = vehicleService.searchVehicles(
                brand, model, minPrice, maxPrice, minYear, maxYear);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVehicle(@PathVariable Long id) {
        try {
            VehicleResponse response = vehicleService.getVehicleById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my-vehicles")
    public ResponseEntity<List<VehicleResponse>> getMyVehicles(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<VehicleResponse> vehicles = vehicleService.getSellerVehicles(userId);
        return ResponseEntity.ok(vehicles);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id,
                                           @Valid @RequestBody VehicleRequest request,
                                           Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            VehicleResponse response = vehicleService.updateVehicle(id, request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id,
                                           Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            vehicleService.deleteVehicle(id, userId);
            return ResponseEntity.ok("Véhicule supprimé");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Upload de photos
    @PostMapping("/{id}/photos")
    public ResponseEntity<?> uploadPhotos(@PathVariable Long id,
                                          @RequestParam("files") MultipartFile[] files,
                                          Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            VehicleResponse response = vehicleService.uploadPhotos(id, files, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Suppression d'une photo
    @DeleteMapping("/{id}/photos")
    public ResponseEntity<?> deletePhoto(@PathVariable Long id,
                                         @RequestParam String photoUrl,
                                         Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            VehicleResponse response = vehicleService.deletePhoto(id, photoUrl, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}