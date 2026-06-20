package com.movieticket;

import com.movieticket.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(JwtProperties.class)
public class MovieTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieTicketApplication.class, args);
    }
}
