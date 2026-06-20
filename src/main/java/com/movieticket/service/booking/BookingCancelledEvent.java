package com.movieticket.service.booking;

import java.util.UUID;

public record BookingCancelledEvent(UUID bookingId) {
}
