package com.occazcar.dto;

import lombok.Data;

@Data
public class VehicleResponse {
    private Long id;
    private Long sellerId;
    private String sellerName;
    private String sellerPhone;
    private String brand;
    private String model;
    private Integer year;
    private Integer mileage;
    private Double price;
    private String description;
    private String fuelType;
    private String transmission;
    private String color;
    private Integer doors;
    private java.util.List<String> photos;
    private Double latitude;
    private Double longitude;
    private String address;
    private String status;
    private String createdAt;
}

