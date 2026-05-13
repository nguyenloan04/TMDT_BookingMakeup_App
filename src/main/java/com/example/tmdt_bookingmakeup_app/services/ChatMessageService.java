package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.common.enums.MessageStatus;
import com.example.tmdt_bookingmakeup_app.dto.chat.ChatDto;
import com.example.tmdt_bookingmakeup_app.models.chat.ChatMessage;
import com.example.tmdt_bookingmakeup_app.models.chat.ChatRoom;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import com.example.tmdt_bookingmakeup_app.repositories.ChatMessageRepository;
import com.example.tmdt_bookingmakeup_app.repositories.ChatRoomRepository;
import com.example.tmdt_bookingmakeup_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository repository;
    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public ChatMessage save(ChatDto chatDto) {
        User sender = userRepository.findById(UUID.fromString(chatDto.senderId()))
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User recipient = userRepository.findById(UUID.fromString(chatDto.recipientId()))
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        ChatRoom room = chatRoomService
                .getChatRoom(sender, recipient, true)
                .orElseThrow(() -> new RuntimeException("Chat room error"));

        ChatMessage message = new ChatMessage();
        message.setChatRoom(room);
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(chatDto.content());
        message.setStatus(MessageStatus.SENT);
        message.setTimestamp(LocalDateTime.now());
        //Update unread message
        room.setUnreadCount(room.getUnreadCount() + 1);
        room.setLastMessage(message.getContent());
        room.setLastMessageTime(message.getTimestamp());
        chatRoomRepository.save(room);
        return repository.save(message);
    }

    public Page<ChatMessage> findChatMessages(UUID sId, UUID rId, Pageable pageable) {
        User sender = userRepository.findById(sId).orElseThrow();
        User recipient = userRepository.findById(rId).orElseThrow();

        return chatRoomService.getChatRoom(sender, recipient, false)
                .map(room -> repository.findByChatRoomOrderByTimestampAsc(room, pageable))
                .orElse(Page.empty());
    }

    @Transactional
    public void updateStatus(UUID sId, MessageStatus status) {
        ChatMessage message = repository.findById(sId).orElseThrow();
        message.setStatus(status);
        repository.save(message);
    }

    @Transactional
    public void markAsRead(String roomId, UUID userId) {
        repository.updateAllToRead(roomId, userId);
        chatRoomService.resetUnreadCount(roomId);
    }
}
