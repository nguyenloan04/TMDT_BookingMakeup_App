package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.models.chat.ChatRoom;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import com.example.tmdt_bookingmakeup_app.repositories.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
}