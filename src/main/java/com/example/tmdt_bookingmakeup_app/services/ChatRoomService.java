package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.dto.chat.ChatRoomResponseDto;
import com.example.tmdt_bookingmakeup_app.models.chat.ChatRoom;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import com.example.tmdt_bookingmakeup_app.repositories.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    //Use String as Id to make sure both user always enter the same room
    public Optional<ChatRoom> getChatRoom(User sender, User recipient, boolean createIfNotExist) {
        String sId = sender.getId().toString();
        String rId = recipient.getId().toString();
        String roomId = sId.compareTo(rId) < 0 ? sId + "_" + rId : rId + "_" + sId;

        return chatRoomRepository.findById(roomId)
                .or(() -> {
                    if (createIfNotExist) {
                        ChatRoom newRoom = ChatRoom.builder()
                                .id(roomId)
                                .sender(sender)
                                .recipient(recipient)
                                .build();
                        return Optional.of(chatRoomRepository.save(newRoom));
                    }
                    return Optional.empty();
                });
    }

    public void resetUnreadCount(String roomId) {
        chatRoomRepository.resetUnreadCount(roomId);
    }

    public List<ChatRoomResponseDto> getUserChatRooms(UUID userId) {
        List<ChatRoom> rooms = chatRoomRepository.findByUserId(userId);

        return rooms.stream().map(room -> {
            User partner = room.getSender().getId().equals(userId) ? room.getRecipient() : room.getSender();

            int unread = 0;
            if (room.getUnreadCount() > 0) {
                if (!userId.toString().equals(room.getLastSenderId())) {
                    unread = room.getUnreadCount();
                }
            }

            String timeStr = "";
            if (room.getLastMessageTime() != null) {
                timeStr = room.getLastMessageTime().format(DateTimeFormatter.ofPattern("HH:mm dd/MM"));
            }

            String name = partner.getDisplayName() != null ? partner.getDisplayName() : partner.getUsername();

            return new ChatRoomResponseDto(
                    room.getId(),
                    partner.getId().toString(),
                    name,
                    partner.getAvatarUrl(),
                    room.getLastMessage(),
                    timeStr,
                    unread
            );
        }).collect(Collectors.toList());
    }
}