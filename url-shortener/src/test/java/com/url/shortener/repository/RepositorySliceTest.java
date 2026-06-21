package com.url.shortener.repository;

import com.url.shortener.models.ClickEvent;
import com.url.shortener.models.UrlMapping;
import com.url.shortener.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Repository slice tests using an embedded H2 database.
 * Requires h2 on the test classpath (added via spring-boot-starter-test).
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Repository Slice Tests")
class RepositorySliceTest {

    @Autowired private UserRepository userRepository;
    @Autowired private UrlMappingRepository urlMappingRepository;
    @Autowired private ClickEventRepository clickEventRepository;

    private User savedUser;
    private UrlMapping savedMapping;

    @BeforeEach
    void setUp() {
        // Persist a user
        User user = new User();
        user.setUsername("sliceuser");
        user.setEmail("slice@test.com");
        user.setPassword("encoded");
        user.setRole("ROLE_USER");
        savedUser = userRepository.save(user);

        // Persist a url mapping
        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl("https://example.com/original");
        mapping.setShortUrl("slice001");
        mapping.setClickCount(0);
        mapping.setCreatedDate(LocalDate.now());
        mapping.setUser(savedUser);
        savedMapping = urlMappingRepository.save(mapping);

        // Persist click events
        ClickEvent e1 = new ClickEvent();
        e1.setUrlMapping(savedMapping);
        e1.setClickDate(LocalDateTime.of(2026, 6, 10, 10, 0));

        ClickEvent e2 = new ClickEvent();
        e2.setUrlMapping(savedMapping);
        e2.setClickDate(LocalDateTime.of(2026, 6, 11, 9, 0));

        clickEventRepository.saveAll(List.of(e1, e2));
    }

    // ──────────────────────────────────────────────────────────────
    // UserRepository
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UserRepository: findByUsername returns correct user")
    void findByUsername_returnsUser() {
        var result = userRepository.findByUsername("sliceuser");
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("slice@test.com");
    }

    @Test
    @DisplayName("UserRepository: findByUsername returns empty for unknown user")
    void findByUsername_returnsEmpty() {
        assertThat(userRepository.findByUsername("ghost")).isEmpty();
    }

    // ──────────────────────────────────────────────────────────────
    // UrlMappingRepository
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UrlMappingRepository: findByShortUrl returns correct mapping")
    void findByShortUrl_returnsMapping() {
        UrlMapping result = urlMappingRepository.findByShortUrl("slice001");
        assertThat(result).isNotNull();
        assertThat(result.getOriginalUrl()).isEqualTo("https://example.com/original");
    }

    @Test
    @DisplayName("UrlMappingRepository: findByShortUrl returns null for unknown code")
    void findByShortUrl_returnsNull() {
        UrlMapping result = urlMappingRepository.findByShortUrl("xxxxxxxx");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("UrlMappingRepository: findByUser returns all URLs for that user")
    void findByUser_returnsAllMappings() {
        List<UrlMapping> result = urlMappingRepository.findByUser(savedUser);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getShortUrl()).isEqualTo("slice001");
    }

    @Test
    @DisplayName("UrlMappingRepository: findByUser returns empty list for user with no URLs")
    void findByUser_returnsEmptyList() {
        User anotherUser = new User();
        anotherUser.setUsername("nobody");
        anotherUser.setEmail("no@no.com");
        anotherUser.setPassword("x");
        anotherUser.setRole("ROLE_USER");
        User savedOther = userRepository.save(anotherUser);

        List<UrlMapping> result = urlMappingRepository.findByUser(savedOther);
        assertThat(result).isEmpty();
    }

    // ──────────────────────────────────────────────────────────────
    // ClickEventRepository
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ClickEventRepository: findByUrlMappingAndClickDateBetween returns events in range")
    void findByUrlMappingAndClickDateBetween_returnsInRange() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 10, 0, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 6, 10, 23, 59);

        List<ClickEvent> result = clickEventRepository
                .findByUrlMappingAndClickDateBetween(savedMapping, start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClickDate().toLocalDate())
                .isEqualTo(LocalDate.of(2026, 6, 10));
    }

    @Test
    @DisplayName("ClickEventRepository: findByUrlMappingAndClickDateBetween returns all events in wide range")
    void findByUrlMappingAndClickDateBetween_returnsAllInWideRange() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 12, 31, 23, 59);

        List<ClickEvent> result = clickEventRepository
                .findByUrlMappingAndClickDateBetween(savedMapping, start, end);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("ClickEventRepository: findByUrlMappingInAndClickDateBetween aggregates across multiple URLs")
    void findByUrlMappingInAndClickDateBetween_aggregatesMultiple() {
        // Add a second mapping and click
        UrlMapping mapping2 = new UrlMapping();
        mapping2.setOriginalUrl("https://another.com");
        mapping2.setShortUrl("slice002");
        mapping2.setCreatedDate(LocalDate.now());
        mapping2.setUser(savedUser);
        UrlMapping saved2 = urlMappingRepository.save(mapping2);

        ClickEvent e = new ClickEvent();
        e.setUrlMapping(saved2);
        e.setClickDate(LocalDateTime.of(2026, 6, 10, 12, 0));
        clickEventRepository.save(e);

        LocalDateTime start = LocalDateTime.of(2026, 6, 1, 0, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 6, 30, 23, 59);

        List<ClickEvent> result = clickEventRepository
                .findByUrlMappingInAndClickDateBetween(List.of(savedMapping, saved2), start, end);

        // 1 from savedMapping (June 10) + 1 from mapping2 (June 10) + 1 from savedMapping (June 11)
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("ClickEventRepository: returns empty list when no events in range")
    void findByUrlMappingAndClickDateBetween_returnsEmptyOutOfRange() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end   = LocalDateTime.of(2025, 12, 31, 23, 59);

        List<ClickEvent> result = clickEventRepository
                .findByUrlMappingAndClickDateBetween(savedMapping, start, end);

        assertThat(result).isEmpty();
    }
}
