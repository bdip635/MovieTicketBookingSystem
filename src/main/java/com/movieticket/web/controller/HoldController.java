package com.movieticket.web.controller;

import com.movieticket.security.SecurityUtils;
import com.movieticket.service.booking.HoldService;
import com.movieticket.web.dto.booking.CreateHoldRequest;
import com.movieticket.web.dto.booking.HoldResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class HoldController {

    private final HoldService holdService;

    @PostMapping("/api/v1/shows/{showId}/holds")
    @ResponseStatus(HttpStatus.CREATED)
    public HoldResponse createHold(
            @PathVariable UUID showId,
            @Valid @RequestBody CreateHoldRequest request) {
        return holdService.createHold(showId, request, SecurityUtils.currentUserId());
    }

    @GetMapping("/api/v1/holds/{holdId}")
    public HoldResponse getHold(@PathVariable UUID holdId) {
        return holdService.getHold(holdId, SecurityUtils.currentUserId());
    }

    @DeleteMapping("/api/v1/holds/{holdId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void releaseHold(@PathVariable UUID holdId) {
        holdService.releaseHold(holdId, SecurityUtils.currentUserId());
    }
}
