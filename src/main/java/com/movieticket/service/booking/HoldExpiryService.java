package com.movieticket.service.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldExpiryService {

    private final HoldService holdService;

    public int releaseExpiredHolds() {
        int released = holdService.expireActiveHoldsDueBefore(Instant.now());
        if (released > 0) {
            log.info("Released {} expired seat hold(s)", released);
        }
        return released;
    }
}
