package com.movieticket.service.catalog;

import com.movieticket.domain.entity.Screen;
import com.movieticket.domain.entity.Theater;
import com.movieticket.exception.ApiException;
import com.movieticket.repository.ScreenRepository;
import com.movieticket.web.dto.catalog.CreateScreenRequest;
import com.movieticket.web.dto.catalog.ScreenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final TheaterService theaterService;

    @Transactional
    public ScreenResponse create(CreateScreenRequest request) {
        Theater theater = theaterService.getTheaterOrThrow(request.theaterId());

        Screen screen = Screen.builder()
                .theater(theater)
                .name(request.name().trim())
                .totalRows(request.totalRows())
                .totalColumns(request.totalColumns())
                .build();

        return CatalogMapper.toScreenResponse(screenRepository.save(screen));
    }

    @Transactional(readOnly = true)
    public List<ScreenResponse> listAll(UUID theaterId) {
        List<Screen> screens = theaterId == null
                ? screenRepository.findAll()
                : screenRepository.findByTheaterId(theaterId);

        return screens.stream()
                .map(CatalogMapper::toScreenResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Screen getScreenOrThrow(UUID screenId) {
        return screenRepository.findById(screenId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Screen not found: " + screenId));
    }
}
