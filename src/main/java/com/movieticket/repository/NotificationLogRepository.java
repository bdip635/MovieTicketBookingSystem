package com.movieticket.repository;

import com.movieticket.domain.entity.NotificationLog;
import com.movieticket.domain.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    boolean existsByBooking_IdAndType(UUID bookingId, NotificationType type);
}
