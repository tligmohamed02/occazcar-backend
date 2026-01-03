package com.occazcar.dto;

import lombok.Data;

@Data
public class AlertRequest {
    private String brand;
    private String model;
    private Double minPrice;
    private Double maxPrice;
    private Integer minYear;
    private Integer maxYear;
    private Integer maxMileage;
    private String fuelType;
}
