package com.movieticket.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticket.config.AdminDataInitializer;
import com.movieticket.domain.entity.Booking;
import com.movieticket.domain.entity.Show;
import com.movieticket.domain.enums.NotificationType;
import com.movieticket.domain.enums.SeatTier;
import com.movieticket.repository.BookingRepository;
import com.movieticket.repository.NotificationLogRepository;
import com.movieticket.repository.ShowRepository;
import com.movieticket.service.notification.NotificationService;
import com.movieticket.web.dto.auth.LoginRequest;
import com.movieticket.web.dto.auth.RegisterRequest;
import com.movieticket.web.dto.booking.ConfirmHoldRequest;
import com.movieticket.web.dto.booking.CreateHoldRequest;
import com.movieticket.web.dto.catalog.CreateCityRequest;
import com.movieticket.web.dto.catalog.CreateMovieRequest;
import com.movieticket.web.dto.catalog.CreateRefundPolicyRequest;
import com.movieticket.web.dto.catalog.CreateScreenRequest;
import com.movieticket.web.dto.catalog.CreateSeatLayoutRequest;
import com.movieticket.web.dto.catalog.CreateShowRequest;
import com.movieticket.web.dto.catalog.CreateTheaterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingCancelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowRepository showRepository;

    private String adminToken;
    private String customerToken;
    private UUID showId;
    private UUID seatId;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = login(AdminDataInitializer.ADMIN_EMAIL, AdminDataInitializer.ADMIN_PASSWORD);
        customerToken = registerCustomer("cancel-user-" + UUID.randomUUID() + "@test.com");

        UUID cityId = extractId(adminPost("/api/v1/admin/cities",
                new CreateCityRequest("Chennai", "Asia/Kolkata")));
        UUID theaterId = extractId(adminPost("/api/v1/admin/theaters",
                new CreateTheaterRequest(cityId, "SPI Cinemas", "Express Avenue")));
        UUID screenId = extractId(adminPost("/api/v1/admin/screens",
                new CreateScreenRequest(theaterId, "Audi A", 5, 8)));

        String seatsResponse = mockMvc.perform(post("/api/v1/admin/screens/{screenId}/seats", screenId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatLayoutRequest(List.of(
                                new CreateSeatLayoutRequest.SeatDefinition("C", 1, SeatTier.REGULAR)
                        )))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        seatId = UUID.fromString(objectMapper.readTree(seatsResponse).get(0).get("id").asText());

        UUID movieId = extractId(adminPost("/api/v1/admin/movies",
                new CreateMovieRequest("Oppenheimer", "Biographical drama", 180, "English")));

        showId = extractId(adminPost("/api/v1/admin/shows",
                new CreateShowRequest(
                        movieId,
                        screenId,
                        Instant.now().plus(48, ChronoUnit.HOURS),
                        new BigDecimal("400.00"),
                        new BigDecimal("600.00"),
                        new BigDecimal("1.20")
                )));

        adminPost("/api/v1/admin/refund-policies", new CreateRefundPolicyRequest(
                theaterId,
                "Standard Policy",
                true,
                List.of(
                        new CreateRefundPolicyRequest.RefundPolicyTierRequest(new BigDecimal("24"), new BigDecimal("100"), 1),
                        new CreateRefundPolicyRequest.RefundPolicyTierRequest(new BigDecimal("2"), new BigDecimal("50"), 2),
                        new CreateRefundPolicyRequest.RefundPolicyTierRequest(new BigDecimal("0"), new BigDecimal("0"), 3)
                )
        ));
    }

    @Test
    void customerCanCancelBookingWithFullRefundAndSeatsReleased() throws Exception {
        UUID bookingId = confirmBooking(customerToken, seatId);

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/cancel", bookingId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.refundPercentage").value(100.00))
                .andExpect(jsonPath("$.refundAmount").value(400.00));

        mockMvc.perform(get("/api/v1/bookings/{bookingId}", bookingId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.refund.refundAmount").value(400.00));

        mockMvc.perform(get("/api/v1/shows/{showId}/seats", showId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCount").value(1));

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/cancel", bookingId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    void sendsShowReminderWhenShowIsWithinReminderWindow() throws Exception {
        UUID bookingId = confirmBooking(customerToken, seatId);

        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        Show show = booking.getShow();
        show.setStartTime(Instant.now().plus(2, ChronoUnit.HOURS));
        showRepository.save(show);

        int sent = notificationService.sendDueShowReminders();

        assertThat(sent).isEqualTo(1);
        assertThat(notificationLogRepository.existsByBooking_IdAndType(bookingId, NotificationType.SHOW_REMINDER))
                .isTrue();
    }

    private UUID confirmBooking(String token, UUID seat) throws Exception {
        String holdResponse = mockMvc.perform(post("/api/v1/shows/{showId}/holds", showId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateHoldRequest(List.of(seat)))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID holdId = UUID.fromString(objectMapper.readTree(holdResponse).get("holdId").asText());

        String bookingResponse = mockMvc.perform(post("/api/v1/holds/{holdId}/confirm", holdId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmHoldRequest(null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return UUID.fromString(objectMapper.readTree(bookingResponse).get("bookingId").asText());
    }

    private String registerCustomer(String email) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest(email, "password123", "Cancel User"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).get("token").asText();
    }

    private String login(String email, String password) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("token").asText();
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
                .andReturn().getResponse().getContentAsString();
    }
}
