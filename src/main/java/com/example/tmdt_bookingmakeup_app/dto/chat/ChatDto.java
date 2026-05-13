package com.example.tmdt_bookingmakeup_app.dto.chat;

import java.time.LocalDateTime;

public record ChatDto(
        String chatId, //UUID
        String senderId,    //UUID
        String recipientId, //UUID
        String content,
        LocalDateTime timestamp
) {

}
