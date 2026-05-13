package com.example.tmdt_bookingmakeup_app.repositories;

import com.example.tmdt_bookingmakeup_app.models.chat.ChatMessage;
import com.example.tmdt_bookingmakeup_app.models.chat.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    Page<ChatMessage> findByChatRoomOrderByTimestampAsc(ChatRoom chatRoom, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m SET m.status = 'READ' " +
            "WHERE m.chatRoom.id = :roomId AND m.recipient.id = :userId " +
            "AND m.status <> 'READ'")
    void updateAllToRead(String roomId, UUID userId);
}
