package com.movieticket.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticket.config.AdminDataInitializer;
import com.movieticket.domain.enums.DiscountType;
import com.movieticket.domain.enums.SeatTier;
import com.movieticket.repository.NotificationLogRepository;
import com.movieticket.web.dto.auth.LoginRequest;
import com.movieticket.web.dto.auth.RegisterRequest;
import com.movieticket.web.dto.booking.ConfirmHoldRequest;
import com.movieticket.web.dto.booking.CreateHoldRequest;
import com.movieticket.web.dto.catalog.CreateCityRequest;
import com.movieticket.web.dto.catalog.CreateDiscountCodeRequest;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingConfirmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    private String adminToken;
    private String customerToken;
    private UUID showId;
    private UUID seatId;
    private UUID premiumSeatId;
    private String discountCode;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = login(AdminDataInitializer.ADMIN_EMAIL, AdminDataInitializer.ADMIN_PASSWORD);
        customerToken = registerCustomer("confirm-user-" + UUID.randomUUID() + "@test.com");

        UUID cityId = extractId(adminPost("/api/v1/admin/cities",
                new CreateCityRequest("Delhi", "Asia/Kolkata")));
        UUID theaterId = extractId(adminPost("/api/v1/admin/theaters",
                new CreateTheaterRequest(cityId, "Cinepolis", "DLF")));
        UUID screenId = extractId(adminPost("/api/v1/admin/screens",
                new CreateScreenRequest(theaterId, "Screen 2", 4, 6)));

        String seatsResponse = mockMvc.perform(post("/api/v1/admin/screens/{screenId}/seats", screenId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatLayoutRequest(List.of(
                                new CreateSeatLayoutRequest.SeatDefinition("B", 1, SeatTier.REGULAR),
                                new CreateSeatLayoutRequest.SeatDefinition("B", 2, SeatTier.PREMIUM)
                        )))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode seats = objectMapper.readTree(seatsResponse);
        seatId = UUID.fromString(seats.get(0).get("id").asText());
        premiumSeatId = UUID.fromString(seats.get(1).get("id").asText());

        UUID movieId = extractId(adminPost("/api/v1/admin/movies",
                new CreateMovieRequest("Dune", "Sci-fi epic", 155, "English")));

        showId = extractId(adminPost("/api/v1/admin/shows",
                new CreateShowRequest(
                        movieId,
                        screenId,
                        Instant.now().plus(4, ChronoUnit.DAYS),
                        new BigDecimal("300.00"),
                        new BigDecimal("500.00"),
                        new BigDecimal("1.20")
                )));

        discountCode = "SAVE50-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        adminPost("/api/v1/admin/discount-codes", new CreateDiscountCodeRequest(
                discountCode,
                DiscountType.FIXED_AMOUNT,
                new BigDecimal("50.00"),
                null,
                new BigDecimal("200.00"),
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().plus(30, ChronoUnit.DAYS),
                10,
                true
        ));
    }

    @Test
    void customerCanConfirmHoldWithDiscountAndViewBooking() throws Exception {
        String holdResponse = mockMvc.perform(post("/api/v1/shows/{showId}/holds", showId)
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateHoldRequest(List.of(seatId)))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID holdId = UUID.fromString(objectMapper.readTree(holdResponse).get("holdId").asText());

        String bookingResponse = mockMvc.perform(post("/api/v1/holds/{holdId}/confirm", holdId)
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmHoldRequest(discountCode))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(300.00))
                .andExpect(jsonPath("$.discountAmount").value(50.00))
                .andExpect(jsonPath("$.finalAmount").value(250.00))
                .andExpect(jsonPath("$.discountCode").value(discountCode))
                .andExpect(jsonPath("$.paymentTransactionId", startsWith("TXN-")))
                .andExpect(jsonPath("$.confirmationCode", startsWith("BK")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID bookingId = UUID.fromString(objectMapper.readTree(bookingResponse).get("bookingId").asText());

        mockMvc.perform(get("/api/v1/bookings/{bookingId}", bookingId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(get("/api/v1/bookings")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(get("/api/v1/shows/{showId}/seats", showId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookedCount").value(1))
                .andExpect(jsonPath("$.availableCount").value(1));

        TimeUnit.MILLISECONDS.sleep(300);
        assertThat(notificationLogRepository.count()).isGreaterThan(0);
    }

    @Test
    void cannotConfirmHoldTwice() throws Exception {
        UUID holdId = createHold(customerToken, premiumSeatId);

        mockMvc.perform(post("/api/v1/holds/{holdId}/confirm", holdId)
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/holds/{holdId}/confirm", holdId)
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict());
    }

    private UUID createHold(String token, UUID seat) throws Exception {
        String holdResponse = mockMvc.perform(post("/api/v1/shows/{showId}/holds", showId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateHoldRequest(List.of(seat)))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(holdResponse).get("holdId").asText());
    }

    private String registerCustomer(String email) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest(email, "password123", "Confirm User"))))
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
