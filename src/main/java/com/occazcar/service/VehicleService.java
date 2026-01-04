package com.occazcar.service;

import com.occazcar.dto.VehicleRequest;
import com.occazcar.dto.VehicleResponse;
import com.occazcar.model.User;
import com.occazcar.model.Vehicle;
import com.occazcar.repository.VehicleRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    public VehicleService(VehicleRepository vehicleRepository,
                          AuthService authService,
                          NotificationService notificationService) {
        this.vehicleRepository = vehicleRepository;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request, Long sellerId) {
        User seller = authService.getUserById(sellerId);

        Vehicle vehicle = new Vehicle();
        vehicle.setSeller(seller);
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setMileage(request.getMileage());
        vehicle.setPrice(request.getPrice());
        vehicle.setDescription(request.getDescription());
        vehicle.setColor(request.getColor());
        vehicle.setDoors(request.getDoors());
        vehicle.setLatitude(request.getLatitude());
        vehicle.setLongitude(request.getLongitude());
        vehicle.setAddress(request.getAddress());

        if (request.getFuelType() != null) {
            vehicle.setFuelType(Vehicle.FuelType.valueOf(request.getFuelType().toUpperCase()));
        }
        if (request.getTransmission() != null) {
            vehicle.setTransmission(Vehicle.Transmission.valueOf(request.getTransmission().toUpperCase()));
        }

        vehicle = vehicleRepository.save(vehicle);

        // Notifier tous les acheteurs du nouveau véhicule
        notificationService.notifyNewVehicle(vehicle);

        return mapToResponse(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> searchVehicles(String brand, String model,
                                                Double minPrice, Double maxPrice,
                                                Integer minYear, Integer maxYear) {
        Specification<Vehicle> spec = (root, query, cb) -> cb.conjunction();

        if (brand != null && !brand.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("brand")), "%" + brand.toLowerCase() + "%"));
        }
        if (model != null && !model.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("model")), "%" + model.toLowerCase() + "%"));
        }
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }
        if (minYear != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("year"), minYear));
        }
        if (maxYear != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("year"), maxYear));
        }

        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("status"), Vehicle.VehicleStatus.DISPONIBLE));

        return vehicleRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
        return mapToResponse(vehicle);
    }

    public List<VehicleResponse> getSellerVehicles(Long sellerId) {
        return vehicleRepository.findBySellerIdAndStatus(sellerId, Vehicle.VehicleStatus.DISPONIBLE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleRequest request, Long sellerId) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));

        if (!vehicle.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Non autorisé");
        }

        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setMileage(request.getMileage());
        vehicle.setPrice(request.getPrice());
        vehicle.setDescription(request.getDescription());

        vehicle = vehicleRepository.save(vehicle);
        return mapToResponse(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long id, Long sellerId) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));

        if (!vehicle.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Non autorisé");
        }

        vehicleRepository.delete(vehicle);
    }

    @Transactional
    public VehicleResponse uploadPhotos(Long vehicleId, org.springframework.web.multipart.MultipartFile[] files, Long userId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));

        if (!vehicle.getSeller().getId().equals(userId)) {
            throw new RuntimeException("Non autorisé");
        }

        try {
            String uploadDir = "uploads/vehicles/" + vehicleId;
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);

            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            for (org.springframework.web.multipart.MultipartFile file : files) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                java.nio.file.Path filePath = uploadPath.resolve(fileName);
                java.nio.file.Files.copy(file.getInputStream(), filePath);

                String photoUrl = "/uploads/vehicles/" + vehicleId + "/" + fileName;
                vehicle.getPhotos().add(photoUrl);
            }

            vehicle = vehicleRepository.save(vehicle);
            return mapToResponse(vehicle);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'upload: " + e.getMessage());
        }
    }

    @Transactional
    public VehicleResponse deletePhoto(Long vehicleId, String photoUrl, Long userId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));

        if (!vehicle.getSeller().getId().equals(userId)) {
            throw new RuntimeException("Non autorisé");
        }

        try {
            vehicle.getPhotos().remove(photoUrl);
            vehicle = vehicleRepository.save(vehicle);

            // Supprimer le fichier physique
            String filePath = "." + photoUrl;
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(filePath));

            return mapToResponse(vehicle);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());
        response.setSellerId(vehicle.getSeller().getId());
        response.setSellerName(vehicle.getSeller().getFullName());
        response.setSellerPhone(vehicle.getSeller().getPhone());
        response.setBrand(vehicle.getBrand());
        response.setModel(vehicle.getModel());
        response.setYear(vehicle.getYear());
        response.setMileage(vehicle.getMileage());
        response.setPrice(vehicle.getPrice());
        response.setDescription(vehicle.getDescription());
        response.setColor(vehicle.getColor());
        response.setDoors(vehicle.getDoors());
        response.setPhotos(vehicle.getPhotos());
        response.setLatitude(vehicle.getLatitude());
        response.setLongitude(vehicle.getLongitude());
        response.setAddress(vehicle.getAddress());
        response.setStatus(vehicle.getStatus().name());
        response.setCreatedAt(vehicle.getCreatedAt().toString());

        if (vehicle.getFuelType() != null) {
            response.setFuelType(vehicle.getFuelType().name());
        }
        if (vehicle.getTransmission() != null) {
            response.setTransmission(vehicle.getTransmission().name());
        }

        return response;
    }
}