package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.dto.response.notification.NotificationDto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NotificationRedisCache {

    private static final String LIST_KEY_PREFIX = "notifications:";
    private static final String UNREAD_KEY_PREFIX = "notifications:unread:";

    private final StringRedisTemplate redisTemplate;
    private final Gson gson;

    private static final DateTimeFormatter LDT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Value("${app.notifications.redis-ttl-hours:24}")
    private long ttlHours;

    @Value("${app.notifications.recent-limit:20}")
    private int recentLimit;

    @Autowired
    public NotificationRedisCache(@Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc, ctx) ->
                                ctx.serialize(src.format(LDT_FORMATTER)))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, typeOfT, ctx) ->
                                LocalDateTime.parse(json.getAsString(), LDT_FORMATTER))
                .create();
    }

    private boolean isAvailable() {
        return redisTemplate != null;
    }

    public void pushNotification(UUID recipientId, NotificationDto dto) {
        if (!isAvailable()) return;
        try {
            String listKey = listKey(recipientId);
            redisTemplate.opsForList().leftPush(listKey, gson.toJson(dto));
            redisTemplate.opsForList().trim(listKey, 0, recentLimit - 1);
            redisTemplate.expire(listKey, ttlHours, TimeUnit.HOURS);

            if (!dto.read()) {
                String unreadKey = unreadKey(recipientId);
                redisTemplate.opsForValue().increment(unreadKey);
                redisTemplate.expire(unreadKey, ttlHours, TimeUnit.HOURS);
            }
        } catch (Exception e) {
            log.warn("Redis push notification failed for user {}: {}", recipientId, e.getMessage());
        }
    }

    public List<NotificationDto> getRecentNotifications(UUID recipientId) {
        if (!isAvailable()) return List.of();
        try {
            List<String> raw = redisTemplate.opsForList().range(listKey(recipientId), 0, recentLimit - 1);
            if (raw == null || raw.isEmpty()) {
                return List.of();
            }
            List<NotificationDto> result = new ArrayList<>();
            for (String json : raw) {
                result.add(gson.fromJson(json, NotificationDto.class));
            }
            return result;
        } catch (Exception e) {
            log.warn("Redis get notifications failed for user {}: {}", recipientId, e.getMessage());
            return List.of();
        }
    }

    public void cacheRecentNotifications(UUID recipientId, List<NotificationDto> notifications) {
        if (!isAvailable()) return;
        try {
            String listKey = listKey(recipientId);
            redisTemplate.delete(listKey);
            if (notifications.isEmpty()) {
                return;
            }
            List<String> serialized = notifications.stream()
                    .map(gson::toJson)
                    .toList();
            redisTemplate.opsForList().rightPushAll(listKey, serialized);
            redisTemplate.opsForList().trim(listKey, 0, recentLimit - 1);
            redisTemplate.expire(listKey, ttlHours, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis cache refresh failed for user {}: {}", recipientId, e.getMessage());
        }
    }

    public long getUnreadCount(UUID recipientId) {
        if (!isAvailable()) return -1;
        try {
            String value = redisTemplate.opsForValue().get(unreadKey(recipientId));
            if (value == null) {
                return -1;
            }
            return Long.parseLong(value);
        } catch (Exception e) {
            log.warn("Redis unread count failed for user {}: {}", recipientId, e.getMessage());
            return -1;
        }
    }

    public void setUnreadCount(UUID recipientId, long count) {
        if (!isAvailable()) return;
        try {
            redisTemplate.opsForValue().set(unreadKey(recipientId), String.valueOf(count));
            redisTemplate.expire(unreadKey(recipientId), ttlHours, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis set unread count failed for user {}: {}", recipientId, e.getMessage());
        }
    }

    public void markAsRead(UUID recipientId, UUID notificationId) {
        if (!isAvailable()) return;
        try {
            List<NotificationDto> cached = getRecentNotifications(recipientId);
            if (cached.isEmpty()) {
                return;
            }
            boolean updated = false;
            List<NotificationDto> refreshed = new ArrayList<>();
            for (NotificationDto dto : cached) {
                if (dto.id().equals(notificationId) && !dto.read()) {
                    refreshed.add(new NotificationDto(
                            dto.id(), dto.recipientId(), dto.bookingId(),
                            dto.type(), dto.title(), dto.message(), true, dto.createdAt()
                    ));
                    updated = true;
                } else {
                    refreshed.add(dto);
                }
            }
            if (updated) {
                cacheRecentNotifications(recipientId, refreshed);
                String unreadKey = unreadKey(recipientId);
                Long current = redisTemplate.opsForValue().decrement(unreadKey);
                if (current != null && current < 0) {
                    redisTemplate.opsForValue().set(unreadKey, "0");
                }
            }
        } catch (Exception e) {
            log.warn("Redis mark as read failed for user {}: {}", recipientId, e.getMessage());
        }
    }

    public void clearUnreadCount(UUID recipientId) {
        if (!isAvailable()) return;
        try {
            redisTemplate.opsForValue().set(unreadKey(recipientId), "0");
            redisTemplate.expire(unreadKey(recipientId), ttlHours, TimeUnit.HOURS);

            List<NotificationDto> cached = getRecentNotifications(recipientId);
            if (!cached.isEmpty()) {
                List<NotificationDto> allRead = cached.stream()
                        .map(dto -> new NotificationDto(
                                dto.id(), dto.recipientId(), dto.bookingId(),
                                dto.type(), dto.title(), dto.message(), true, dto.createdAt()
                        ))
                        .toList();
                cacheRecentNotifications(recipientId, allRead);
            }
        } catch (Exception e) {
            log.warn("Redis clear unread failed for user {}: {}", recipientId, e.getMessage());
        }
    }

    private String listKey(UUID recipientId) {
        return LIST_KEY_PREFIX + recipientId;
    }

    private String unreadKey(UUID recipientId) {
        return UNREAD_KEY_PREFIX + recipientId;
    }
}
