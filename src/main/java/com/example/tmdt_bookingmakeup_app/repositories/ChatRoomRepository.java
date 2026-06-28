package com.example.tmdt_bookingmakeup_app.repositories;

import com.example.tmdt_bookingmakeup_app.models.chat.ChatRoom;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    Optional<ChatRoom> findBySenderAndRecipient(User sender, User recipient);

    @Query("SELECT r FROM ChatRoom r WHERE r.sender.id = :userId OR r.recipient.id = :userId ORDER BY r.lastMessageTime DESC")
    List<ChatRoom> findByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE ChatRoom c SET c.unreadCount = 0 WHERE c.id = :roomId")
    void resetUnreadCount(String roomId);
}
