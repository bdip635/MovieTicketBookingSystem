package com.movieticket.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticket.config.AdminDataInitializer;
import com.movieticket.domain.enums.DiscountType;
import com.movieticket.domain.enums.SeatTier;
import com.movieticket.web.dto.auth.LoginRequest;
import com.movieticket.web.dto.catalog.CreateCityRequest;
import com.movieticket.web.dto.catalog.CreateDiscountCodeRequest;
import com.movieticket.web.dto.catalog.CreateMovieRequest;
import com.movieticket.web.dto.catalog.CreateRefundPolicyRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void loginAsAdmin() throws Exception {
        LoginRequest loginRequest = new LoginRequest(
                AdminDataInitializer.ADMIN_EMAIL,
                AdminDataInitializer.ADMIN_PASSWORD
        );

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        adminToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void adminCanCreateFullCatalogAndShow() throws Exception {
        String cityResponse = mockMvc.perform(post("/api/v1/admin/cities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCityRequest("Mumbai", "Asia/Kolkata"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mumbai"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID cityId = UUID.fromString(objectMapper.readTree(cityResponse).get("id").asText());

        String theaterResponse = mockMvc.perform(post("/api/v1/admin/theaters")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateTheaterRequest(cityId, "PVR Phoenix", "Lower Parel"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID theaterId = UUID.fromString(objectMapper.readTree(theaterResponse).get("id").asText());

        String screenResponse = mockMvc.perform(post("/api/v1/admin/screens")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateScreenRequest(theaterId, "Screen 1", 5, 10))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID screenId = UUID.fromString(objectMapper.readTree(screenResponse).get("id").asText());

        CreateSeatLayoutRequest seatLayout = new CreateSeatLayoutRequest(List.of(
                new CreateSeatLayoutRequest.SeatDefinition("A", 1, SeatTier.REGULAR),
                new CreateSeatLayoutRequest.SeatDefinition("A", 2, SeatTier.PREMIUM)
        ));

        mockMvc.perform(post("/api/v1/admin/screens/{screenId}/seats", screenId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(seatLayout)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)));

        String movieResponse = mockMvc.perform(post("/api/v1/admin/movies")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateMovieRequest("Inception", "Mind-bending thriller", 148, "English"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID movieId = UUID.fromString(objectMapper.readTree(movieResponse).get("id").asText());

        CreateShowRequest showRequest = new CreateShowRequest(
                movieId,
                screenId,
                Instant.now().plus(2, ChronoUnit.DAYS),
                new BigDecimal("200.00"),
                new BigDecimal("350.00"),
                new BigDecimal("1.25")
        );

        mockMvc.perform(post("/api/v1/admin/shows")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(showRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalSeats").value(2))
                .andExpect(jsonPath("$.movieTitle").value("Inception"));

        CreateRefundPolicyRequest refundPolicyRequest = new CreateRefundPolicyRequest(
                theaterId,
                "Standard Policy",
                true,
                List.of(
                        new CreateRefundPolicyRequest.RefundPolicyTierRequest(new BigDecimal("24"), new BigDecimal("100"), 1),
                        new CreateRefundPolicyRequest.RefundPolicyTierRequest(new BigDecimal("2"), new BigDecimal("50"), 2),
                        new CreateRefundPolicyRequest.RefundPolicyTierRequest(new BigDecimal("0"), new BigDecimal("0"), 3)
                )
        );

        mockMvc.perform(post("/api/v1/admin/refund-policies")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundPolicyRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tiers", hasSize(3)));

        CreateDiscountCodeRequest discountRequest = new CreateDiscountCodeRequest(
                "WEEKEND20",
                DiscountType.PERCENTAGE,
                new BigDecimal("20"),
                new BigDecimal("100.00"),
                new BigDecimal("500.00"),
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().plus(30, ChronoUnit.DAYS),
                100,
                true
        );

        mockMvc.perform(post("/api/v1/admin/discount-codes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("WEEKEND20"));

        mockMvc.perform(get("/api/v1/admin/cities")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    void customerCannotCreateCity() throws Exception {
        JsonNode registerResponse = objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.movieticket.web.dto.auth.RegisterRequest(
                                        "catalog-customer@test.com", "password123", "Customer"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        String customerToken = registerResponse.get("token").asText();

        mockMvc.perform(post("/api/v1/admin/cities")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCityRequest("Delhi", "Asia/Kolkata"))))
                .andExpect(status().isForbidden());
    }
}
