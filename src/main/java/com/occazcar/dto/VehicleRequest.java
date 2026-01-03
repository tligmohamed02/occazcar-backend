package com.occazcar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VehicleRequest {
    @NotBlank
    private String brand;

    @NotBlank
    private String model;

    @NotNull
    @Min(1900)
    private Integer year;

    @NotNull
    @Min(0)
    private Integer mileage;

    @NotNull
    private Double price;

    private String description;
    private String fuelType;
    private String transmission;
    private String color;
    private Integer doors;
    private Double latitude;
    private Double longitude;
    private String address;
}