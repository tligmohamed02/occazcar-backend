package com.occazcar.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MessageRequest {
    @NotNull
    private Long vehicleId;

    @NotNull
    private Long receiverId;

    @NotBlank
    @Size(max = 2000)
    private String content;
}
