package com.vikrant.urlshortener.repository;

import com.vikrant.urlshortener.entity.Url;
import com.vikrant.urlshortener.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
@SpringBootTest
class UrlRepositoryTest {

    @Autowired
    private UrlRepository urlRepository;
    @Autowired
    private ClickEventRepository clickEventRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanup() {
        clickEventRepository.deleteAll();
        urlRepository.deleteAll();
        userRepository.deleteAll();
    }
    private User createTestUser() {

        User user = new User();

        user.setEmail("test@example.com");
        user.setPasswordHash("hashed-password");

        return userRepository.save(user);
    }

    @Test
    void shouldSaveAndFindUrlByShortCode() {

        // Arrange
        User user = createTestUser();

        Url url = new Url();

        url.setOriginalUrl("https://example.com");
        url.setShortCode("abc123");
        url.setUser(user);

        // Act
        Url savedUrl = urlRepository.save(url);

        Optional<Url> result =
                urlRepository.findByShortCode("abc123");

        // Assert
        assertThat(savedUrl.getId()).isNotNull();

        assertThat(result).isPresent();

        assertThat(result.get().getOriginalUrl())
                .isEqualTo("https://example.com");

        assertThat(result.get().getShortCode())
                .isEqualTo("abc123");

//        assertThat(result.get().getUser().getEmail())
//                .isEqualTo("test@example.com");
    }

    @Test
    void shouldRejectDuplicateShortCode() {

        // Arrange
        User user = createTestUser();

        Url firstUrl = new Url();
        firstUrl.setOriginalUrl("https://example.com/first");
        firstUrl.setShortCode("duplicate123");
        firstUrl.setUser(user);

        Url secondUrl = new Url();
        secondUrl.setOriginalUrl("https://example.com/second");
        secondUrl.setShortCode("duplicate123");
        secondUrl.setUser(user);

        // Act
        urlRepository.saveAndFlush(firstUrl);

        // Assert
        assertThatThrownBy(() ->
                urlRepository.saveAndFlush(secondUrl)
        )
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
