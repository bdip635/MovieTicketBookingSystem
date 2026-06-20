package com.movieticket.service.booking;

import com.movieticket.domain.entity.Show;
import com.movieticket.domain.entity.ShowSeat;
import com.movieticket.domain.enums.SeatStatus;
import com.movieticket.exception.ApiException;
import com.movieticket.repository.ShowRepository;
import com.movieticket.repository.ShowSeatRepository;
import com.movieticket.web.dto.booking.CustomerShowResponse;
import com.movieticket.web.dto.booking.SeatMapResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShowBrowseService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;

    @Transactional(readOnly = true)
    public List<CustomerShowResponse> browseShows(UUID cityId, LocalDate date) {
        LocalDate searchDate = date != null ? date : LocalDate.now(ZoneOffset.UTC);
        Instant from = searchDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = searchDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<Show> shows = cityId == null
                ? showRepository.findByStartTimeBetween(from, to)
                : showRepository.findByCityAndStartTimeBetween(cityId, from, to);

        return shows.stream()
                .map(this::toCustomerShowResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMap(UUID showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Show not found: " + showId));

        List<ShowSeat> showSeats = showSeatRepository.findByShowIdOrderBySeat_RowLabelAscSeat_SeatNumberAsc(showId);

        List<SeatMapResponse.SeatMapEntry> entries = showSeats.stream()
                .map(ss -> new SeatMapResponse.SeatMapEntry(
                        ss.getId(),
                        ss.getSeat().getId(),
                        ss.getSeat().getRowLabel(),
                        ss.getSeat().getSeatNumber(),
                        ss.getSeat().getTier(),
                        ss.getStatus(),
                        ss.getHoldExpiresAt()
                ))
                .toList();

        int available = countByStatus(showSeats, SeatStatus.AVAILABLE);
        int held = countByStatus(showSeats, SeatStatus.HELD);
        int booked = countByStatus(showSeats, SeatStatus.BOOKED);

        return new SeatMapResponse(
                show.getId(),
                show.getMovie().getTitle(),
                show.getStartTime(),
                entries,
                available,
                held,
                booked
        );
    }

    private CustomerShowResponse toCustomerShowResponse(Show show) {
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdOrderBySeat_RowLabelAscSeat_SeatNumberAsc(show.getId());
        var screen = show.getScreen();
        var theater = screen.getTheater();
        var city = theater.getCity();

        return new CustomerShowResponse(
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
                showSeats.size(),
                countByStatus(showSeats, SeatStatus.AVAILABLE),
                countByStatus(showSeats, SeatStatus.HELD),
                countByStatus(showSeats, SeatStatus.BOOKED),
                show.getCreatedAt()
        );
    }

    private int countByStatus(List<ShowSeat> showSeats, SeatStatus status) {
        return (int) showSeats.stream().filter(seat -> seat.getStatus() == status).count();
    }
}
