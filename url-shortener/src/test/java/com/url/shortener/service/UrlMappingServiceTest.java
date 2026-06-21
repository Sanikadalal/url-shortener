package com.url.shortener.service;

import com.url.shortener.dtos.ClickEventDTO;
import com.url.shortener.dtos.UrlMappingDTO;
import com.url.shortener.models.ClickEvent;
import com.url.shortener.models.UrlMapping;
import com.url.shortener.models.User;
import com.url.shortener.repository.ClickEventRepository;
import com.url.shortener.repository.UrlMappingRepository;
import com.url.shortener.security.service.UrlMappingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UrlMappingService Tests")
class UrlMappingServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @Mock
    private ClickEventRepository clickEventRepository;

    @InjectMocks
    private UrlMappingService urlMappingService;

    private User testUser;
    private UrlMapping testUrlMapping;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("ROLE_USER");

        testUrlMapping = new UrlMapping();
        testUrlMapping.setId(1L);
        testUrlMapping.setOriginalUrl("https://www.example.com/some/very/long/url");
        testUrlMapping.setShortUrl("abc12345");
        testUrlMapping.setClickCount(0);
        testUrlMapping.setCreatedDate(LocalDate.now());
        testUrlMapping.setUser(testUser);
    }

    // ──────────────────────────────────────────────────────────────
    // createShortUrl
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createShortUrl()")
    class CreateShortUrl {

        @Test
        @DisplayName("should persist a new UrlMapping and return a DTO")
        void shouldPersistAndReturnDto() {
            when(urlMappingRepository.save(any(UrlMapping.class))).thenReturn(testUrlMapping);

            UrlMappingDTO result = urlMappingService.createShortUrl("https://www.example.com/some/very/long/url", testUser);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getOriginalUrl()).isEqualTo("https://www.example.com/some/very/long/url");
            assertThat(result.getShortUrl()).isEqualTo("abc12345");
            assertThat(result.getUsername()).isEqualTo("testuser");
            verify(urlMappingRepository, times(1)).save(any(UrlMapping.class));
        }

        @Test
        @DisplayName("BUG-001: createShortUrl does NOT set createdDate — saved entity has null createdDate")
        void bugCreatedDateIsNullOnSave() {
            // DOCUMENTED BUG: UrlMappingService.createShortUrl() never calls
            // urlMapping.setCreatedDate(LocalDate.now()), so the createdDate
            // column in the DB will always be null unless Hibernate @PrePersist is used.
            ArgumentCaptor<UrlMapping> captor = ArgumentCaptor.forClass(UrlMapping.class);
            when(urlMappingRepository.save(any(UrlMapping.class))).thenReturn(testUrlMapping);

            urlMappingService.createShortUrl("https://example.com", testUser);

            verify(urlMappingRepository).save(captor.capture());
            UrlMapping saved = captor.getValue();
            // This assertion WILL FAIL until the bug is fixed.
            assertThat(saved.getCreatedDate())
                    .as("BUG-001: createdDate must be set before saving")
                    .isNotNull();
        }

        @Test
        @DisplayName("should generate a short URL of exactly 8 characters")
        void shouldGenerateShortUrlOfCorrectLength() {
            ArgumentCaptor<UrlMapping> captor = ArgumentCaptor.forClass(UrlMapping.class);
            when(urlMappingRepository.save(any(UrlMapping.class))).thenReturn(testUrlMapping);

            urlMappingService.createShortUrl("https://example.com", testUser);

            verify(urlMappingRepository).save(captor.capture());
            assertThat(captor.getValue().getShortUrl())
                    .hasSize(8)
                    .matches("[A-Za-z0-9]{8}");
        }

        @Test
        @DisplayName("BUG-002: no duplicate-short-URL check — collision is possible")
        void bugNoDuplicateShortUrlCheck() {
            // DOCUMENTED BUG: generateShortUrl() uses Random (not SecureRandom) and
            // does not check whether the generated code already exists in the DB.
            // Two calls could theoretically produce the same 8-char code.
            when(urlMappingRepository.save(any(UrlMapping.class))).thenReturn(testUrlMapping);

            UrlMappingDTO first  = urlMappingService.createShortUrl("https://a.com", testUser);
            UrlMappingDTO second = urlMappingService.createShortUrl("https://b.com", testUser);

            // The service NEVER queries the repo before saving, so uniqueness is not guaranteed.
            verify(urlMappingRepository, never()).findByShortUrl(anyString());
        }

        @Test
        @DisplayName("BUG-003: null originalUrl is not validated — NullPointerException risk")
        void bugNullOriginalUrlNotValidated() {
            when(urlMappingRepository.save(any(UrlMapping.class))).thenReturn(testUrlMapping);

            // Should either throw an IllegalArgumentException or return a 400.
            // Currently the service passes null straight to the entity.
            assertThatCode(() -> urlMappingService.createShortUrl(null, testUser))
                    .doesNotThrowAnyException(); // passes — but it SHOULD throw
        }
    }

    // ──────────────────────────────────────────────────────────────
    // getUrlsByUser
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getUrlsByUser()")
    class GetUrlsByUser {

        @Test
        @DisplayName("should return all DTOs for the given user")
        void shouldReturnAllDtosForUser() {
            when(urlMappingRepository.findByUser(testUser)).thenReturn(List.of(testUrlMapping));

            List<UrlMappingDTO> result = urlMappingService.getUrlsByUser(testUser);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getShortUrl()).isEqualTo("abc12345");
        }

        @Test
        @DisplayName("should return empty list when user has no URLs")
        void shouldReturnEmptyListWhenNoUrls() {
            when(urlMappingRepository.findByUser(testUser)).thenReturn(Collections.emptyList());

            List<UrlMappingDTO> result = urlMappingService.getUrlsByUser(testUser);

            assertThat(result).isEmpty();
        }
    }

    // ──────────────────────────────────────────────────────────────
    // getOriginalUrl
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getOriginalUrl()")
    class GetOriginalUrl {

        @Test
        @DisplayName("should increment clickCount and save a ClickEvent")
        void shouldIncrementClickCountAndSaveClickEvent() {
            testUrlMapping.setClickCount(5);
            when(urlMappingRepository.findByShortUrl("abc12345"))
                    .thenReturn(testUrlMapping)   // first call — found
                    .thenReturn(testUrlMapping);  // second call — return result

            urlMappingService.getOriginalUrl("abc12345");

            ArgumentCaptor<UrlMapping> urlCaptor = ArgumentCaptor.forClass(UrlMapping.class);
            verify(urlMappingRepository).save(urlCaptor.capture());
            assertThat(urlCaptor.getValue().getClickCount()).isEqualTo(6);

            ArgumentCaptor<ClickEvent> eventCaptor = ArgumentCaptor.forClass(ClickEvent.class);
            verify(clickEventRepository).save(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getClickDate()).isNotNull();
            assertThat(eventCaptor.getValue().getUrlMapping()).isEqualTo(testUrlMapping);
        }

        @Test
        @DisplayName("BUG-004: getOriginalUrl calls findByShortUrl TWICE — double DB hit")
        void bugDoubleDbHitOnGetOriginalUrl() {
            when(urlMappingRepository.findByShortUrl("abc12345")).thenReturn(testUrlMapping);

            urlMappingService.getOriginalUrl("abc12345");

            // The method queries the DB twice — once for the update, once to return
            // the result. The second query is redundant; the already-saved entity
            // should be returned directly.
            verify(urlMappingRepository, times(2)).findByShortUrl("abc12345");
        }

        @Test
        @DisplayName("should return null when shortUrl does not exist")
        void shouldReturnNullWhenNotFound() {
            when(urlMappingRepository.findByShortUrl("unknown")).thenReturn(null);

            UrlMapping result = urlMappingService.getOriginalUrl("unknown");

            assertThat(result).isNull();
            verify(urlMappingRepository, never()).save(any());
            verify(clickEventRepository, never()).save(any());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // getClickEventsByDate
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getClickEventsByDate()")
    class GetClickEventsByDate {

        @Test
        @DisplayName("should group click events by date and return correct counts")
        void shouldGroupClickEventsByDate() {
            ClickEvent event1 = new ClickEvent();
            event1.setClickDate(LocalDateTime.of(2026, 6, 1, 10, 0));
            event1.setUrlMapping(testUrlMapping);

            ClickEvent event2 = new ClickEvent();
            event2.setClickDate(LocalDateTime.of(2026, 6, 1, 14, 0));
            event2.setUrlMapping(testUrlMapping);

            ClickEvent event3 = new ClickEvent();
            event3.setClickDate(LocalDateTime.of(2026, 6, 2, 9, 0));
            event3.setUrlMapping(testUrlMapping);

            when(urlMappingRepository.findByShortUrl("abc12345")).thenReturn(testUrlMapping);
            when(clickEventRepository.findByUrlMappingAndClickDateBetween(
                    eq(testUrlMapping), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(event1, event2, event3));

            LocalDateTime start = LocalDateTime.of(2026, 6, 1, 0, 0);
            LocalDateTime end   = LocalDateTime.of(2026, 6, 30, 23, 59);

            List<ClickEventDTO> result = urlMappingService.getClickEventsByDate("abc12345", start, end);

            assertThat(result).hasSize(2);
            ClickEventDTO june1 = result.stream()
                    .filter(d -> d.getClickDate().equals(LocalDate.of(2026, 6, 1)))
                    .findFirst().orElseThrow();
            assertThat(june1.getClickCount()).isEqualTo(2L);
        }

        @Test
        @DisplayName("BUG-005: returns null instead of empty list when shortUrl not found")
        void bugReturnsNullInsteadOfEmptyList() {
            when(urlMappingRepository.findByShortUrl("none")).thenReturn(null);

            List<ClickEventDTO> result = urlMappingService.getClickEventsByDate(
                    "none",
                    LocalDateTime.now().minusDays(7),
                    LocalDateTime.now());

            // DOCUMENTED BUG: the method returns null when the URL is not found.
            // It should return Collections.emptyList() to avoid NullPointerException
            // in callers that iterate the result.
            assertThat(result)
                    .as("BUG-005: should return empty list, not null")
                    .isNotNull();
        }
    }

    // ──────────────────────────────────────────────────────────────
    // getTotalClicksByUserAndDate
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getTotalClicksByUserAndDate()")
    class GetTotalClicksByUserAndDate {

        @Test
        @DisplayName("should aggregate total clicks across all user URLs by date")
        void shouldAggregateTotalClicksByDate() {
            UrlMapping mapping2 = new UrlMapping();
            mapping2.setId(2L);
            mapping2.setShortUrl("xyz98765");
            mapping2.setUser(testUser);

            ClickEvent e1 = new ClickEvent();
            e1.setClickDate(LocalDateTime.of(2026, 6, 10, 10, 0));
            e1.setUrlMapping(testUrlMapping);

            ClickEvent e2 = new ClickEvent();
            e2.setClickDate(LocalDateTime.of(2026, 6, 10, 15, 0));
            e2.setUrlMapping(mapping2);

            ClickEvent e3 = new ClickEvent();
            e3.setClickDate(LocalDateTime.of(2026, 6, 11, 9, 0));
            e3.setUrlMapping(testUrlMapping);

            when(urlMappingRepository.findByUser(testUser)).thenReturn(List.of(testUrlMapping, mapping2));
            when(clickEventRepository.findByUrlMappingInAndClickDateBetween(
                    anyList(), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(e1, e2, e3));

            LocalDate start = LocalDate.of(2026, 6, 1);
            LocalDate end   = LocalDate.of(2026, 6, 30);

            Map<LocalDate, Long> result = urlMappingService.getTotalClicksByUserAndDate(testUser, start, end);

            assertThat(result).containsEntry(LocalDate.of(2026, 6, 10), 2L);
            assertThat(result).containsEntry(LocalDate.of(2026, 6, 11), 1L);
        }

        @Test
        @DisplayName("should return empty map when user has no URLs")
        void shouldReturnEmptyMapWhenNoUrls() {
            when(urlMappingRepository.findByUser(testUser)).thenReturn(Collections.emptyList());
            when(clickEventRepository.findByUrlMappingInAndClickDateBetween(
                    anyList(), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            Map<LocalDate, Long> result = urlMappingService.getTotalClicksByUserAndDate(
                    testUser, LocalDate.now().minusDays(7), LocalDate.now());

            assertThat(result).isEmpty();
        }
    }
}
