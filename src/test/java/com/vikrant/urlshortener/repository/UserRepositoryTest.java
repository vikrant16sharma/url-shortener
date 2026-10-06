package com.vikrant.urlshortener.repository;

import com.vikrant.urlshortener.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindUserByEmail() {

        // Arrange
        User user = new User();

        user.setEmail("test@example.com");
        user.setPasswordHash("hashed-password");

        // Act
        userRepository.save(user);

        Optional<User> result =
                userRepository.findByEmail("test@example.com");

        // Assert
        assertThat(result).isPresent();

        assertThat(result.get().getEmail())
                .isEqualTo("test@example.com");

        assertThat(result.get().getPasswordHash())
                .isEqualTo("hashed-password");
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {

        // Arrange
        User user = new User();

        user.setEmail("test@example.com");
        user.setPasswordHash("hashed-password");

        userRepository.save(user);

        // Act
        boolean exists =
                userRepository.existsByEmail("test@example.com");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {

        // Act
        boolean exists =
                userRepository.existsByEmail(
                        "unknown@example.com"
                );

        // Assert
        assertThat(exists).isFalse();
    }
}