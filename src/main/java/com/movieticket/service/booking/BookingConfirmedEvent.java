package com.movieticket.service.booking;

import java.util.UUID;

public record BookingConfirmedEvent(UUID bookingId) {
}
