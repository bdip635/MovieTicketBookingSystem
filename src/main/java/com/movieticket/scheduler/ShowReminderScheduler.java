package com.movieticket.scheduler;

import com.movieticket.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShowReminderScheduler {

    private final NotificationService notificationService;

    @Scheduled(fixedRateString = "${app.notification.reminder-check-interval-ms:300000}")
    public void sendShowReminders() {
        int sent = notificationService.sendDueShowReminders();
        if (sent > 0) {
            log.info("Sent {} show reminder(s)", sent);
        }
    }
}
