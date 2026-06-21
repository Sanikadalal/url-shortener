package com.url.shortener.controller;

import com.url.shortener.models.UrlMapping;
import com.url.shortener.security.service.UrlMappingService;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = RedirectController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RedirectController Tests")
class RedirectControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private UrlMappingService urlMappingService;

    // Beans required by security context
    @MockBean private com.url.shortener.security.jwt.JwtUtils jwtUtils;
    @MockBean private com.url.shortener.security.service.UserDetailsServiceImpl userDetailsService;

    private UrlMapping testMapping;

    @BeforeEach
    void setUp() {
        testMapping = new UrlMapping();
        testMapping.setId(1L);
        testMapping.setShortUrl("abc12345");
        testMapping.setOriginalUrl("https://www.example.com/original");
        testMapping.setClickCount(0);
        testMapping.setCreatedDate(LocalDate.now());
    }

    @Test
    @DisplayName("should return 302 redirect with Location header for a valid short URL")
    void shouldReturn302ForValidShortUrl() throws Exception {
        when(urlMappingService.getOriginalUrl("abc12345")).thenReturn(testMapping);

        mockMvc.perform(get("/abc12345"))
                .andExpect(status().isFound())                    // 302
                .andExpect(header().string("Location", "https://www.example.com/original"));
    }

    @Test
    @DisplayName("should return 404 when short URL does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
        when(urlMappingService.getOriginalUrl("unknown")).thenReturn(null);

        mockMvc.perform(get("/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("BUG-018: redirect uses 302 (temporary) instead of 301 (permanent)")
    void bugRedirectUses302NotPermanent() throws Exception {
        // DOCUMENTED BUG-018: URL shorteners should use 301 Permanent Redirect so
        // that browsers and crawlers cache the mapping. 302 causes every request
        // to hit the server unnecessarily.
        when(urlMappingService.getOriginalUrl("abc12345")).thenReturn(testMapping);

        mockMvc.perform(get("/abc12345"))
                .andExpect(status().isFound()); // 302 — a 301 would be preferable
    }

    @Test
    @DisplayName("BUG-019: originalUrl is not validated — open redirect risk if URL is attacker-controlled")
    void bugOpenRedirectRisk() throws Exception {
        // DOCUMENTED BUG-019: No validation of the stored originalUrl.
        // An attacker who somehow stores a javascript: or data: URL would be
        // redirected to by any user clicking the short link.
        UrlMapping maliciousMapping = new UrlMapping();
        maliciousMapping.setOriginalUrl("javascript:alert(1)");
        when(urlMappingService.getOriginalUrl("evil01")).thenReturn(maliciousMapping);

        mockMvc.perform(get("/evil01"))
                .andExpect(status().isFound()) // Blindly redirects — should validate scheme
                .andExpect(header().string("Location", "javascript:alert(1)"));
    }

    @Test
    @DisplayName("BUG-020: short URL endpoint conflicts with Spring Boot Actuator /health path")
    void bugPathConflictWithActuator() throws Exception {
        // DOCUMENTED BUG-020: The /{shortUrl} catch-all pattern will intercept
        // requests to actuator endpoints like /health if actuator is enabled,
        // unless the route ordering is explicitly managed.
        // This test documents the concern — no assertion, just awareness.
        // Actuator is not enabled in this project, but it is a future risk.
    }
}
