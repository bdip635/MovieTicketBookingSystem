package com.movieticket.service.pricing;

import com.movieticket.domain.entity.Show;
import com.movieticket.domain.enums.SeatTier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;

@Component
public class PriceCalculator {

    public BigDecimal calculateUnitPrice(Show show, SeatTier tier) {
        BigDecimal basePrice = tier == SeatTier.PREMIUM
                ? show.getPremiumBasePrice()
                : show.getRegularBasePrice();

        if (isWeekend(show)) {
            basePrice = basePrice.multiply(show.getWeekendMultiplier());
        }

        return basePrice.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isWeekend(Show show) {
        ZoneId zoneId = ZoneId.of(show.getScreen().getTheater().getCity().getTimezone());
        DayOfWeek day = show.getStartTime().atZone(zoneId).getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
