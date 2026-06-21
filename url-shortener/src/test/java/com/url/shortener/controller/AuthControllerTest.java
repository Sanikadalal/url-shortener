package com.url.shortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.url.shortener.dtos.LoginRequest;
import com.url.shortener.dtos.RegisterRequest;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import com.url.shortener.models.User;
import com.url.shortener.security.jwt.JwtAuthenticationResponse;
import com.url.shortener.security.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserService userService;

    // Beans required by the security context
    @MockBean private com.url.shortener.security.jwt.JwtUtils jwtUtils;
    @MockBean private com.url.shortener.security.service.UserDetailsServiceImpl userDetailsService;

    // ──────────────────────────────────────────────────────────────
    // POST /api/auth/public/login
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/public/login")
    class Login {

        @Test
        @DisplayName("should return 200 with JWT token on valid credentials")
        void shouldReturn200WithToken() throws Exception {
            when(userService.authenticateUser(any(LoginRequest.class)))
                    .thenReturn(new JwtAuthenticationResponse("jwt.token.here"));

            mockMvc.perform(post("/api/auth/public/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"sanika\",\"password\":\"password\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt.token.here"));
        }

        @Test
        @DisplayName("should propagate 500 on bad credentials (BUG-014: no global exception handler)")
        void bugBadCredentialsReturns500() throws Exception {
            // DOCUMENTED BUG-014: When authenticateUser throws BadCredentialsException,
            // there is no @ControllerAdvice / @ExceptionHandler, so Spring returns
            // a 500 Internal Server Error instead of 401 Unauthorized.
            when(userService.authenticateUser(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> mockMvc.perform(post("/api/auth/public/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"sanika\",\"password\":\"wrong\"}")))
                    .hasCauseInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("BUG-015: empty username/password body returns 500 instead of 400")
        void bugEmptyCredentialsNot400() throws Exception {
            // DOCUMENTED BUG-015: No @Valid / @NotBlank annotations on LoginRequest,
            // so an empty body '{}' passes to the service producing a null username.
            when(userService.authenticateUser(any())).thenThrow(new RuntimeException());

            assertThatThrownBy(() -> mockMvc.perform(post("/api/auth/public/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")))
                    .hasCauseInstanceOf(RuntimeException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // POST /api/auth/public/register
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/public/register")
    class Register {

        @Test
        @DisplayName("should return 200 with success message on valid registration")
        void shouldReturn200OnValidRegistration() throws Exception {
            User savedUser = new User();
            savedUser.setId(1L);
            savedUser.setUsername("sanika");
            when(userService.registerUser(any(User.class))).thenReturn(savedUser);

            mockMvc.perform(post("/api/auth/public/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"sanika\",\"email\":\"s@ex.com\",\"password\":\"pass\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("User registered successfully"));
        }

        @Test
        @DisplayName("BUG-016: registration does not return 201 Created — uses 200 OK")
        void bugRegistrationReturns200NotCreated() throws Exception {
            // DOCUMENTED BUG-016: REST convention requires 201 Created for resource
            // creation. The endpoint returns 200 OK.
            when(userService.registerUser(any(User.class))).thenReturn(new User());

            mockMvc.perform(post("/api/auth/public/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"u\",\"email\":\"e@e.com\",\"password\":\"p\"}"))
                    .andExpect(status().isOk()); // Should be 201
        }

        @Test
        @DisplayName("BUG-017: no input validation on register — blank fields accepted")
        void bugBlankFieldsAccepted() throws Exception {
            // DOCUMENTED BUG-017: RegisterRequest has no @NotBlank/@Email constraints,
            // so empty strings / missing fields are accepted by the controller.
            when(userService.registerUser(any(User.class))).thenReturn(new User());

            mockMvc.perform(post("/api/auth/public/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"\",\"email\":\"\",\"password\":\"\"}"))
                    .andExpect(status().isOk()); // Should be 400
        }
    }
}
