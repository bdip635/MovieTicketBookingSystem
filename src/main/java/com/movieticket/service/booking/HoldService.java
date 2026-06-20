package com.movieticket.service.booking;

import com.movieticket.config.BookingProperties;
import com.movieticket.domain.entity.*;
import com.movieticket.domain.enums.HoldStatus;
import com.movieticket.domain.enums.SeatStatus;
import com.movieticket.exception.ApiException;
import com.movieticket.repository.SeatHoldRepository;
import com.movieticket.repository.ShowRepository;
import com.movieticket.repository.ShowSeatRepository;
import com.movieticket.repository.UserRepository;
import com.movieticket.service.pricing.PriceCalculator;
import com.movieticket.web.dto.booking.CreateHoldRequest;
import com.movieticket.web.dto.booking.HoldResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HoldService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final UserRepository userRepository;
    private final PriceCalculator priceCalculator;
    private final BookingProperties bookingProperties;

    @Transactional
    public HoldResponse createHold(UUID showId, CreateHoldRequest request, UUID userId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Show not found: " + showId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        List<UUID> seatIds = distinctSeatIds(request.seatIds());
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndSeatIdInForUpdate(showId, seatIds);

        if (showSeats.size() != seatIds.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "One or more seats were not found for this show");
        }

        for (ShowSeat showSeat : showSeats) {
            if (showSeat.getStatus() != SeatStatus.AVAILABLE) {
                Seat seat = showSeat.getSeat();
                throw new ApiException(HttpStatus.CONFLICT,
                        "Seat " + seat.getRowLabel() + seat.getSeatNumber() + " is already held or booked");
            }
        }

        Instant expiresAt = Instant.now().plus(bookingProperties.getHoldDurationMinutes(), ChronoUnit.MINUTES);
        BigDecimal subtotal = BigDecimal.ZERO;

        SeatHold hold = SeatHold.builder()
                .show(show)
                .user(user)
                .status(HoldStatus.ACTIVE)
                .expiresAt(expiresAt)
                .subtotal(BigDecimal.ZERO)
                .build();

        for (ShowSeat showSeat : showSeats) {
            BigDecimal unitPrice = priceCalculator.calculateUnitPrice(show, showSeat.getSeat().getTier());
            subtotal = subtotal.add(unitPrice);

            showSeat.setStatus(SeatStatus.HELD);
            showSeat.setHeldByUser(user);
            showSeat.setHoldExpiresAt(expiresAt);
            showSeat.setSeatHold(hold);

            SeatHoldItem item = SeatHoldItem.builder()
                    .showSeat(showSeat)
                    .unitPrice(unitPrice)
                    .build();
            hold.addItem(item);
        }

        hold.setSubtotal(subtotal);
        hold = seatHoldRepository.save(hold);
        showSeatRepository.saveAll(showSeats);

        return toHoldResponse(hold);
    }

    @Transactional(readOnly = true)
    public HoldResponse getHold(UUID holdId, UUID userId) {
        SeatHold hold = seatHoldRepository.findByIdAndUserId(holdId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hold not found"));
        return toHoldResponse(hold);
    }

    @Transactional
    public void releaseHold(UUID holdId, UUID userId) {
        SeatHold hold = seatHoldRepository.findByIdAndUserId(holdId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hold not found"));

        if (hold.getStatus() != HoldStatus.ACTIVE) {
            throw new ApiException(HttpStatus.CONFLICT, "Hold is no longer active");
        }

        finalizeHold(hold, HoldStatus.RELEASED);
    }

    @Transactional
    public int expireActiveHoldsDueBefore(Instant cutoff) {
        List<SeatHold> expiredHolds = seatHoldRepository.findByStatusAndExpiresAtBefore(HoldStatus.ACTIVE, cutoff);
        expiredHolds.forEach(hold -> finalizeHold(hold, HoldStatus.EXPIRED));
        return expiredHolds.size();
    }

    private void finalizeHold(SeatHold hold, HoldStatus newStatus) {
        List<ShowSeat> showSeats = showSeatRepository.findBySeatHold_Id(hold.getId());
        for (ShowSeat showSeat : showSeats) {
            showSeat.setStatus(SeatStatus.AVAILABLE);
            showSeat.setHeldByUser(null);
            showSeat.setHoldExpiresAt(null);
            showSeat.setSeatHold(null);
        }
        showSeatRepository.saveAll(showSeats);
        hold.setStatus(newStatus);
        seatHoldRepository.save(hold);
    }

    private HoldResponse toHoldResponse(SeatHold hold) {
        List<HoldResponse.HoldSeatLine> lines = hold.getItems().stream()
                .map(item -> {
                    ShowSeat showSeat = item.getShowSeat();
                    Seat seat = showSeat.getSeat();
                    return new HoldResponse.HoldSeatLine(
                            showSeat.getId(),
                            seat.getId(),
                            seat.getRowLabel(),
                            seat.getSeatNumber(),
                            seat.getTier(),
                            item.getUnitPrice()
                    );
                })
                .toList();

        return new HoldResponse(
                hold.getId(),
                hold.getShow().getId(),
                hold.getShow().getMovie().getTitle(),
                hold.getStatus(),
                hold.getExpiresAt(),
                hold.getSubtotal(),
                lines
        );
    }

    private List<UUID> distinctSeatIds(List<UUID> seatIds) {
        Set<UUID> unique = new HashSet<>(seatIds);
        if (unique.size() != seatIds.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Duplicate seat IDs in request");
        }
        return seatIds;
    }
}
