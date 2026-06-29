package com.example.tmdt_bookingmakeup_app.dto.chat;

public record ChatRoomResponseDto(
    String id,
    String recipientId,
    String recipientName,
    String recipientAvatar,
    String lastMessage,
    String lastMessageTime,
    int unreadCount
) {}