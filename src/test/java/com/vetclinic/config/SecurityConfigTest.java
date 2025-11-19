package com.vetclinic.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void passwordEncoder_IsNotNull() {
        // Assert
        assertNotNull(passwordEncoder);
    }

    @Test
    void passwordEncoder_EncodesPassword() {
        // Arrange
        String rawPassword = "testPassword123";

        // Act
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
    }

    @Test
    void passwordEncoder_MatchesEncodedPassword() {
        // Arrange
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Act
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        // Assert
        assertTrue(matches);
    }

    @Test
    void passwordEncoder_DoesNotMatchDifferentPassword() {
        // Arrange
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Act
        boolean matches = passwordEncoder.matches("differentPassword", encodedPassword);

        // Assert
        assertFalse(matches);
    }
}
