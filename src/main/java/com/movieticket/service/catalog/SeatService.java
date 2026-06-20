package com.movieticket.service.catalog;

import com.movieticket.domain.entity.Screen;
import com.movieticket.domain.entity.Seat;
import com.movieticket.exception.ApiException;
import com.movieticket.repository.SeatRepository;
import com.movieticket.web.dto.catalog.CreateSeatLayoutRequest;
import com.movieticket.web.dto.catalog.SeatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ScreenService screenService;

    @Transactional
    public List<SeatResponse> createLayout(UUID screenId, CreateSeatLayoutRequest request) {
        Screen screen = screenService.getScreenOrThrow(screenId);

        if (!seatRepository.findByScreenIdOrderByRowLabelAscSeatNumberAsc(screenId).isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "Seat layout already exists for screen: " + screenId);
        }

        Set<String> uniquePositions = new HashSet<>();
        List<Seat> seats = request.seats().stream()
                .map(definition -> {
                    String key = definition.rowLabel().trim().toUpperCase() + "-" + definition.seatNumber();
                    if (!uniquePositions.add(key)) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Duplicate seat position: " + key);
                    }
                    return Seat.builder()
                            .screen(screen)
                            .rowLabel(definition.rowLabel().trim().toUpperCase())
                            .seatNumber(definition.seatNumber())
                            .tier(definition.tier())
                            .build();
                })
                .toList();

        return seatRepository.saveAll(seats).stream()
                .map(CatalogMapper::toSeatResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> listByScreen(UUID screenId) {
        screenService.getScreenOrThrow(screenId);
        return seatRepository.findByScreenIdOrderByRowLabelAscSeatNumberAsc(screenId).stream()
                .map(CatalogMapper::toSeatResponse)
                .toList();
    }
}
