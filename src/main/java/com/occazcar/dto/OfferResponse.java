package com.occazcar.dto;

import lombok.Data;

@Data
public class OfferResponse {
    private Long id;
    private Long vehicleId;
    private String vehicleBrand;
    private String vehicleModel;
    private Long buyerId;
    private String buyerName;
    private String buyerPhone;
    private Double proposedPrice;
    private String message;
    private String status;
    private String createdAt;
}
