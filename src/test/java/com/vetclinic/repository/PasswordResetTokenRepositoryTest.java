package com.vetclinic.repository;

import com.vetclinic.entity.PasswordResetToken;
import com.vetclinic.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("PasswordResetTokenRepository Tests")
class PasswordResetTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    private User testUser;
    private PasswordResetToken activeToken;
    private PasswordResetToken usedToken;
    private PasswordResetToken expiredToken;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();

        testUser = User.builder()
                .username("testtokenuser")
                .email("testtokenuser@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .roles(new HashSet<>())
                .build();
        testUser = entityManager.persist(testUser);

        // Token activo y sin usar
        activeToken = PasswordResetToken.builder()
                .token("test-active-token-123")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .createdAt(LocalDateTime.now())
                .build();
        activeToken = entityManager.persist(activeToken);

        // Token usado (no expirado)
        usedToken = PasswordResetToken.builder()
                .token("test-used-token-456")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .usedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
        usedToken = entityManager.persist(usedToken);

        // Token expirado Y usado (para evitar conflicto con findByUserAndUsedAtIsNull)
        expiredToken = PasswordResetToken.builder()
                .token("test-expired-token-789")
                .user(testUser)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .usedAt(LocalDateTime.now().minusHours(30)) // ← AGREGADO: marcado como usado
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
        expiredToken = entityManager.persist(expiredToken);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find token by token string")
    void shouldFindTokenByTokenString() {
        Optional<PasswordResetToken> foundToken = tokenRepository.findByToken("test-active-token-123");

        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getToken()).isEqualTo("test-active-token-123");
        assertThat(foundToken.get().getUser().getUsername()).isEqualTo("testtokenuser");
        assertThat(foundToken.get().getUsedAt()).isNull();
    }

    @Test
    @DisplayName("Should return empty when token string not found")
    void shouldReturnEmptyWhenTokenStringNotFound() {
        Optional<PasswordResetToken> foundToken = tokenRepository.findByToken("nonexistent-token");

        assertThat(foundToken).isEmpty();
    }

    @Test
    @DisplayName("Should find unused token by user")
    void shouldFindUnusedTokenByUser() {
        Optional<PasswordResetToken> foundToken = tokenRepository.findByUserAndUsedAtIsNull(testUser);

        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getToken()).isEqualTo("test-active-token-123");
        assertThat(foundToken.get().getUsedAt()).isNull();
    }

    @Test
    @DisplayName("Should delete expired tokens")
    void shouldDeleteExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        tokenRepository.deleteExpiredTokens(now);
        tokenRepository.flush();

        Optional<PasswordResetToken> expiredTokenCheck = tokenRepository.findByToken("test-expired-token-789");
        Optional<PasswordResetToken> activeTokenCheck = tokenRepository.findByToken("test-active-token-123");

        assertThat(expiredTokenCheck).isEmpty();
        assertThat(activeTokenCheck).isPresent();
    }

    @Test
    @DisplayName("Should delete all tokens by user")
    void shouldDeleteAllTokensByUser() {
        tokenRepository.deleteByUser(testUser);
        tokenRepository.flush();

        Optional<PasswordResetToken> activeTokenCheck = tokenRepository.findByToken("test-active-token-123");
        Optional<PasswordResetToken> usedTokenCheck = tokenRepository.findByToken("test-used-token-456");
        Optional<PasswordResetToken> expiredTokenCheck = tokenRepository.findByToken("test-expired-token-789");

        assertThat(activeTokenCheck).isEmpty();
        assertThat(usedTokenCheck).isEmpty();
        assertThat(expiredTokenCheck).isEmpty();
    }

    @Test
    @DisplayName("Should save token successfully")
    void shouldSaveTokenSuccessfully() {
        PasswordResetToken newToken = PasswordResetToken.builder()
                .token("test-new-token-999")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .createdAt(LocalDateTime.now())
                .build();

        PasswordResetToken savedToken = tokenRepository.save(newToken);

        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo("test-new-token-999");
    }

    @Test
    @DisplayName("Should update token to mark as used")
    void shouldUpdateTokenToMarkAsUsed() {
        LocalDateTime usedTime = LocalDateTime.now();
        activeToken.setUsedAt(usedTime);

        PasswordResetToken updatedToken = tokenRepository.save(activeToken);

        assertThat(updatedToken.getUsedAt()).isNotNull();
        assertThat(updatedToken.isUsed()).isTrue();
    }

    @Test
    @DisplayName("Should verify token is expired")
    void shouldVerifyTokenIsExpired() {
        Optional<PasswordResetToken> foundToken = tokenRepository.findByToken("test-expired-token-789");

        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().isExpired()).isTrue();
    }

    @Test
    @DisplayName("Should verify token is not expired")
    void shouldVerifyTokenIsNotExpired() {
        Optional<PasswordResetToken> foundToken = tokenRepository.findByToken("test-active-token-123");

        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().isExpired()).isFalse();
    }

    @Test
    @DisplayName("Should delete token successfully")
    void shouldDeleteTokenSuccessfully() {
        tokenRepository.delete(usedToken);
        tokenRepository.flush();

        Optional<PasswordResetToken> foundToken = tokenRepository.findByToken("test-used-token-456");

        assertThat(foundToken).isEmpty();
    }
}