package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.common.enums.BookingStatus;
import com.example.tmdt_bookingmakeup_app.common.enums.NotificationType;
import com.example.tmdt_bookingmakeup_app.dto.response.notification.NotificationDto;
import com.example.tmdt_bookingmakeup_app.models.booking.Booking;
import com.example.tmdt_bookingmakeup_app.models.notification.Notification;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import com.example.tmdt_bookingmakeup_app.repositories.BookingRepository;
import com.example.tmdt_bookingmakeup_app.repositories.NotificationRepository;
import com.example.tmdt_bookingmakeup_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private static final int RECENT_LIMIT = 20;

    private final NotificationRepository notificationRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationRedisCache redisCache;

    public void notifyBookingStatusChange(Booking booking, BookingStatus newStatus, UUID actorId) {
        UUID ownerId = booking.getService().getOwner().getUserId();
        UUID customerId = booking.getCustomer().getId();
        String serviceName = booking.getService().getName();
        String customerName = booking.getCustomer().getDisplayName() != null
                ? booking.getCustomer().getDisplayName()
                : booking.getCustomer().getUsername();

        switch (newStatus) {
            case PENDING -> createNotification(
                    ownerId, booking, NotificationType.BOOKING_PENDING,
                    "Booking mới",
                    customerName + " vừa đặt lịch dịch vụ \"" + serviceName + "\""
            );
            case CONFIRMED -> createNotification(
                    customerId, booking, NotificationType.BOOKING_CONFIRMED,
                    "Booking đã được xác nhận",
                    "Dịch vụ \"" + serviceName + "\" đã được xác nhận. Vui lòng thanh toán cọc."
            );
            case CANCELLED -> {
                if (actorId != null && actorId.equals(customerId)) {
                    createNotification(
                            ownerId, booking, NotificationType.BOOKING_CANCELLED,
                            "Khách hủy booking",
                            customerName + " đã hủy lịch dịch vụ \"" + serviceName + "\""
                    );
                } else {
                    createNotification(
                            customerId, booking, NotificationType.BOOKING_REJECTED,
                            "Booking bị từ chối",
                            "Dịch vụ \"" + serviceName + "\" đã bị từ chối bởi chủ tiệm"
                    );
                }
            }
            case PAID -> {
                createNotification(
                        customerId, booking, NotificationType.BOOKING_PAID,
                        "Thanh toán thành công",
                        "Bạn đã thanh toán cọc cho dịch vụ \"" + serviceName + "\" thành công"
                );
                createNotification(
                        ownerId, booking, NotificationType.BOOKING_PAID,
                        "Khách đã thanh toán",
                        customerName + " đã thanh toán cọc cho dịch vụ \"" + serviceName + "\""
                );
            }
            case COMPLETED -> createNotification(
                    customerId, booking, NotificationType.BOOKING_COMPLETED,
                    "Dịch vụ hoàn thành",
                    "Dịch vụ \"" + serviceName + "\" đã hoàn thành. Hãy để lại đánh giá nhé!"
            );
            default -> { /* no notification */ }
        }
    }

    public void notifyPaymentSuccess(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        notifyBookingStatusChange(booking, BookingStatus.PAID, null);
    }

    private void createNotification(UUID recipientId, Booking booking, NotificationType type,
                                    String title, String message) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found: " + recipientId));

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setBooking(booking);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        NotificationDto dto = mapToDto(saved);
        redisCache.pushNotification(recipientId, dto);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getRecentNotifications(UUID recipientId) {
        List<NotificationDto> cached = redisCache.getRecentNotifications(recipientId);
        if (!cached.isEmpty()) {
            return cached;
        }

        List<NotificationDto> fromDb = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipientId, PageRequest.of(0, RECENT_LIMIT))
                .stream()
                .map(this::mapToDto)
                .toList();

        if (!fromDb.isEmpty()) {
            redisCache.cacheRecentNotifications(recipientId, fromDb);
            long unreadCount = notificationRepository.countByRecipientIdAndReadFalse(recipientId);
            redisCache.setUnreadCount(recipientId, unreadCount);
        }

        return fromDb;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getAllNotifications(UUID recipientId) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipientId, PageRequest.of(0, 100))
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID recipientId) {
        long cached = redisCache.getUnreadCount(recipientId);
        if (cached >= 0) {
            return cached;
        }
        long fromDb = notificationRepository.countByRecipientIdAndReadFalse(recipientId);
        redisCache.setUnreadCount(recipientId, fromDb);
        return fromDb;
    }

    public void markAsRead(UUID recipientId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getRecipient().getId().equals(recipientId)) {
            throw new RuntimeException("Access Denied");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
            redisCache.markAsRead(recipientId, notificationId);
        }
    }

    public void markAllAsRead(UUID recipientId) {
        notificationRepository.markAllAsRead(recipientId);
        redisCache.clearUnreadCount(recipientId);
    }

    public void notifyWithdrawStatus(com.example.tmdt_bookingmakeup_app.models.payment.Withdraw withdraw, com.example.tmdt_bookingmakeup_app.common.enums.WithdrawStatus status) {
        UUID ownerId = withdraw.getOwner().getUserId();
        String formattedAmount = java.text.NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(withdraw.getAmount());

        if (status == com.example.tmdt_bookingmakeup_app.common.enums.WithdrawStatus.APPROVED) {
            createNotification(
                    ownerId, null, NotificationType.WITHDRAW_APPROVED,
                    "Rút tiền thành công.",
                    "Yêu cầu rút " + formattedAmount + " của bạn đã được Admin duyệt và chuyển khoản thành công!"
            );
        } else if (status == com.example.tmdt_bookingmakeup_app.common.enums.WithdrawStatus.REJECTED) {
            createNotification(
                    ownerId, null, NotificationType.WITHDRAW_REJECTED,
                    "Rút tiền thất bại.",
                    "Yêu cầu rút " + formattedAmount + " bị từ chối. Lý do: " + withdraw.getNote() + ". Số tiền đã được hoàn lại vào ví."
            );
        }
    }

    private NotificationDto mapToDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getRecipient().getId(),
                notification.getBooking() != null ? notification.getBooking().getId() : null,
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
