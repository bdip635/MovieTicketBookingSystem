package com.movieticket.service.catalog;

import com.movieticket.domain.entity.*;
import com.movieticket.domain.enums.SeatStatus;
import com.movieticket.exception.ApiException;
import com.movieticket.repository.SeatRepository;
import com.movieticket.repository.ShowRepository;
import com.movieticket.repository.ShowSeatRepository;
import com.movieticket.web.dto.catalog.CreateShowRequest;
import com.movieticket.web.dto.catalog.ShowResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatRepository seatRepository;
    private final MovieService movieService;
    private final ScreenService screenService;

    @Transactional
    public ShowResponse create(CreateShowRequest request) {
        Movie movie = movieService.getMovieOrThrow(request.movieId());
        Screen screen = screenService.getScreenOrThrow(request.screenId());

        List<Seat> seats = seatRepository.findByScreenIdOrderByRowLabelAscSeatNumberAsc(screen.getId());
        if (seats.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Screen has no seat layout defined");
        }

        BigDecimal weekendMultiplier = request.weekendMultiplier() != null
                ? request.weekendMultiplier()
                : new BigDecimal("1.20");

        Show show = Show.builder()
                .movie(movie)
                .screen(screen)
                .startTime(request.startTime())
                .regularBasePrice(request.regularBasePrice())
                .premiumBasePrice(request.premiumBasePrice())
                .weekendMultiplier(weekendMultiplier)
                .build();

        show = showRepository.save(show);

        List<ShowSeat> showSeats = new ArrayList<>();
        for (Seat seat : seats) {
            showSeats.add(ShowSeat.builder()
                    .show(show)
                    .seat(seat)
                    .status(SeatStatus.AVAILABLE)
                    .build());
        }
        showSeatRepository.saveAll(showSeats);

        return CatalogMapper.toShowResponse(show, showSeats.size());
    }

    @Transactional(readOnly = true)
    public List<ShowResponse> listAll() {
        return showRepository.findAll().stream()
                .map(show -> {
                    int totalSeats = showSeatRepository.findByShowIdOrderBySeat_RowLabelAscSeat_SeatNumberAsc(show.getId()).size();
                    return CatalogMapper.toShowResponse(show, totalSeats);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Show getShowOrThrow(UUID showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Show not found: " + showId));
    }
}
