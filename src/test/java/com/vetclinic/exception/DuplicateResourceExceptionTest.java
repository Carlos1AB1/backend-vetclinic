package com.vetclinic.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DuplicateResourceException Tests")
class DuplicateResourceExceptionTest {

    @Test
    @DisplayName("Debe crear excepción con mensaje simple")
    void shouldCreateExceptionWithSimpleMessage() {
        // Arrange
        String message = "Resource already exists";

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("Debe crear excepción con constructor de tres parámetros")
    void shouldCreateExceptionWithThreeParameters() {
        // Arrange
        String resourceName = "User";
        String fieldName = "email";
        Object fieldValue = "test@example.com";

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(
                resourceName,
                fieldName,
                fieldValue
        );

        // Assert
        assertNotNull(exception);
        String expectedMessage = "User already exists with email: 'test@example.com'";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Debe crear excepción con diferentes tipos de fieldValue")
    void shouldCreateExceptionWithDifferentFieldValueTypes() {
        // Test con String
        DuplicateResourceException exception1 = new DuplicateResourceException(
                "Owner",
                "email",
                "owner@example.com"
        );
        assertTrue(exception1.getMessage().contains("owner@example.com"));

        // Test con Integer
        DuplicateResourceException exception2 = new DuplicateResourceException(
                "Product",
                "code",
                12345
        );
        assertTrue(exception2.getMessage().contains("12345"));

        // Test con Long
        DuplicateResourceException exception3 = new DuplicateResourceException(
                "Patient",
                "microchip",
                9876543210L
        );
        assertTrue(exception3.getMessage().contains("9876543210"));
    }

    @Test
    @DisplayName("Debe heredar de RuntimeException")
    void shouldExtendRuntimeException() {
        // Arrange & Act
        DuplicateResourceException exception = new DuplicateResourceException("Test");

        // Assert
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("Debe poder ser lanzada y capturada")
    void shouldBeThrowableAndCatchable() {
        // Arrange & Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            throw new DuplicateResourceException("Duplicate resource");
        });
    }

    @Test
    @DisplayName("Debe crear mensaje formateado correctamente")
    void shouldFormatMessageCorrectly() {
        // Arrange
        String resourceName = "Appointment";
        String fieldName = "scheduleId";
        String fieldValue = "APT-2024-001";

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(
                resourceName,
                fieldName,
                fieldValue
        );

        // Assert
        String expectedMessage = "Appointment already exists with scheduleId: 'APT-2024-001'";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Debe manejar fieldValue null")
    void shouldHandleNullFieldValue() {
        // Arrange
        String resourceName = "Owner";
        String fieldName = "documentNumber";
        Object fieldValue = null;

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(
                resourceName,
                fieldName,
                fieldValue
        );

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    @DisplayName("Debe manejar mensaje vacío")
    void shouldHandleEmptyMessage() {
        // Arrange
        String message = "";

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(message);

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
        DuplicateResourceException exception = new DuplicateResourceException(message);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Debe usar en contexto de email duplicado")
    void shouldUseInDuplicateEmailContext() {
        // Arrange
        String message = "Ya existe un propietario con el email: owner@example.com";

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            throw new DuplicateResourceException(message);
        });

        assertTrue(exception.getMessage().contains("email"));
        assertTrue(exception.getMessage().contains("owner@example.com"));
    }

    @Test
    @DisplayName("Debe usar en contexto de username duplicado")
    void shouldUseInDuplicateUsernameContext() {
        // Arrange
        DuplicateResourceException exception = new DuplicateResourceException(
                "User",
                "username",
                "john_doe"
        );

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("User already exists"));
        assertTrue(exception.getMessage().contains("username"));
        assertTrue(exception.getMessage().contains("john_doe"));
    }

    @Test
    @DisplayName("Debe usar en contexto de documento duplicado")
    void shouldUseInDuplicateDocumentContext() {
        // Arrange
        String message = "Ya existe un propietario con el número de documento: 12345678";

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            throw new DuplicateResourceException(message);
        });

        assertTrue(exception.getMessage().contains("documento"));
        assertTrue(exception.getMessage().contains("12345678"));
    }

    @Test
    @DisplayName("Debe usar en contexto de código duplicado")
    void shouldUseInDuplicateCodeContext() {
        // Arrange
        DuplicateResourceException exception = new DuplicateResourceException(
                "Product",
                "code",
                "PROD-001"
        );

        // Assert
        assertNotNull(exception);
        String expectedMessage = "Product already exists with code: 'PROD-001'";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Debe usar en contexto de microchip duplicado")
    void shouldUseInDuplicateMicrochipContext() {
        // Arrange
        DuplicateResourceException exception = new DuplicateResourceException(
                "Patient",
                "microchipNumber",
                "982000123456789"
        );

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Patient already exists"));
        assertTrue(exception.getMessage().contains("microchipNumber"));
        assertTrue(exception.getMessage().contains("982000123456789"));
    }

    @Test
    @DisplayName("Debe crear mensaje con caracteres especiales en fieldValue")
    void shouldCreateMessageWithSpecialCharactersInFieldValue() {
        // Arrange
        String resourceName = "User";
        String fieldName = "email";
        String fieldValue = "test+user@example.com";

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(
                resourceName,
                fieldName,
                fieldValue
        );

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("+"));
        assertTrue(exception.getMessage().contains("test+user@example.com"));
    }

    @Test
    @DisplayName("Debe mantener el formato del mensaje con espacios")
    void shouldMaintainMessageFormatWithSpaces() {
        // Arrange
        String resourceName = "Medical Record";
        String fieldName = "record number";
        String fieldValue = "MR-2024-001";

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(
                resourceName,
                fieldName,
                fieldValue
        );

        // Assert
        String expectedMessage = "Medical Record already exists with record number: 'MR-2024-001'";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Debe funcionar con resourceName largo")
    void shouldWorkWithLongResourceName() {
        // Arrange
        String resourceName = "VeterinaryAppointmentSchedule";
        String fieldName = "id";
        Long fieldValue = 123L;

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(
                resourceName,
                fieldName,
                fieldValue
        );

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(resourceName));
    }

    @Test
    @DisplayName("Debe manejar fieldValue con UUID")
    void shouldHandleFieldValueWithUUID() {
        // Arrange
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        DuplicateResourceException exception = new DuplicateResourceException(
                "Session",
                "token",
                uuid
        );

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(uuid));
    }

    @Test
    @DisplayName("Debe usar en contexto de validación de creación")
    void shouldUseInCreationValidationContext() {
        // Arrange
        String email = "existing@example.com";

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            // Simular validación de email duplicado
            boolean emailExists = true;
            if (emailExists) {
                throw new DuplicateResourceException("Owner", "email", email);
            }
        });

        assertTrue(exception.getMessage().contains("Owner"));
        assertTrue(exception.getMessage().contains(email));
    }

    @Test
    @DisplayName("Debe manejar mensaje con múltiples idiomas")
    void shouldHandleMessageWithMultipleLanguages() {
        // Arrange
        String message = "Usuario ya existe - User already exists";

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debe ser serializable")
    void shouldBeSerializable() {
        // Arrange
        DuplicateResourceException exception = new DuplicateResourceException("Test");

        // Assert
        assertInstanceOf(RuntimeException.class, exception);
        assertInstanceOf(Exception.class, exception);
    }

    @Test
    @DisplayName("Debe manejar mensaje con números y letras mixtos")
    void shouldHandleMessageWithMixedNumbersAndLetters() {
        // Arrange
        DuplicateResourceException exception = new DuplicateResourceException(
                "Invoice",
                "invoiceNumber",
                "INV-2024-0001"
        );

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("INV-2024-0001"));
    }

    @Test
    @DisplayName("Debe usar en contexto de teléfono duplicado")
    void shouldUseInDuplicatePhoneContext() {
        // Arrange
        DuplicateResourceException exception = new DuplicateResourceException(
                "Owner",
                "phone",
                "+57 300 123 4567"
        );

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("phone"));
        assertTrue(exception.getMessage().contains("+57 300 123 4567"));
    }

    @Test
    @DisplayName("Debe manejar fieldValue con caracteres especiales")
    void shouldHandleFieldValueWithSpecialCharacters() {
        // Arrange
        DuplicateResourceException exception = new DuplicateResourceException(
                "User",
                "username",
                "user@123#test"
        );

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("user@123#test"));
    }

    @Test
    @DisplayName("Debe preservar el caso del fieldValue")
    void shouldPreserveCaseOfFieldValue() {
        // Arrange
        DuplicateResourceException exception = new DuplicateResourceException(
                "User",
                "email",
                "TestUser@Example.COM"
        );

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("TestUser@Example.COM"));
    }
}