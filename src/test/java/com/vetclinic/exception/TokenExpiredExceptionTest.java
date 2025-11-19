package com.vetclinic.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TokenExpiredException Tests")
class TokenExpiredExceptionTest {

    @Test
    @DisplayName("Debe crear excepción con mensaje")
    void shouldCreateExceptionWithMessage() {
        // Arrange
        String message = "JWT token has expired";

        // Act
        TokenExpiredException exception = new TokenExpiredException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debe heredar de RuntimeException")
    void shouldExtendRuntimeException() {
        // Arrange & Act
        TokenExpiredException exception = new TokenExpiredException("Test");

        // Assert
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("Debe poder ser lanzada y capturada")
    void shouldBeThrowableAndCatchable() {
        // Arrange & Act & Assert
        assertThrows(TokenExpiredException.class, () -> {
            throw new TokenExpiredException("Token expired");
        });
    }

    @Test
    @DisplayName("Debe mantener el mensaje original sin modificación")
    void shouldMaintainOriginalMessageWithoutModification() {
        // Arrange
        String message = "Refresh token has expired";

        // Act
        TokenExpiredException exception = new TokenExpiredException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debe manejar mensaje vacío")
    void shouldHandleEmptyMessage() {
        // Arrange
        String message = "";

        // Act
        TokenExpiredException exception = new TokenExpiredException(message);

        // Assert
        assertNotNull(exception);
        assertEquals("", exception.getMessage());
    }

    @Test
    @DisplayName("Debe manejar mensaje null")
    void shouldHandleNullMessage() {
        // Arrange
        String message = null;

        // Act
        TokenExpiredException exception = new TokenExpiredException(message);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Debe usar en contexto de JWT expirado")
    void shouldUseInExpiredJWTContext() {
        // Arrange
        String message = "JWT token has expired at 2024-01-15T10:30:00Z";

        // Act & Assert
        TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> {
            throw new TokenExpiredException(message);
        });

        assertTrue(exception.getMessage().contains("JWT"));
        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    @DisplayName("Debe usar en contexto de refresh token expirado")
    void shouldUseInExpiredRefreshTokenContext() {
        // Arrange
        String message = "Refresh token has expired. Please login again";

        // Act & Assert
        TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> {
            throw new TokenExpiredException(message);
        });

        assertTrue(exception.getMessage().contains("Refresh token"));
    }

    @Test
    @DisplayName("Debe usar en contexto de token de reset password expirado")
    void shouldUseInExpiredPasswordResetTokenContext() {
        // Arrange
        String message = "Password reset token has expired";

        // Act & Assert
        TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> {
            throw new TokenExpiredException(message);
        });

        assertTrue(exception.getMessage().contains("Password reset"));
        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    @DisplayName("Debe manejar mensaje con timestamp detallado")
    void shouldHandleMessageWithDetailedTimestamp() {
        // Arrange
        String message = "Token expired at 2024-01-15T10:30:00.123456Z. Current time: 2024-01-15T11:00:00.000000Z";

        // Act
        TokenExpiredException exception = new TokenExpiredException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().contains("2024-01-15"));
    }

    @Test
    @DisplayName("Debe manejar mensaje con duración de expiración")
    void shouldHandleMessageWithExpirationDuration() {
        // Arrange
        String message = "Token expired 2 hours ago. Maximum lifetime is 24 hours";

        // Act
        TokenExpiredException exception = new TokenExpiredException(message);

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("hours"));
        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    @DisplayName("Debe usar en contexto de verificación de email expirada")
    void shouldUseInExpiredEmailVerificationContext() {
        // Arrange
        String message = "Email verification token has expired. Please request a new one";

        // Act & Assert
        TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> {
            throw new TokenExpiredException(message);
        });

        assertTrue(exception.getMessage().contains("Email verification"));
    }

    @Test
    @DisplayName("Debe manejar mensaje con información de usuario")
    void shouldHandleMessageWithUserInformation() {
        // Arrange
        String userId = "user-123";
        String message = String.format("Token for user %s has expired", userId);

        // Act
        TokenExpiredException exception = new TokenExpiredException(message);

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(userId));
    }

    @Test
    @DisplayName("Debe manejar mensaje con sugerencia de acción")
    void shouldHandleMessageWithActionSuggestion() {
        // Arrange
        String message = "Your session has expired. Please login again to continue";

        // Act
        TokenExpiredException exception = new TokenExpiredException(message);

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("login again"));
    }

    @Test
    @DisplayName("Debe manejar mensaje con código de error")
    void shouldHandleMessageWithErrorCode() {
        // Arrange
        String message = "TOKEN_EXPIRED: The authentication token has expired [Error Code: AUTH-401-E01]";

        // Act
        TokenExpiredException exception = new TokenExpiredException(message);

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("TOKEN_EXPIRED"));
        assertTrue(exception.getMessage().contains("AUTH-401-E01"));
    }

    @Test
    @DisplayName("Debe usar en contexto de token de API expirado")
    void shouldUseInExpiredAPITokenContext() {
        // Arrange
        String message = "API access token has expired. Validity period: 1 hour";

        // Act & Assert
        TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> {
            throw new TokenExpiredException(message);
        });

        assertTrue(exception.getMessage().contains("API access token"));
    }

    @Test
    @DisplayName("Debe manejar mensaje largo con múltiples detalles")
    void shouldHandleLongMessageWithMultipleDetails() {
        // Arrange
        String message = "Authentication token has expired. Token was issued at 2024-01-15T09:00:00Z, " +
                "expired at 2024-01-15T10:00:00Z, current time is 2024-01-15T11:30:00Z. " +
                "Please obtain a new token by logging in again";

        // Act
        TokenExpiredException exception = new TokenExpiredException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().length() > 100);
    }

    @Test
    @DisplayName("Debe manejar mensaje con formato JSON")
    void shouldHandleMessageWithJSONFormat() {
        // Arrange
        String message = "{\"error\": \"token_expired\", \"expired_at\": \"2024-01-15T10:00:00Z\"}";

        // Act
        TokenExpiredException exception = new TokenExpiredException(message);

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("token_expired"));
    }

    @Test
    @DisplayName("Debe manejar mensaje con caracteres especiales")
    void shouldHandleMessageWithSpecialCharacters() {
        // Arrange
        String message = "Token expired! @#$% Time: 10:30:00 (UTC+0)";

        // Act
        TokenExpiredException exception = new TokenExpiredException(message);

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("@#$%"));
    }

    @Test
    @DisplayName("Debe ser serializable")
    void shouldBeSerializable() {
        // Arrange
        TokenExpiredException exception = new TokenExpiredException("Test");

        // Assert
        assertInstanceOf(RuntimeException.class, exception);
        assertInstanceOf(Exception.class, exception);
    }

    @Test
    @DisplayName("Debe manejar mensaje en diferentes idiomas")
    void shouldHandleMessageInDifferentLanguages() {
        // Arrange
        String message = "Token expirado - Token expired - トークンの有効期限が切れました";

        // Act
        TokenExpiredException exception = new TokenExpiredException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debe usar en contexto de autenticación de dos factores")
    void shouldUseInTwoFactorAuthContext() {
        // Arrange
        String message = "Two-factor authentication code has expired. Please request a new code";

        // Act & Assert
        TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> {
            throw new TokenExpiredException(message);
        });

        assertTrue(exception.getMessage().contains("Two-factor"));
    }
}