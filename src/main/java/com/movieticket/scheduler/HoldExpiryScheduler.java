package com.movieticket.scheduler;

import com.movieticket.service.booking.HoldExpiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HoldExpiryScheduler {

    private final HoldExpiryService holdExpiryService;

    @Scheduled(fixedRateString = "${app.booking.hold-expiry-interval-ms:30000}")
    public void expireHolds() {
        holdExpiryService.releaseExpiredHolds();
    }
}
