package com.vetclinic.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenTest {

    private PasswordResetToken token;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();

        token = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .token("reset-token-123")
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testPasswordResetTokenBuilder() {
        assertNotNull(token);
        assertEquals("reset-token-123", token.getToken());
        assertEquals(user, token.getUser());
        assertNotNull(token.getExpiresAt());
        assertNotNull(token.getCreatedAt());
        assertNull(token.getUsedAt());
    }

    @Test
    void testIsExpired_NotExpired() {
        PasswordResetToken futureToken = PasswordResetToken.builder()
                .token("future-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        assertFalse(futureToken.isExpired());
    }

    @Test
    void testIsExpired_Expired() {
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .token("expired-token")
                .user(user)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        assertTrue(expiredToken.isExpired());
    }

    @Test
    void testIsUsed_NotUsed() {
        assertFalse(token.isUsed());
    }

    @Test
    void testIsUsed_Used() {
        token.setUsedAt(LocalDateTime.now());
        assertTrue(token.isUsed());
    }

    @Test
    void testSettersAndGetters() {
        UUID newId = UUID.randomUUID();
        token.setId(newId);
        assertEquals(newId, token.getId());

        token.setToken("new-token");
        assertEquals("new-token", token.getToken());

        User newUser = User.builder()
                .id(UUID.randomUUID())
                .username("newuser")
                .build();
        token.setUser(newUser);
        assertEquals(newUser, token.getUser());

        LocalDateTime newExpiry = LocalDateTime.now().plusDays(1);
        token.setExpiresAt(newExpiry);
        assertEquals(newExpiry, token.getExpiresAt());

        LocalDateTime usedTime = LocalDateTime.now();
        token.setUsedAt(usedTime);
        assertEquals(usedTime, token.getUsedAt());

        LocalDateTime createdTime = LocalDateTime.now().minusDays(1);
        token.setCreatedAt(createdTime);
        assertEquals(createdTime, token.getCreatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        PasswordResetToken emptyToken = new PasswordResetToken();
        assertNotNull(emptyToken);
        assertNull(emptyToken.getId());
        assertNull(emptyToken.getToken());
        assertNull(emptyToken.getUser());
    }

    @Test
    void testAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        LocalDateTime usedAt = LocalDateTime.now();
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);

        PasswordResetToken fullToken = new PasswordResetToken(
                id,
                "full-token",
                user,
                expiresAt,
                usedAt,
                createdAt
        );

        assertNotNull(fullToken);
        assertEquals(id, fullToken.getId());
        assertEquals("full-token", fullToken.getToken());
        assertEquals(user, fullToken.getUser());
        assertEquals(expiresAt, fullToken.getExpiresAt());
        assertEquals(usedAt, fullToken.getUsedAt());
        assertEquals(createdAt, fullToken.getCreatedAt());
    }

    @Test
    void testDefaultCreatedAt() {
        PasswordResetToken tokenWithDefaults = PasswordResetToken.builder()
                .token("test-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        assertNotNull(tokenWithDefaults.getCreatedAt());
        assertTrue(tokenWithDefaults.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}