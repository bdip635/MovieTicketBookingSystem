package com.movieticket.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.booking")
public class BookingProperties {

    private int holdDurationMinutes = 10;
}
