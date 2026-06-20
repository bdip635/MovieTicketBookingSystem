package com.movieticket.service.catalog;

import com.movieticket.domain.entity.Movie;
import com.movieticket.exception.ApiException;
import com.movieticket.repository.MovieRepository;
import com.movieticket.web.dto.catalog.CreateMovieRequest;
import com.movieticket.web.dto.catalog.MovieResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    @Transactional
    public MovieResponse create(CreateMovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.title().trim())
                .description(request.description())
                .durationMinutes(request.durationMinutes())
                .language(request.language())
                .build();

        return CatalogMapper.toMovieResponse(movieRepository.save(movie));
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> listAll() {
        return movieRepository.findAll().stream()
                .map(CatalogMapper::toMovieResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Movie getMovieOrThrow(UUID movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Movie not found: " + movieId));
    }
}
