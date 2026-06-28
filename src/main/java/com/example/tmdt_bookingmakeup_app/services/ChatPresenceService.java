package com.example.tmdt_bookingmakeup_app.services;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
@RequiredArgsConstructor
public class ChatPresenceService {
    private final SimpMessagingTemplate messagingTemplate;
    //Save current session user
    private final ConcurrentHashMap<String, String> sessionUserMap = new ConcurrentHashMap<>();

    //Used for multithreading
    //- Read thread in old array
    //- Write thread in new array
    //- Then swap two array for Reading
    private final CopyOnWriteArraySet<String> onlineUsers = new CopyOnWriteArraySet<>();

    public void userJoined(String sessionId, String userId) {
        sessionUserMap.put(sessionId, userId);
        onlineUsers.add(userId);
        broadcast();
    }

    public void userLeft(String sessionId) {
        String userId = sessionUserMap.remove(sessionId);
        if (userId != null && !sessionUserMap.containsValue(userId)) {
            onlineUsers.remove(userId);
            broadcast();
        }
    }

    public void broadcast() {
        messagingTemplate.convertAndSend("/topic/presence", onlineUsers);
    }
}