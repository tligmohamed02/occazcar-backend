package com.occazcar.dto;

import lombok.Data;

@Data
public class AlertResponse {
    private Long id;
    private String brand;
    private String model;
    private Double minPrice;
    private Double maxPrice;
    private Integer minYear;
    private Integer maxYear;
    private Integer maxMileage;
    private String fuelType;
    private Boolean active;
    private String createdAt;
}
