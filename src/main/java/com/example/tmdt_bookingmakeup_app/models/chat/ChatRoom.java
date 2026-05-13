package com.example.tmdt_bookingmakeup_app.models.chat;

import com.example.tmdt_bookingmakeup_app.models.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatRoom {
    //Reduce 1 query when finding a room
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")

    private User recipient;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
    private String lastSenderId;
}
