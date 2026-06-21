package com.url.shortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.url.shortener.dtos.UrlMappingDTO;
import com.url.shortener.models.User;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import com.url.shortener.security.service.UrlMappingService;
import com.url.shortener.security.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = UrlMappingController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@DisplayName("UrlMappingController Tests")
class UrlMappingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UrlMappingService urlMappingService;
    @MockBean private UserService userService;

    // Beans required by the security context
    @MockBean private com.url.shortener.security.jwt.JwtUtils jwtUtils;
    @MockBean private com.url.shortener.security.service.UserDetailsServiceImpl userDetailsService;

    private User testUser;
    private UrlMappingDTO testDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("sanika");
        testUser.setEmail("sanika@example.com");
        testUser.setPassword("encoded");
        testUser.setRole("ROLE_USER");

        testDto = new UrlMappingDTO();
        testDto.setId(1L);
        testDto.setOriginalUrl("https://www.example.com/long/url");
        testDto.setShortUrl("abc12345");
        testDto.setClickCount(0);
        testDto.setCreatedDate(LocalDate.now());
        testDto.setUsername("sanika");
    }

    // ──────────────────────────────────────────────────────────────
    // POST /api/urls/shorten
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/urls/shorten")
    class ShortenUrl {

        @Test
        @WithMockUser(username = "sanika", roles = "USER")
        @DisplayName("should return 200 with DTO for a valid URL")
        void shouldReturn200WithDto() throws Exception {
            when(userService.findByUsername("sanika")).thenReturn(testUser);
            when(urlMappingService.createShortUrl(anyString(), eq(testUser))).thenReturn(testDto);

            mockMvc.perform(post("/api/urls/shorten")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"originalUrl\":\"https://www.example.com/long/url\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.shortUrl").value("abc12345"))
                    .andExpect(jsonPath("$.originalUrl").value("https://www.example.com/long/url"));
        }

        @Test
        @DisplayName("should return 401/403 when called without authentication")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(post("/api/urls/shorten")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"originalUrl\":\"https://example.com\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "sanika", roles = "USER")
        @DisplayName("BUG-011: empty body does not return 400 — NPE risk in service")
        void bugEmptyBodyNotValidated() throws Exception {
            // DOCUMENTED BUG: The controller reads request.get("originalUrl") without
            // null-checking. An empty JSON body '{}' will produce a null originalUrl
            // passed to the service, which can cause an NPE downstream.
            when(userService.findByUsername("sanika")).thenReturn(testUser);
            when(urlMappingService.createShortUrl(isNull(), eq(testUser))).thenReturn(testDto);

            mockMvc.perform(post("/api/urls/shorten")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    // Should be 400 Bad Request — currently passes to the service with null
                    .andExpect(status().isOk());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/urls/myurls
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/urls/myurls")
    class GetMyUrls {

        @Test
        @WithMockUser(username = "sanika", roles = "USER")
        @DisplayName("should return list of URLs for the authenticated user")
        void shouldReturnUserUrls() throws Exception {
            when(userService.findByUsername("sanika")).thenReturn(testUser);
            when(urlMappingService.getUrlsByUser(testUser)).thenReturn(List.of(testDto));

            mockMvc.perform(get("/api/urls/myurls"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].shortUrl").value("abc12345"));
        }

        @Test
        @WithMockUser(username = "sanika", roles = "USER")
        @DisplayName("should return empty array when user has no URLs")
        void shouldReturnEmptyArray() throws Exception {
            when(userService.findByUsername("sanika")).thenReturn(testUser);
            when(urlMappingService.getUrlsByUser(testUser)).thenReturn(List.of());

            mockMvc.perform(get("/api/urls/myurls"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("should return 401 when called without authentication")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/urls/myurls"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/urls/analytics/{shortUrl}
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/urls/analytics/{shortUrl}")
    class GetAnalytics {

        @Test
        @WithMockUser(username = "sanika", roles = "USER")
        @DisplayName("should return click events for the given short URL and date range")
        void shouldReturnClickEvents() throws Exception {
            com.url.shortener.dtos.ClickEventDTO eventDto = new com.url.shortener.dtos.ClickEventDTO();
            eventDto.setClickDate(LocalDate.of(2026, 6, 1));
            eventDto.setClickCount(5L);

            when(urlMappingService.getClickEventsByDate(
                    eq("abc12345"), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(eventDto));

            mockMvc.perform(get("/api/urls/analytics/abc12345")
                            .param("startDate", "2026-06-01T00:00:00")
                            .param("endDate",   "2026-06-30T23:59:59"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].clickCount").value(5));
        }

        @Test
        @WithMockUser(username = "sanika", roles = "USER")
        @DisplayName("BUG-012: missing startDate param causes 500 instead of 400")
        void bugMissingParamCauses500() throws Exception {
            // DOCUMENTED BUG: The analytics endpoint does not declare params as
            // `required=false` nor does it validate them; a missing startDate /
            // endDate causes a 400 MissingServletRequestParameterException but
            // there is no @ExceptionHandler returning a proper error body.
            mockMvc.perform(get("/api/urls/analytics/abc12345"))
                    .andExpect(status().isBadRequest()); // Spring throws 400 automatically
        }

        @Test
        @WithMockUser(username = "sanika", roles = "USER")
        @DisplayName("BUG-013: no ownership check — any authenticated user can see another user's analytics")
        void bugNoOwnershipCheckOnAnalytics() throws Exception {
            // DOCUMENTED BUG: getUrlAnalytics() does not verify that the shortUrl
            // belongs to the authenticated principal. Any logged-in user can query
            // analytics for any URL.
            com.url.shortener.dtos.ClickEventDTO eventDto = new com.url.shortener.dtos.ClickEventDTO();
            eventDto.setClickDate(LocalDate.now());
            eventDto.setClickCount(99L);

            when(urlMappingService.getClickEventsByDate(anyString(), any(), any()))
                    .thenReturn(List.of(eventDto));

            mockMvc.perform(get("/api/urls/analytics/someoneElsesUrl")
                            .param("startDate", "2026-06-01T00:00:00")
                            .param("endDate",   "2026-06-30T23:59:59"))
                    .andExpect(status().isOk()) // Should be 403 if URL belongs to another user
                    .andExpect(jsonPath("$[0].clickCount").value(99));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/urls/totalClicks
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/urls/totalClicks")
    class GetTotalClicks {

        @Test
        @WithMockUser(username = "sanika", roles = "USER")
        @DisplayName("should return total click map for authenticated user")
        void shouldReturnTotalClicks() throws Exception {
            when(userService.findByUsername("sanika")).thenReturn(testUser);
            when(urlMappingService.getTotalClicksByUserAndDate(eq(testUser), any(), any()))
                    .thenReturn(Map.of(LocalDate.of(2026, 6, 10), 7L));

            mockMvc.perform(get("/api/urls/totalClicks")
                            .param("startDate", "2026-06-01")
                            .param("endDate",   "2026-06-30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$['2026-06-10']").value(7));
        }
    }
}
