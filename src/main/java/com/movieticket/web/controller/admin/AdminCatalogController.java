package com.movieticket.web.controller.admin;

import com.movieticket.service.catalog.*;
import com.movieticket.web.dto.catalog.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final CityService cityService;
    private final TheaterService theaterService;
    private final ScreenService screenService;
    private final SeatService seatService;
    private final MovieService movieService;
    private final ShowService showService;
    private final RefundPolicyService refundPolicyService;
    private final DiscountCodeService discountCodeService;

    @PostMapping("/cities")
    @ResponseStatus(HttpStatus.CREATED)
    public CityResponse createCity(@Valid @RequestBody CreateCityRequest request) {
        return cityService.create(request);
    }

    @GetMapping("/cities")
    public List<CityResponse> listCities() {
        return cityService.listAll();
    }

    @PostMapping("/theaters")
    @ResponseStatus(HttpStatus.CREATED)
    public TheaterResponse createTheater(@Valid @RequestBody CreateTheaterRequest request) {
        return theaterService.create(request);
    }

    @GetMapping("/theaters")
    public List<TheaterResponse> listTheaters(@RequestParam(required = false) UUID cityId) {
        return theaterService.listAll(cityId);
    }

    @PostMapping("/screens")
    @ResponseStatus(HttpStatus.CREATED)
    public ScreenResponse createScreen(@Valid @RequestBody CreateScreenRequest request) {
        return screenService.create(request);
    }

    @GetMapping("/screens")
    public List<ScreenResponse> listScreens(@RequestParam(required = false) UUID theaterId) {
        return screenService.listAll(theaterId);
    }

    @PostMapping("/screens/{screenId}/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SeatResponse> createSeatLayout(
            @PathVariable UUID screenId,
            @Valid @RequestBody CreateSeatLayoutRequest request) {
        return seatService.createLayout(screenId, request);
    }

    @GetMapping("/screens/{screenId}/seats")
    public List<SeatResponse> listSeats(@PathVariable UUID screenId) {
        return seatService.listByScreen(screenId);
    }

    @PostMapping("/movies")
    @ResponseStatus(HttpStatus.CREATED)
    public MovieResponse createMovie(@Valid @RequestBody CreateMovieRequest request) {
        return movieService.create(request);
    }

    @GetMapping("/movies")
    public List<MovieResponse> listMovies() {
        return movieService.listAll();
    }

    @PostMapping("/shows")
    @ResponseStatus(HttpStatus.CREATED)
    public ShowResponse createShow(@Valid @RequestBody CreateShowRequest request) {
        return showService.create(request);
    }

    @GetMapping("/shows")
    public List<ShowResponse> listShows() {
        return showService.listAll();
    }

    @PostMapping("/refund-policies")
    @ResponseStatus(HttpStatus.CREATED)
    public RefundPolicyResponse createRefundPolicy(@Valid @RequestBody CreateRefundPolicyRequest request) {
        return refundPolicyService.create(request);
    }

    @GetMapping("/refund-policies")
    public List<RefundPolicyResponse> listRefundPolicies(@RequestParam(required = false) UUID theaterId) {
        return refundPolicyService.listAll(theaterId);
    }

    @PostMapping("/discount-codes")
    @ResponseStatus(HttpStatus.CREATED)
    public DiscountCodeResponse createDiscountCode(@Valid @RequestBody CreateDiscountCodeRequest request) {
        return discountCodeService.create(request);
    }

    @GetMapping("/discount-codes")
    public List<DiscountCodeResponse> listDiscountCodes() {
        return discountCodeService.listAll();
    }
}
