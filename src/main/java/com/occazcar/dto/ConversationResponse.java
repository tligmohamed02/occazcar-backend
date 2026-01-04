package com.occazcar.dto;

import lombok.Data;

@Data
public class ConversationResponse {
    private Long vehicleId;
    private String vehicleBrand;
    private String vehicleModel;
    private Long otherUserId;
    private String otherUserName;
    private String lastMessage;
    private String lastMessageTime;
    private Integer unreadCount;
}