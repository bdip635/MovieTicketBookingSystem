package com.movieticket.service.catalog;

import com.movieticket.domain.entity.City;
import com.movieticket.domain.entity.Theater;
import com.movieticket.exception.ApiException;
import com.movieticket.repository.TheaterRepository;
import com.movieticket.web.dto.catalog.CreateTheaterRequest;
import com.movieticket.web.dto.catalog.TheaterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final CityService cityService;

    @Transactional
    public TheaterResponse create(CreateTheaterRequest request) {
        City city = cityService.getCityOrThrow(request.cityId());

        Theater theater = Theater.builder()
                .city(city)
                .name(request.name().trim())
                .address(request.address())
                .build();

        return CatalogMapper.toTheaterResponse(theaterRepository.save(theater));
    }

    @Transactional(readOnly = true)
    public List<TheaterResponse> listAll(UUID cityId) {
        List<Theater> theaters = cityId == null
                ? theaterRepository.findAll()
                : theaterRepository.findByCityId(cityId);

        return theaters.stream()
                .map(CatalogMapper::toTheaterResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Theater getTheaterOrThrow(UUID theaterId) {
        return theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Theater not found: " + theaterId));
    }
}
