package com.occazcar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OfferRequest {
    @NotNull
    private Long vehicleId;

    @NotNull
    private Double proposedPrice;

    private String message;
}