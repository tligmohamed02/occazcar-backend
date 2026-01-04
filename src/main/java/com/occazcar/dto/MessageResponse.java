package com.occazcar.dto;

import lombok.Data;

@Data
public class MessageResponse {
    private Long id;
    private Long vehicleId;
    private String vehicleBrand;
    private String vehicleModel;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String content;
    private Boolean isRead;
    private String createdAt;
}
