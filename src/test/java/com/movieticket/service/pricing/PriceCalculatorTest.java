package com.movieticket.service.pricing;

import com.movieticket.domain.entity.*;
import com.movieticket.domain.enums.SeatTier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceCalculatorTest {

    private final PriceCalculator priceCalculator = new PriceCalculator();

    @Test
    void calculatesRegularPriceOnWeekday() {
        Show show = weekdayShow(new BigDecimal("200.00"), new BigDecimal("350.00"), new BigDecimal("1.20"));
        assertEquals(new BigDecimal("200.00"), priceCalculator.calculateUnitPrice(show, SeatTier.REGULAR));
        assertEquals(new BigDecimal("350.00"), priceCalculator.calculateUnitPrice(show, SeatTier.PREMIUM));
    }

    @Test
    void appliesWeekendMultiplierOnSaturday() {
        Show show = weekendShow(new BigDecimal("200.00"), new BigDecimal("350.00"), new BigDecimal("1.25"));
        assertEquals(new BigDecimal("250.00"), priceCalculator.calculateUnitPrice(show, SeatTier.REGULAR));
        assertEquals(new BigDecimal("437.50"), priceCalculator.calculateUnitPrice(show, SeatTier.PREMIUM));
    }

    private Show weekdayShow(BigDecimal regular, BigDecimal premium, BigDecimal multiplier) {
        return baseShow(regular, premium, multiplier, Instant.parse("2026-06-18T14:00:00Z"));
    }

    private Show weekendShow(BigDecimal regular, BigDecimal premium, BigDecimal multiplier) {
        return baseShow(regular, premium, multiplier, Instant.parse("2026-06-20T14:00:00Z"));
    }

    private Show baseShow(BigDecimal regular, BigDecimal premium, BigDecimal multiplier, Instant startTime) {
        City city = City.builder().name("Mumbai").timezone("Asia/Kolkata").build();
        Theater theater = Theater.builder().city(city).name("PVR").build();
        Screen screen = Screen.builder().theater(theater).name("Screen 1").totalRows(5).totalColumns(8).build();
        Movie movie = Movie.builder().title("Test").durationMinutes(120).build();

        return Show.builder()
                .movie(movie)
                .screen(screen)
                .startTime(startTime)
                .regularBasePrice(regular)
                .premiumBasePrice(premium)
                .weekendMultiplier(multiplier)
                .build();
    }
}
