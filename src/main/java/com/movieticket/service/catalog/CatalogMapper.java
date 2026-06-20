package com.movieticket.service.catalog;

import com.movieticket.domain.entity.*;
import com.movieticket.web.dto.catalog.*;

import java.util.List;

final class CatalogMapper {

    private CatalogMapper() {
    }

    static CityResponse toCityResponse(City city) {
        return new CityResponse(city.getId(), city.getName(), city.getTimezone(), city.getCreatedAt());
    }

    static TheaterResponse toTheaterResponse(Theater theater) {
        return new TheaterResponse(
                theater.getId(),
                theater.getCity().getId(),
                theater.getCity().getName(),
                theater.getName(),
                theater.getAddress(),
                theater.getCreatedAt()
        );
    }

    static ScreenResponse toScreenResponse(Screen screen) {
        return new ScreenResponse(
                screen.getId(),
                screen.getTheater().getId(),
                screen.getTheater().getName(),
                screen.getName(),
                screen.getTotalRows(),
                screen.getTotalColumns(),
                screen.getCreatedAt()
        );
    }

    static SeatResponse toSeatResponse(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getScreen().getId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                seat.getTier(),
                seat.getCreatedAt()
        );
    }

    static MovieResponse toMovieResponse(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getDurationMinutes(),
                movie.getLanguage(),
                movie.getCreatedAt()
        );
    }

    static ShowResponse toShowResponse(Show show, int totalSeats) {
        Screen screen = show.getScreen();
        Theater theater = screen.getTheater();
        City city = theater.getCity();
        return new ShowResponse(
                show.getId(),
                show.getMovie().getId(),
                show.getMovie().getTitle(),
                screen.getId(),
                screen.getName(),
                theater.getId(),
                theater.getName(),
                city.getId(),
                city.getName(),
                show.getStartTime(),
                show.getRegularBasePrice(),
                show.getPremiumBasePrice(),
                show.getWeekendMultiplier(),
                totalSeats,
                show.getCreatedAt()
        );
    }

    static RefundPolicyResponse toRefundPolicyResponse(RefundPolicy policy) {
        List<RefundPolicyResponse.RefundPolicyTierResponse> tiers = policy.getTiers().stream()
                .map(tier -> new RefundPolicyResponse.RefundPolicyTierResponse(
                        tier.getId(),
                        tier.getMinHoursBefore(),
                        tier.getRefundPercentage(),
                        tier.getSortOrder()
                ))
                .toList();

        return new RefundPolicyResponse(
                policy.getId(),
                policy.getTheater().getId(),
                policy.getTheater().getName(),
                policy.getName(),
                policy.isActive(),
                tiers,
                policy.getCreatedAt()
        );
    }

    static DiscountCodeResponse toDiscountCodeResponse(DiscountCode code) {
        return new DiscountCodeResponse(
                code.getId(),
                code.getCode(),
                code.getType(),
                code.getValue(),
                code.getMaxDiscountAmount(),
                code.getMinOrderAmount(),
                code.getValidFrom(),
                code.getValidUntil(),
                code.getMaxUsageCount(),
                code.getCurrentUsageCount(),
                code.isActive(),
                code.getCreatedAt()
        );
    }
}
