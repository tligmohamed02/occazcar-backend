package com.occazcar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer mileage;

    @Column(nullable = false)
    private Double price;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    private Transmission transmission;

    private String color;

    private Integer doors;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "vehicle_photos", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "photo_url")
    private List<String> photos = new ArrayList<>();

    private Double latitude;

    private Double longitude;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status = VehicleStatus.DISPONIBLE;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum FuelType {
        ESSENCE, DIESEL, ELECTRIQUE, HYBRIDE, GPL
    }

    public enum Transmission {
        MANUELLE, AUTOMATIQUE
    }

    public enum VehicleStatus {
        DISPONIBLE, VENDU, RESERVE
    }
}