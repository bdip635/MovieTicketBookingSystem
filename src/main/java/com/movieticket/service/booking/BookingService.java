package com.movieticket.service.booking;

import com.movieticket.domain.entity.*;
import com.movieticket.domain.enums.BookingStatus;
import com.movieticket.domain.enums.HoldStatus;
import com.movieticket.domain.enums.PaymentStatus;
import com.movieticket.domain.enums.SeatStatus;
import com.movieticket.exception.ApiException;
import com.movieticket.repository.BookingRepository;
import com.movieticket.repository.DiscountCodeRepository;
import com.movieticket.repository.SeatHoldRepository;
import com.movieticket.repository.ShowSeatRepository;
import com.movieticket.service.notification.NotificationService;
import com.movieticket.service.payment.MockPaymentService;
import com.movieticket.service.payment.PaymentResult;
import com.movieticket.service.pricing.DiscountContext;
import com.movieticket.service.pricing.DiscountResult;
import com.movieticket.service.pricing.DiscountService;
import com.movieticket.web.dto.booking.BookingResponse;
import com.movieticket.web.dto.booking.ConfirmHoldRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final SeatHoldRepository seatHoldRepository;
    private final ShowSeatRepository showSeatRepository;
    private final BookingRepository bookingRepository;
    private final DiscountCodeRepository discountCodeRepository;
    private final DiscountService discountService;
    private final MockPaymentService mockPaymentService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public BookingResponse confirmHold(UUID holdId, ConfirmHoldRequest request, UUID userId) {
        SeatHold hold = seatHoldRepository.findByIdAndUserId(holdId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hold not found"));

        validateActiveHold(hold);

        List<ShowSeat> showSeats = showSeatRepository.findByIdInForUpdate(
                hold.getItems().stream()
                        .map(item -> item.getShowSeat().getId())
                        .toList());

        for (ShowSeat showSeat : showSeats) {
            if (showSeat.getStatus() != SeatStatus.HELD || showSeat.getSeatHold() == null
                    || !showSeat.getSeatHold().getId().equals(hold.getId())) {
                throw new ApiException(HttpStatus.CONFLICT, "Hold is no longer valid for one or more seats");
            }
        }

        DiscountContext discountContext = buildDiscountContext(hold, userId);
        DiscountResult discountResult = discountService.applyDiscount(request.discountCode(), discountContext);

        PaymentResult paymentResult = mockPaymentService.charge(discountResult.finalAmount());

        DiscountCode appliedCode = discountService.getAppliedCode(request.discountCode());
        if (appliedCode != null) {
            appliedCode.setCurrentUsageCount(appliedCode.getCurrentUsageCount() + 1);
            discountCodeRepository.save(appliedCode);
        }

        Booking booking = Booking.builder()
                .show(hold.getShow())
                .user(hold.getUser())
                .seatHold(hold)
                .status(BookingStatus.CONFIRMED)
                .subtotal(hold.getSubtotal())
                .discountAmount(discountResult.discountAmount())
                .finalAmount(discountResult.finalAmount())
                .discountCode(appliedCode)
                .confirmationCode(generateConfirmationCode())
                .build();

        for (SeatHoldItem item : hold.getItems()) {
            ShowSeat showSeat = item.getShowSeat();
            showSeat.setStatus(SeatStatus.BOOKED);
            showSeat.setHeldByUser(null);
            showSeat.setHoldExpiresAt(null);
            showSeat.setSeatHold(null);

            BookingSeat bookingSeat = BookingSeat.builder()
                    .showSeat(showSeat)
                    .unitPrice(item.getUnitPrice())
                    .build();
            booking.addSeat(bookingSeat);
        }

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(discountResult.finalAmount())
                .status(paymentResult.status())
                .transactionId(paymentResult.transactionId())
                .failureReason(paymentResult.failureReason())
                .build();
        booking.setPayment(payment);

        hold.setStatus(HoldStatus.CONFIRMED);
        seatHoldRepository.save(hold);
        showSeatRepository.saveAll(showSeats);
        booking = bookingRepository.save(booking);

        eventPublisher.publishEvent(new BookingConfirmedEvent(booking.getId()));

        return toBookingResponse(booking, discountResult.description());
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getUser().getId().equals(userId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
        return toBookingResponse(booking, discountDescription(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listBookings(UUID userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(booking -> toBookingResponse(booking, discountDescription(booking)))
                .toList();
    }

    private void validateActiveHold(SeatHold hold) {
        if (hold.getStatus() != HoldStatus.ACTIVE) {
            throw new ApiException(HttpStatus.CONFLICT, "Hold is no longer active");
        }
        if (hold.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.GONE, "Hold has expired");
        }
    }

    private DiscountContext buildDiscountContext(SeatHold hold, UUID userId) {
        List<DiscountContext.SeatPricingLine> lines = hold.getItems().stream()
                .map(item -> {
                    Seat seat = item.getShowSeat().getSeat();
                    return new DiscountContext.SeatPricingLine(
                            seat.getId(),
                            seat.getRowLabel(),
                            seat.getSeatNumber(),
                            item.getUnitPrice()
                    );
                })
                .toList();

        return new DiscountContext(
                hold.getSubtotal(),
                lines,
                userId,
                hold.getShow().getId(),
                Instant.now()
        );
    }

    private String generateConfirmationCode() {
        return "BK" + ThreadLocalRandom.current().nextInt(100_000, 999_999);
    }

    private BookingResponse toBookingResponse(Booking booking, String discountDescription) {
        List<BookingResponse.BookingSeatLine> seats = booking.getSeats().stream()
                .map(bookingSeat -> {
                    ShowSeat showSeat = bookingSeat.getShowSeat();
                    Seat seat = showSeat.getSeat();
                    return new BookingResponse.BookingSeatLine(
                            showSeat.getId(),
                            seat.getId(),
                            seat.getRowLabel(),
                            seat.getSeatNumber(),
                            seat.getTier(),
                            bookingSeat.getUnitPrice()
                    );
                })
                .toList();

        return new BookingResponse(
                booking.getId(),
                booking.getConfirmationCode(),
                booking.getStatus(),
                booking.getShow().getId(),
                booking.getShow().getMovie().getTitle(),
                booking.getShow().getStartTime(),
                booking.getSubtotal(),
                booking.getDiscountAmount(),
                booking.getFinalAmount(),
                booking.getDiscountCode() != null ? booking.getDiscountCode().getCode() : null,
                discountDescription,
                booking.getPayment().getTransactionId(),
                booking.getCreatedAt(),
                seats
        );
    }

    private String discountDescription(Booking booking) {
        if (booking.getDiscountCode() == null) {
            return "No discount applied";
        }
        return "Discount code " + booking.getDiscountCode().getCode() + " applied";
    }
}
