package com.movieticket.service.notification;

import com.movieticket.config.NotificationProperties;
import com.movieticket.domain.entity.Booking;
import com.movieticket.domain.entity.NotificationLog;
import com.movieticket.domain.entity.User;
import com.movieticket.domain.enums.BookingStatus;
import com.movieticket.domain.enums.NotificationType;
import com.movieticket.repository.BookingRepository;
import com.movieticket.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final BookingRepository bookingRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final NotificationProperties notificationProperties;

    @Transactional
    public void deliverConfirmation(UUID bookingId) {
        Booking booking = loadBooking(bookingId);
        User user = booking.getUser();
        String subject = "Booking confirmed: " + booking.getConfirmationCode();
        String body = "Your tickets for " + booking.getShow().getMovie().getTitle()
                + " are confirmed. Confirmation code: " + booking.getConfirmationCode();

        saveNotification(user, booking, NotificationType.BOOKING_CONFIRMATION, subject, body);
        log.info("MOCK NOTIFICATION to {} — {}", user.getEmail(), subject);
    }

    @Transactional
    public void deliverCancellation(UUID bookingId) {
        Booking booking = loadBooking(bookingId);
        User user = booking.getUser();
        String subject = "Booking cancelled: " + booking.getConfirmationCode();
        String body = "Your booking for " + booking.getShow().getMovie().getTitle()
                + " has been cancelled.";

        if (booking.getRefund() != null) {
            body += " Refund amount: " + booking.getRefund().getRefundAmount();
        }

        saveNotification(user, booking, NotificationType.BOOKING_CANCELLATION, subject, body);
        log.info("MOCK NOTIFICATION to {} — {}", user.getEmail(), subject);
    }

    @Transactional
    public void deliverShowReminder(UUID bookingId) {
        Booking booking = loadBooking(bookingId);
        User user = booking.getUser();
        String subject = "Show reminder: " + booking.getShow().getMovie().getTitle();
        String body = "Reminder — your show " + booking.getShow().getMovie().getTitle()
                + " starts at " + booking.getShow().getStartTime()
                + ". Confirmation code: " + booking.getConfirmationCode();

        saveNotification(user, booking, NotificationType.SHOW_REMINDER, subject, body);
        log.info("MOCK NOTIFICATION to {} — {}", user.getEmail(), subject);
    }

    @Transactional
    public int sendDueShowReminders() {
        Instant now = Instant.now();
        Instant windowCenter = now.plus(notificationProperties.getReminderHoursBefore(), ChronoUnit.HOURS);
        Instant windowStart = windowCenter.minus(notificationProperties.getReminderWindowMinutes(), ChronoUnit.MINUTES);
        Instant windowEnd = windowCenter.plus(notificationProperties.getReminderWindowMinutes(), ChronoUnit.MINUTES);

        List<Booking> bookings = bookingRepository.findByStatusAndShowStartTimeBetween(
                BookingStatus.CONFIRMED, windowStart, windowEnd);

        int sent = 0;
        for (Booking booking : bookings) {
            if (!notificationLogRepository.existsByBooking_IdAndType(
                    booking.getId(), NotificationType.SHOW_REMINDER)) {
                deliverShowReminder(booking.getId());
                sent++;
            }
        }
        return sent;
    }

    private Booking loadBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found for notification: " + bookingId));
    }

    private void saveNotification(
            User user,
            Booking booking,
            NotificationType type,
            String subject,
            String body) {

        NotificationLog notification = NotificationLog.builder()
                .user(user)
                .booking(booking)
                .type(type)
                .recipient(user.getEmail())
                .subject(subject)
                .body(body)
                .build();

        notificationLogRepository.save(notification);
    }
}
