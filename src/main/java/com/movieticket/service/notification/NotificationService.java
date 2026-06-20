package com.movieticket.service.notification;

import com.movieticket.domain.entity.Booking;
import com.movieticket.domain.entity.NotificationLog;
import com.movieticket.domain.entity.User;
import com.movieticket.domain.enums.NotificationType;
import com.movieticket.repository.BookingRepository;
import com.movieticket.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final BookingRepository bookingRepository;
    private final NotificationLogRepository notificationLogRepository;

    @Transactional
    public void deliverConfirmation(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found for notification: " + bookingId));

        User user = booking.getUser();
        String subject = "Booking confirmed: " + booking.getConfirmationCode();
        String body = "Your tickets for " + booking.getShow().getMovie().getTitle()
                + " are confirmed. Confirmation code: " + booking.getConfirmationCode();

        log.info("MOCK NOTIFICATION to {} — {}", user.getEmail(), subject);

        NotificationLog notification = NotificationLog.builder()
                .user(user)
                .booking(booking)
                .type(NotificationType.BOOKING_CONFIRMATION)
                .recipient(user.getEmail())
                .subject(subject)
                .body(body)
                .build();

        notificationLogRepository.save(notification);
    }
}
