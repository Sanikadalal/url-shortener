package com.url.shortener.service;

import com.url.shortener.dtos.LoginRequest;
import com.url.shortener.dtos.RegisterRequest;
import com.url.shortener.models.User;
import com.url.shortener.repository.UserRepository;
import com.url.shortener.security.jwt.JwtAuthenticationResponse;
import com.url.shortener.security.jwt.JwtUtils;
import com.url.shortener.security.service.UserDetailsImpl;
import com.url.shortener.security.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserRepository userRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("sanika");
        testUser.setEmail("sanika@example.com");
        testUser.setPassword("rawPassword");
        testUser.setRole("ROLE_USER");
    }

    // ──────────────────────────────────────────────────────────────
    // registerUser
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("registerUser()")
    class RegisterUser {

        @Test
        @DisplayName("should encode password before saving the user")
        void shouldEncodePasswordBeforeSaving() {
            when(passwordEncoder.encode("rawPassword")).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.registerUser(testUser);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPassword()).isEqualTo("hashedPassword");
        }

        @Test
        @DisplayName("should return the saved user entity")
        void shouldReturnSavedUser() {
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.registerUser(testUser);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("sanika");
        }

        @Test
        @DisplayName("BUG-006: no duplicate username check before registration")
        void bugNoDuplicateUsernameCheck() {
            // DOCUMENTED BUG: registerUser() never checks if the username already
            // exists in the DB. A second registration with the same username will
            // throw a DB constraint error instead of a clean 409 Conflict response.
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Service never calls findByUsername before saving
            userService.registerUser(testUser);
            verify(userRepository, never()).findByUsername(anyString());
        }

        @Test
        @DisplayName("BUG-007: no email validation — invalid emails are accepted")
        void bugNoEmailValidation() {
            testUser.setEmail("not-an-email");
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Should throw a validation error — currently it passes silently.
            assertThatCode(() -> userService.registerUser(testUser)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("BUG-008: no minimum password length enforcement")
        void bugNoPasswordLengthEnforcement() {
            testUser.setPassword("1"); // too short
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Should reject a 1-character password — currently it passes silently.
            assertThatCode(() -> userService.registerUser(testUser)).doesNotThrowAnyException();
        }
    }

    // ──────────────────────────────────────────────────────────────
    // authenticateUser
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("authenticateUser()")
    class AuthenticateUser {

        @Test
        @DisplayName("should return a JWT token on successful login")
        void shouldReturnJwtTokenOnSuccess() {
            UserDetailsImpl principal = new UserDetailsImpl(
                    1L, "sanika", "sanika@example.com", "hashed", Collections.emptyList());

            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(principal);
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(jwtUtils.generateToken(principal)).thenReturn("jwt.token.here");

            JwtAuthenticationResponse response = userService.authenticateUser(
                    new LoginRequest("sanika", "rawPassword"));

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt.token.here");
        }

        @Test
        @DisplayName("should propagate BadCredentialsException on wrong password")
        void shouldPropagateBadCredentialsException() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> userService.authenticateUser(
                    new LoginRequest("sanika", "wrongPassword")))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // findByUsername
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByUsername()")
    class FindByUsername {

        @Test
        @DisplayName("should return the user when found")
        void shouldReturnUserWhenFound() {
            when(userRepository.findByUsername("sanika")).thenReturn(Optional.of(testUser));

            User result = userService.findByUsername("sanika");

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("sanika");
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findByUsername("ghost"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("ghost");
        }
    }
}
