package com.occazcar.dto;

import lombok.Data;

@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String type;
    private Long relatedEntityId;
    private Boolean isRead;
    private String createdAt;
}