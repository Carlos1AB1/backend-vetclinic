package com.vetclinic.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BadRequestException Tests")
class BadRequestExceptionTest {

    @Test
    @DisplayName("Debe crear excepción con mensaje")
    void shouldCreateExceptionWithMessage() {
        // Arrange
        String message = "Invalid request parameters";

        // Act
        BadRequestException exception = new BadRequestException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debe heredar de RuntimeException")
    void shouldExtendRuntimeException() {
        // Arrange & Act
        BadRequestException exception = new BadRequestException("Test");

        // Assert
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("Debe poder ser lanzada y capturada")
    void shouldBeThrowableAndCatchable() {
        // Arrange & Act & Assert
        assertThrows(BadRequestException.class, () -> {
            throw new BadRequestException("Invalid data");
        });
    }

    @Test
    @DisplayName("Debe mantener el mensaje original sin modificación")
    void shouldMaintainOriginalMessageWithoutModification() {
        // Arrange
        String message = "Email format is invalid";

        // Act
        BadRequestException exception = new BadRequestException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debe manejar mensaje vacío")
    void shouldHandleEmptyMessage() {
        // Arrange
        String message = "";

        // Act
        BadRequestException exception = new BadRequestException(message);

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
        BadRequestException exception = new BadRequestException(message);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Debe manejar mensaje con caracteres especiales")
    void shouldHandleMessageWithSpecialCharacters() {
        // Arrange
        String message = "Invalid data: {field: 'email', value: 'test@'}";

        // Act
        BadRequestException exception = new BadRequestException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().contains("@"));
        assertTrue(exception.getMessage().contains("{"));
    }

    @Test
    @DisplayName("Debe manejar mensaje muy largo")
    void shouldHandleVeryLongMessage() {
        // Arrange
        String message = "This is a very long error message that exceeds typical length. ".repeat(10);

        // Act
        BadRequestException exception = new BadRequestException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().length() > 100);
    }

    @Test
    @DisplayName("Debe manejar mensaje con saltos de línea")
    void shouldHandleMessageWithLineBreaks() {
        // Arrange
        String message = "Error in line 1\nError in line 2\nError in line 3";

        // Act
        BadRequestException exception = new BadRequestException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().contains("\n"));
    }

    @Test
    @DisplayName("Debe manejar mensaje con tabulaciones")
    void shouldHandleMessageWithTabs() {
        // Arrange
        String message = "Field\tValue\nName\tJohn";

        // Act
        BadRequestException exception = new BadRequestException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().contains("\t"));
    }

    @Test
    @DisplayName("Debe usar en contexto de validación de datos")
    void shouldUseInDataValidationContext() {
        // Arrange
        String email = "invalid-email";
        String message = String.format("Invalid email format: %s", email);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            if (!email.contains("@")) {
                throw new BadRequestException(message);
            }
        });

        assertTrue(exception.getMessage().contains("invalid-email"));
    }

    @Test
    @DisplayName("Debe usar en contexto de parámetros faltantes")
    void shouldUseInMissingParametersContext() {
        // Arrange
        String message = "Required parameter 'userId' is missing";

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            throw new BadRequestException(message);
        });

        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debe usar en contexto de formato incorrecto")
    void shouldUseInIncorrectFormatContext() {
        // Arrange
        String message = "Date format must be yyyy-MM-dd";

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            throw new BadRequestException(message);
        });

        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debe usar en contexto de valores fuera de rango")
    void shouldUseInOutOfRangeContext() {
        // Arrange
        int value = 150;
        String message = String.format("Age value %d is out of valid range (0-120)", value);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            if (value > 120) {
                throw new BadRequestException(message);
            }
        });

        assertTrue(exception.getMessage().contains("150"));
        assertTrue(exception.getMessage().contains("0-120"));
    }

    @Test
    @DisplayName("Debe manejar mensaje con JSON")
    void shouldHandleMessageWithJSON() {
        // Arrange
        String message = "{\"error\": \"Invalid request\", \"field\": \"email\"}";

        // Act
        BadRequestException exception = new BadRequestException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().contains("\"error\""));
    }

    @Test
    @DisplayName("Debe manejar mensaje con comillas simples y dobles")
    void shouldHandleMessageWithQuotes() {
        // Arrange
        String message = "Field 'username' must not contain \" character";

        // Act
        BadRequestException exception = new BadRequestException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().contains("'"));
        assertTrue(exception.getMessage().contains("\""));
    }

    @Test
    @DisplayName("Debe ser serializable")
    void shouldBeSerializable() {
        // Arrange
        BadRequestException exception = new BadRequestException("Test");

        // Assert
        assertInstanceOf(RuntimeException.class, exception);
        assertInstanceOf(Exception.class, exception);
    }

    @Test
    @DisplayName("Debe manejar mensaje con diferentes idiomas")
    void shouldHandleMessageWithDifferentLanguages() {
        // Arrange
        String message = "Solicitud inválida - 無効なリクエスト";

        // Act
        BadRequestException exception = new BadRequestException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }
}