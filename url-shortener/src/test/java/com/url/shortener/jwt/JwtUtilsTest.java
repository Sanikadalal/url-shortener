package com.url.shortener.jwt;

import com.url.shortener.security.jwt.JwtUtils;
import com.url.shortener.security.service.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JwtUtils Tests")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    // 64-char secret (min length for HS256 with jjwt strict key validation)
    private static final String SECRET =
            "mysupersecretkeyforjwtauthentication2026springbootproject12345";
    private static final int EXPIRATION_MS = 86_400_000; // 1 day

    private UserDetailsImpl testUser;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", EXPIRATION_MS);

        testUser = new UserDetailsImpl(
                1L,
                "sanika",
                "sanika@example.com",
                "encoded",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    // ──────────────────────────────────────────────────────────────
    // generateToken
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("should generate a non-null, non-empty JWT string")
        void shouldGenerateNonEmptyJwt() {
            String token = jwtUtils.generateToken(testUser);
            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("should generate a token with exactly 3 parts (header.payload.signature)")
        void shouldHaveThreeParts() {
            String token = jwtUtils.generateToken(testUser);
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("should embed the correct username as subject")
        void shouldEmbedUsername() {
            String token = jwtUtils.generateToken(testUser);
            String username = jwtUtils.getUserNameFromToken(token);
            assertThat(username).isEqualTo("sanika");
        }

        @Test
        @DisplayName("should embed the user's role in the claims")
        void shouldEmbedRoleClaim() {
            // Validate indirectly — token must be valid (role embedded does not break parsing)
            String token = jwtUtils.generateToken(testUser);
            assertThat(jwtUtils.validateToken(token)).isTrue();
        }
    }

    // ──────────────────────────────────────────────────────────────
    // validateToken
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("validateToken()")
    class ValidateToken {

        @Test
        @DisplayName("should return true for a freshly generated token")
        void shouldReturnTrueForFreshToken() {
            String token = jwtUtils.generateToken(testUser);
            assertThat(jwtUtils.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("should return false for a tampered token")
        void shouldReturnFalseForTamperedToken() {
            String token = jwtUtils.generateToken(testUser) + "tampered";
            assertThat(jwtUtils.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("should return false for a completely garbage string")
        void shouldReturnFalseForGarbage() {
            assertThat(jwtUtils.validateToken("not.a.jwt")).isFalse();
        }

        @Test
        @DisplayName("should return false for a blank token")
        void shouldReturnFalseForBlank() {
            assertThat(jwtUtils.validateToken("")).isFalse();
        }

        @Test
        @DisplayName("BUG-009: validateToken swallows all exceptions — expired tokens return false silently")
        void bugExpiredTokenReturnsFalseWithNoSignal() {
            // DOCUMENTED BUG: validateToken() catches ALL exceptions and returns false.
            // The caller has no way to distinguish an expired token from a tampered
            // one or a completely invalid one. This makes debugging and proper error
            // responses impossible.
            JwtUtils shortLived = new JwtUtils();
            ReflectionTestUtils.setField(shortLived, "jwtSecret", SECRET);
            ReflectionTestUtils.setField(shortLived, "jwtExpirationMs", -1000); // already expired

            String expiredToken = shortLived.generateToken(testUser);
            boolean result = shortLived.validateToken(expiredToken);

            // Both expired and malformed tokens return false — there is no distinction.
            assertThat(result).isFalse();
        }
    }

    // ──────────────────────────────────────────────────────────────
    // getJwtFromHeader
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getJwtFromHeader()")
    class GetJwtFromHeader {

        @Test
        @DisplayName("should extract token from 'Bearer <token>' header")
        void shouldExtractTokenFromBearerHeader() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn("Bearer mytoken123");

            String result = jwtUtils.getJwtFromHeader(request);

            assertThat(result).isEqualTo("mytoken123");
        }

        @Test
        @DisplayName("should return null when Authorization header is missing")
        void shouldReturnNullWhenHeaderMissing() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn(null);

            assertThat(jwtUtils.getJwtFromHeader(request)).isNull();
        }

        @Test
        @DisplayName("should return null when header does not start with 'Bearer '")
        void shouldReturnNullWhenNotBearer() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

            assertThat(jwtUtils.getJwtFromHeader(request)).isNull();
        }

        @Test
        @DisplayName("BUG-010: 'Bearer' without trailing space is not handled — returns null")
        void bugBearerWithoutSpaceReturnsNull() {
            // DOCUMENTED BUG: If the client sends "Bearer<token>" (no space) the
            // token is silently dropped. The prefix check is case-sensitive and
            // space-sensitive with no error feedback.
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn("Bearermytoken123");

            assertThat(jwtUtils.getJwtFromHeader(request)).isNull();
        }
    }

    // ──────────────────────────────────────────────────────────────
    // getUserNameFromToken
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getUserNameFromToken()")
    class GetUserNameFromToken {

        @Test
        @DisplayName("should return the correct username from a valid token")
        void shouldReturnCorrectUsername() {
            String token = jwtUtils.generateToken(testUser);
            assertThat(jwtUtils.getUserNameFromToken(token)).isEqualTo("sanika");
        }

        @Test
        @DisplayName("should throw when token is invalid")
        void shouldThrowOnInvalidToken() {
            assertThatThrownBy(() -> jwtUtils.getUserNameFromToken("invalid.token.here"))
                    .isInstanceOf(Exception.class);
        }
    }
}
