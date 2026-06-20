package com.movieticket.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.notification")
public class NotificationProperties {

    private int reminderHoursBefore = 2;
    private int reminderWindowMinutes = 15;
    private long reminderCheckIntervalMs = 300_000L;
}
