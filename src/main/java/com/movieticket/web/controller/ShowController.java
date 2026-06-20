package com.movieticket.web.controller;

import com.movieticket.service.booking.ShowBrowseService;
import com.movieticket.web.dto.booking.CustomerShowResponse;
import com.movieticket.web.dto.booking.SeatMapResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowBrowseService showBrowseService;

    @GetMapping
    public List<CustomerShowResponse> browseShows(
            @RequestParam(required = false) UUID cityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return showBrowseService.browseShows(cityId, date);
    }

    @GetMapping("/{showId}/seats")
    public SeatMapResponse getSeatMap(@PathVariable UUID showId) {
        return showBrowseService.getSeatMap(showId);
    }
}
