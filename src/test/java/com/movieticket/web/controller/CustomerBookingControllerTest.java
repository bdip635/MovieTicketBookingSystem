package com.movieticket.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticket.config.AdminDataInitializer;
import com.movieticket.domain.entity.SeatHold;
import com.movieticket.domain.enums.SeatTier;
import com.movieticket.repository.SeatHoldRepository;
import com.movieticket.service.booking.HoldExpiryService;
import com.movieticket.web.dto.auth.LoginRequest;
import com.movieticket.web.dto.auth.RegisterRequest;
import com.movieticket.web.dto.booking.CreateHoldRequest;
import com.movieticket.web.dto.catalog.CreateCityRequest;
import com.movieticket.web.dto.catalog.CreateMovieRequest;
import com.movieticket.web.dto.catalog.CreateScreenRequest;
import com.movieticket.web.dto.catalog.CreateSeatLayoutRequest;
import com.movieticket.web.dto.catalog.CreateShowRequest;
import com.movieticket.web.dto.catalog.CreateTheaterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SeatHoldRepository seatHoldRepository;

    @Autowired
    private HoldExpiryService holdExpiryService;

    private String adminToken;
    private UUID showId;
    private LocalDate showDate;
    private UUID seatIdA1;
    private UUID seatIdA2;

    @BeforeEach
    void setUpCatalog() throws Exception {
        adminToken = login(AdminDataInitializer.ADMIN_EMAIL, AdminDataInitializer.ADMIN_PASSWORD);

        UUID cityId = extractId(adminPost("/api/v1/admin/cities",
                new CreateCityRequest("Bangalore", "Asia/Kolkata")));

        UUID theaterId = extractId(adminPost("/api/v1/admin/theaters",
                new CreateTheaterRequest(cityId, "INOX Forum", "Forum Mall")));

        UUID screenId = extractId(adminPost("/api/v1/admin/screens",
                new CreateScreenRequest(theaterId, "Audi 1", 5, 8)));

        String seatsResponse = mockMvc.perform(post("/api/v1/admin/screens/{screenId}/seats", screenId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatLayoutRequest(List.of(
                                new CreateSeatLayoutRequest.SeatDefinition("A", 1, SeatTier.REGULAR),
                                new CreateSeatLayoutRequest.SeatDefinition("A", 2, SeatTier.PREMIUM)
                        )))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode seats = objectMapper.readTree(seatsResponse);
        seatIdA1 = UUID.fromString(seats.get(0).get("id").asText());
        seatIdA2 = UUID.fromString(seats.get(1).get("id").asText());

        UUID movieId = extractId(adminPost("/api/v1/admin/movies",
                new CreateMovieRequest("Interstellar", "Space epic", 169, "English")));

        Instant showStart = Instant.now().plus(3, ChronoUnit.DAYS);
        showDate = showStart.atZone(ZoneOffset.UTC).toLocalDate();

        showId = extractId(adminPost("/api/v1/admin/shows",
                new CreateShowRequest(
                        movieId,
                        screenId,
                        showStart,
                        new BigDecimal("250.00"),
                        new BigDecimal("400.00"),
                        new BigDecimal("1.20")
                )));
    }

    @Test
    void customerCanBrowseSeatMapAndHoldSeats() throws Exception {
        String customerToken = registerCustomer("browse-user@test.com");

        mockMvc.perform(get("/api/v1/shows")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("date", showDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].availableSeats").value(2));

        mockMvc.perform(get("/api/v1/shows/{showId}/seats", showId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCount").value(2))
                .andExpect(jsonPath("$.seats", hasSize(2)));

        String holdResponse = mockMvc.perform(post("/api/v1/shows/{showId}/holds", showId)
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateHoldRequest(List.of(seatIdA1)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.subtotal").value(250.00))
                .andExpect(jsonPath("$.seats", hasSize(1)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID holdId = UUID.fromString(objectMapper.readTree(holdResponse).get("holdId").asText());

        mockMvc.perform(get("/api/v1/holds/{holdId}", holdId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdId").value(holdId.toString()));

        mockMvc.perform(get("/api/v1/shows/{showId}/seats", showId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCount").value(1))
                .andExpect(jsonPath("$.heldCount").value(1));
    }

    @Test
    void secondCustomerCannotHoldAlreadyHeldSeat() throws Exception {
        String customerOne = registerCustomer("holder-one@test.com");
        String customerTwo = registerCustomer("holder-two@test.com");

        mockMvc.perform(post("/api/v1/shows/{showId}/holds", showId)
                        .header("Authorization", "Bearer " + customerOne)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateHoldRequest(List.of(seatIdA1)))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/shows/{showId}/holds", showId)
                        .header("Authorization", "Bearer " + customerTwo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateHoldRequest(List.of(seatIdA1)))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("A1")));
    }

    @Test
    void customerCanReleaseHoldAndExpiredHoldsAreFreed() throws Exception {
        String customerToken = registerCustomer("release-user@test.com");

        String holdResponse = mockMvc.perform(post("/api/v1/shows/{showId}/holds", showId)
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateHoldRequest(List.of(seatIdA2)))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID holdId = UUID.fromString(objectMapper.readTree(holdResponse).get("holdId").asText());

        mockMvc.perform(delete("/api/v1/holds/{holdId}", holdId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/shows/{showId}/seats", showId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCount").value(2));

        holdResponse = mockMvc.perform(post("/api/v1/shows/{showId}/holds", showId)
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateHoldRequest(List.of(seatIdA1)))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        holdId = UUID.fromString(objectMapper.readTree(holdResponse).get("holdId").asText());

        SeatHold hold = seatHoldRepository.findById(holdId).orElseThrow();
        hold.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        seatHoldRepository.save(hold);

        holdExpiryService.releaseExpiredHolds();

        mockMvc.perform(get("/api/v1/shows/{showId}/seats", showId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCount").value(2))
                .andExpect(jsonPath("$.heldCount").value(0));

        mockMvc.perform(get("/api/v1/holds/{holdId}", holdId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXPIRED"));
    }

    private String registerCustomer(String email) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest(email, "password123", "Customer User"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString()).get("token").asText();
    }

    private String login(String email, String password) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).get("token").asText();
    }

    private UUID extractId(String responseBody) throws Exception {
        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    private String adminPost(String path, Object body) throws Exception {
        return mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
