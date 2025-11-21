package com.vetclinic.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ResourceNotFoundException Tests")
class ResourceNotFoundExceptionTest {

    @Test
    @DisplayName("Debe crear excepción con mensaje simple")
    void shouldCreateExceptionWithSimpleMessage() {
        // Arrange
        String message = "Resource not found";

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

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
        String fieldName = "id";
        Object fieldValue = 123L;

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(
                resourceName,
                fieldName,
                fieldValue
        );

        // Assert
        assertNotNull(exception);
        String expectedMessage = "User not found with id: '123'";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Debe crear excepción con diferentes tipos de fieldValue")
    void shouldCreateExceptionWithDifferentFieldValueTypes() {
        // Test con String
        ResourceNotFoundException exception1 = new ResourceNotFoundException(
                "Product",
                "name",
                "Laptop"
        );
        assertTrue(exception1.getMessage().contains("Laptop"));

        // Test con Integer
        ResourceNotFoundException exception2 = new ResourceNotFoundException(
                "Order",
                "number",
                12345
        );
        assertTrue(exception2.getMessage().contains("12345"));

        // Test con UUID
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        ResourceNotFoundException exception3 = new ResourceNotFoundException(
                "Session",
                "token",
                uuid
        );
        assertTrue(exception3.getMessage().contains(uuid));
    }

    @Test
    @DisplayName("Debe heredar de RuntimeException")
    void shouldExtendRuntimeException() {
        // Arrange & Act
        ResourceNotFoundException exception = new ResourceNotFoundException("Test");

        // Assert
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("Debe poder ser lanzada y capturada")
    void shouldBeThrowableAndCatchable() {
        // Arrange & Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            throw new ResourceNotFoundException("Test exception");
        });
    }

    @Test
    @DisplayName("Debe crear mensaje formateado correctamente con espacios")
    void shouldFormatMessageCorrectlyWithSpaces() {
        // Arrange
        String resourceName = "Patient Record";
        String fieldName = "patient id";
        Long fieldValue = 999L;

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(
                resourceName,
                fieldName,
                fieldValue
        );

        // Assert
        String expectedMessage = "Patient Record not found with patient id: '999'";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Debe manejar fieldValue null")
    void shouldHandleNullFieldValue() {
        // Arrange
        String resourceName = "User";
        String fieldName = "email";
        Object fieldValue = null;

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(
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
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

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
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Debe crear mensaje con caracteres especiales")
    void shouldCreateMessageWithSpecialCharacters() {
        // Arrange
        String resourceName = "User@Profile";
        String fieldName = "user-name";
        String fieldValue = "test_user#123";

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(
                resourceName,
                fieldName,
                fieldValue
        );

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("User@Profile"));
        assertTrue(exception.getMessage().contains("user-name"));
        assertTrue(exception.getMessage().contains("test_user#123"));
    }

    @Test
    @DisplayName("Debe mantener el formato del mensaje en diferentes idiomas")
    void shouldMaintainMessageFormatInDifferentLanguages() {
        // Arrange
        String resourceName = "Usuario";
        String fieldName = "correo";
        String fieldValue = "test@example.com";

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(
                resourceName,
                fieldName,
                fieldValue
        );

        // Assert
        String expectedMessage = "Usuario not found with correo: 'test@example.com'";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Debe funcionar con resourceName largo")
    void shouldWorkWithLongResourceName() {
        // Arrange
        String resourceName = "VeryLongResourceNameThatExceedsTypicalLength";
        String fieldName = "id";
        Long fieldValue = 1L;

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(
                resourceName,
                fieldName,
                fieldValue
        );

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(resourceName));
    }

    @Test
    @DisplayName("Debe ser serializable")
    void shouldBeSerializable() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Test");

        // Assert - verificar que es una RuntimeException que es serializable
        assertInstanceOf(RuntimeException.class, exception);
        assertInstanceOf(Exception.class, exception);
    }
}