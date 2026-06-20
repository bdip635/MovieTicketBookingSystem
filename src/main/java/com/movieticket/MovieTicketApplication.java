package com.movieticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MovieTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieTicketApplication.class, args);
    }
}
