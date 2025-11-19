package com.vetclinic.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UnauthorizedException Tests")
class UnauthorizedExceptionTest {

    @Test
    @DisplayName("Debe crear excepción con mensaje")
    void shouldCreateExceptionWithMessage() {
        // Arrange
        String message = "User is not authorized";

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debe heredar de RuntimeException")
    void shouldExtendRuntimeException() {
        // Arrange & Act
        UnauthorizedException exception = new UnauthorizedException("Test");

        // Assert
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("Debe poder ser lanzada y capturada")
    void shouldBeThrowableAndCatchable() {
        // Arrange & Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            throw new UnauthorizedException("Unauthorized access");
        });
    }

    @Test
    @DisplayName("Debe mantener el mensaje original sin modificación")
    void shouldMaintainOriginalMessageWithoutModification() {
        // Arrange
        String message = "JWT token is invalid";

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debe manejar mensaje vacío")
    void shouldHandleEmptyMessage() {
        // Arrange
        String message = "";

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

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
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Debe usar en contexto de JWT inválido")
    void shouldUseInInvalidJWTContext() {
        // Arrange
        String message = "JWT token is invalid or expired";

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            throw new UnauthorizedException(message);
        });

        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().contains("JWT"));
    }

    @Test
    @DisplayName("Debe usar en contexto de token expirado")
    void shouldUseInExpiredTokenContext() {
        // Arrange
        String message = "Authentication token has expired";

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            throw new UnauthorizedException(message);
        });

        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    @DisplayName("Debe usar en contexto de credenciales inválidas")
    void shouldUseInInvalidCredentialsContext() {
        // Arrange
        String message = "Invalid username or password";

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            throw new UnauthorizedException(message);
        });

        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debe usar en contexto de token faltante")
    void shouldUseInMissingTokenContext() {
        // Arrange
        String message = "Authorization token is missing";

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            throw new UnauthorizedException(message);
        });

        assertTrue(exception.getMessage().contains("missing"));
    }

    @Test
    @DisplayName("Debe usar en contexto de sesión expirada")
    void shouldUseInExpiredSessionContext() {
        // Arrange
        String message = "Your session has expired. Please login again";

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            throw new UnauthorizedException(message);
        });

        assertTrue(exception.getMessage().contains("session"));
        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    @DisplayName("Debe manejar mensaje con detalles técnicos")
    void shouldHandleMessageWithTechnicalDetails() {
        // Arrange
        String message = "Unauthorized: Missing Bearer token in Authorization header";

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().contains("Bearer"));
        assertTrue(exception.getMessage().contains("Authorization"));
    }

    @Test
    @DisplayName("Debe manejar mensaje con información de usuario")
    void shouldHandleMessageWithUserInformation() {
        // Arrange
        String username = "john.doe";
        String message = String.format("User '%s' is not authorized to perform this action", username);

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(username));
    }

    @Test
    @DisplayName("Debe manejar mensaje con timestamp")
    void shouldHandleMessageWithTimestamp() {
        // Arrange
        String message = "Token expired at 2024-01-15T10:30:00Z";

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().contains("2024-01-15"));
    }

    @Test
    @DisplayName("Debe usar en contexto de permisos insuficientes")
    void shouldUseInInsufficientPermissionsContext() {
        // Arrange
        String message = "User does not have required permissions";

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            throw new UnauthorizedException(message);
        });

        assertTrue(exception.getMessage().contains("permissions"));
    }

    @Test
    @DisplayName("Debe manejar mensaje con caracteres especiales")
    void shouldHandleMessageWithSpecialCharacters() {
        // Arrange
        String message = "Invalid token: @#$%^&*()";

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().contains("@#$%^&*()"));
    }

    @Test
    @DisplayName("Debe manejar mensaje largo")
    void shouldHandleLongMessage() {
        // Arrange
        String message = "Authorization failed due to multiple reasons including invalid token signature, " +
                "expired timestamp, missing required claims, and insufficient permissions for the requested resource";

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().length() > 100);
    }

    @Test
    @DisplayName("Debe usar en contexto de refresh token inválido")
    void shouldUseInInvalidRefreshTokenContext() {
        // Arrange
        String message = "Refresh token is invalid or has been revoked";

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            throw new UnauthorizedException(message);
        });

        assertTrue(exception.getMessage().contains("Refresh token"));
        assertTrue(exception.getMessage().contains("revoked"));
    }

    @Test
    @DisplayName("Debe usar en contexto de cuenta bloqueada")
    void shouldUseInAccountLockedContext() {
        // Arrange
        String message = "Account is locked due to multiple failed login attempts";

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            throw new UnauthorizedException(message);
        });

        assertTrue(exception.getMessage().contains("locked"));
        assertTrue(exception.getMessage().contains("failed login"));
    }

    @Test
    @DisplayName("Debe ser serializable")
    void shouldBeSerializable() {
        // Arrange
        UnauthorizedException exception = new UnauthorizedException("Test");

        // Assert
        assertInstanceOf(RuntimeException.class, exception);
        assertInstanceOf(Exception.class, exception);
    }

    @Test
    @DisplayName("Debe manejar mensaje con formato JSON")
    void shouldHandleMessageWithJSONFormat() {
        // Arrange
        String message = "{\"error\": \"unauthorized\", \"reason\": \"token_expired\"}";

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    @DisplayName("Debe manejar mensaje multiidioma")
    void shouldHandleMultiLanguageMessage() {
        // Arrange
        String message = "No autorizado - Unauthorized - 未授权";

        // Act
        UnauthorizedException exception = new UnauthorizedException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }
}