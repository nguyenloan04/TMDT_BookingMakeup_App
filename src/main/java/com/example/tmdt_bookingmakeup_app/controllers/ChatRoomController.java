package com.example.tmdt_bookingmakeup_app.controllers;

import com.example.tmdt_bookingmakeup_app.dto.chat.ChatRoomResponseDto;
import com.example.tmdt_bookingmakeup_app.services.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<ChatRoomResponseDto>> getUserRooms(@PathVariable UUID userId) {
        return ResponseEntity.ok(chatRoomService.getUserChatRooms(userId));
    }
}