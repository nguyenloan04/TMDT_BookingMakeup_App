package com.example.tmdt_bookingmakeup_app.dto.response.notification;

import com.example.tmdt_bookingmakeup_app.common.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        UUID recipientId,
        UUID bookingId,
        NotificationType type,
        String title,
        String message,
        boolean read,
        LocalDateTime createdAt
) {}
