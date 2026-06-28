package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.common.enums.MessageStatus;
import com.example.tmdt_bookingmakeup_app.dto.chat.ChatDto;
import com.example.tmdt_bookingmakeup_app.models.chat.ChatMessage;
import com.example.tmdt_bookingmakeup_app.models.chat.ChatRoom;
import com.example.tmdt_bookingmakeup_app.repositories.ChatRoomRepository;
import com.example.tmdt_bookingmakeup_app.services.ChatMessageService;
import com.example.tmdt_bookingmakeup_app.services.ChatPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatPresenceService presenceService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatDto chatDto) {
        ChatMessage saved = chatMessageService.save(chatDto);

        ///user/{recipientId}/queue/messages
        messagingTemplate.convertAndSendToUser(
                chatDto.recipientId(),
                "/queue/messages",
                saved
        );
    }

    @MessageMapping("/chat.delivered")
    public void markAsDelivered(@Payload UUID messageId) {
        chatMessageService.updateStatus(messageId, MessageStatus.DELIVERED);
    }

    @MessageMapping("/chat.read")
    public void markAsRead(@Payload ChatDto chatDto) {
        chatMessageService.markAsRead(chatDto.chatId(), UUID.fromString(chatDto.recipientId()));
        messagingTemplate.convertAndSendToUser(
                chatDto.senderId(),
                "/queue/messages",
                chatDto
        );
    }

    @MessageMapping("/chat.presence")
    public void requestPresence() {
        presenceService.broadcast();
    }
}
