package com.movieticket.service.catalog;

import com.movieticket.domain.entity.City;
import com.movieticket.exception.ApiException;
import com.movieticket.repository.CityRepository;
import com.movieticket.web.dto.catalog.CityResponse;
import com.movieticket.web.dto.catalog.CreateCityRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    @Transactional
    public CityResponse create(CreateCityRequest request) {
        City city = City.builder()
                .name(request.name().trim())
                .timezone(request.timezone().trim())
                .build();
        return CatalogMapper.toCityResponse(cityRepository.save(city));
    }

    @Transactional(readOnly = true)
    public List<CityResponse> listAll() {
        return cityRepository.findAll().stream()
                .map(CatalogMapper::toCityResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public City getCityOrThrow(UUID cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "City not found: " + cityId));
    }
}
