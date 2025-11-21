package com.vetclinic.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BusinessException Tests")
class BusinessExceptionTest {

    @Test
    @DisplayName("Debe crear excepción con mensaje")
    void shouldCreateExceptionWithMessage() {
        // Arrange
        String message = "Business rule validation failed";

        // Act
        BusinessException exception = new BusinessException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debe crear excepción con mensaje y causa")
    void shouldCreateExceptionWithMessageAndCause() {
        // Arrange
        String message = "Failed to process business logic";
        Throwable cause = new IllegalArgumentException("Invalid argument");

        // Act
        BusinessException exception = new BusinessException(message, cause);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    @DisplayName("Debe heredar de RuntimeException")
    void shouldExtendRuntimeException() {
        // Arrange & Act
        BusinessException exception = new BusinessException("Test");

        // Assert
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("Debe poder ser lanzada y capturada")
    void shouldBeThrowableAndCatchable() {
        // Arrange & Act & Assert
        assertThrows(BusinessException.class, () -> {
            throw new BusinessException("Business error");
        });
    }

    @Test
    @DisplayName("Debe poder ser lanzada y capturada con causa")
    void shouldBeThrowableAndCatchableWithCause() {
        // Arrange & Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            throw new BusinessException("Business error", new RuntimeException("Cause"));
        });

        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("Debe mantener el mensaje original sin modificación")
    void shouldMaintainOriginalMessageWithoutModification() {
        // Arrange
        String message = "Appointment cannot be scheduled in the past";

        // Act
        BusinessException exception = new BusinessException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Debe manejar mensaje vacío")
    void shouldHandleEmptyMessage() {
        // Arrange
        String message = "";

        // Act
        BusinessException exception = new BusinessException(message);

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
        BusinessException exception = new BusinessException(message);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Debe manejar causa null")
    void shouldHandleNullCause() {
        // Arrange
        String message = "Business error";
        Throwable cause = null;

        // Act
        BusinessException exception = new BusinessException(message, cause);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Debe usar en contexto de reglas de negocio")
    void shouldUseInBusinessRuleContext() {
        // Arrange
        String message = "Cannot schedule appointment: Veterinarian is not available at this time";

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            throw new BusinessException(message);
        });

        assertTrue(exception.getMessage().contains("Cannot schedule"));
    }

    @Test
    @DisplayName("Debe usar en contexto de validación de citas")
    void shouldUseInAppointmentValidationContext() {
        // Arrange
        String message = "Appointment duration must be between 15 and 120 minutes";

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            throw new BusinessException(message);
        });

        assertTrue(exception.getMessage().contains("Appointment duration"));
    }

    @Test
    @DisplayName("Debe usar en contexto de inventario")
    void shouldUseInInventoryContext() {
        // Arrange
        String message = "Insufficient stock: Cannot sell more items than available";

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            throw new BusinessException(message);
        });

        assertTrue(exception.getMessage().contains("Insufficient stock"));
    }

    @Test
    @DisplayName("Debe usar en contexto de pagos")
    void shouldUseInPaymentContext() {
        // Arrange
        String message = "Payment amount cannot be negative";

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            throw new BusinessException(message);
        });

        assertTrue(exception.getMessage().contains("Payment amount"));
    }

    @Test
    @DisplayName("Debe envolver excepciones de capa inferior")
    void shouldWrapLowerLayerExceptions() {
        // Arrange
        String message = "Failed to process appointment";
        IOException cause = new IOException("Database connection failed");

        // Act
        BusinessException exception = new BusinessException(message, cause);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertInstanceOf(IOException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Database"));
    }

    @Test
    @DisplayName("Debe encadenar múltiples causas")
    void shouldChainMultipleCauses() {
        // Arrange
        RuntimeException rootCause = new RuntimeException("Root cause");
        IllegalStateException intermediateCause = new IllegalStateException("Intermediate", rootCause);
        String message = "Business operation failed";

        // Act
        BusinessException exception = new BusinessException(message, intermediateCause);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }

    @Test
    @DisplayName("Debe manejar mensaje con detalles de validación")
    void shouldHandleMessageWithValidationDetails() {
        // Arrange
        String message = "Invalid patient age: must be between 0 and 50 years for this species";

        // Act
        BusinessException exception = new BusinessException(message);

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Invalid patient age"));
    }

    @Test
    @DisplayName("Debe usar en contexto de horario de negocio")
    void shouldUseInBusinessHoursContext() {
        // Arrange
        String message = "Cannot schedule appointment outside business hours (9:00 AM - 6:00 PM)";

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            throw new BusinessException(message);
        });

        assertTrue(exception.getMessage().contains("business hours"));
    }

    @Test
    @DisplayName("Debe usar en contexto de capacidad")
    void shouldUseInCapacityContext() {
        // Arrange
        String message = "Maximum daily appointments limit reached (50 appointments)";

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            throw new BusinessException(message);
        });

        assertTrue(exception.getMessage().contains("Maximum daily appointments"));
    }

    @Test
    @DisplayName("Debe manejar mensaje largo con múltiples reglas")
    void shouldHandleLongMessageWithMultipleRules() {
        // Arrange
        String message = "Business validation failed: " +
                "1) Patient must have an active owner, " +
                "2) Veterinarian must be qualified for this procedure, " +
                "3) Required equipment must be available";

        // Act
        BusinessException exception = new BusinessException(message);

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Business validation failed"));
        assertTrue(exception.getMessage().length() > 100);
    }

    @Test
    @DisplayName("Debe usar en contexto de estado de entidad")
    void shouldUseInEntityStateContext() {
        // Arrange
        String message = "Cannot delete patient: Patient has active appointments";

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            throw new BusinessException(message);
        });

        assertTrue(exception.getMessage().contains("Cannot delete patient"));
    }

    @Test
    @DisplayName("Debe usar en contexto de dependencias")
    void shouldUseInDependencyContext() {
        // Arrange
        String message = "Cannot deactivate owner: Owner has registered patients";

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            throw new BusinessException(message);
        });

        assertTrue(exception.getMessage().contains("Cannot deactivate"));
    }

    @Test
    @DisplayName("Debe manejar mensaje con formato JSON")
    void shouldHandleMessageWithJSONFormat() {
        // Arrange
        String message = "{\"rule\": \"appointment_overlap\", \"conflict_time\": \"10:00-11:00\"}";

        // Act
        BusinessException exception = new BusinessException(message);

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("appointment_overlap"));
    }

    @Test
    @DisplayName("Debe ser serializable")
    void shouldBeSerializable() {
        // Arrange
        BusinessException exception = new BusinessException("Test");

        // Assert
        assertInstanceOf(RuntimeException.class, exception);
        assertInstanceOf(Exception.class, exception);
    }

    @Test
    @DisplayName("Debe manejar mensaje con caracteres especiales")
    void shouldHandleMessageWithSpecialCharacters() {
        // Arrange
        String message = "Invalid operation: Price must be > $0 and < $10,000";

        // Act
        BusinessException exception = new BusinessException(message);

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("$"));
        assertTrue(exception.getMessage().contains(">"));
    }

    @Test
    @DisplayName("Debe usar en contexto de fecha y hora")
    void shouldUseInDateTimeContext() {
        // Arrange
        String message = "Appointment date must be at least 24 hours from now";

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            throw new BusinessException(message);
        });

        assertTrue(exception.getMessage().contains("24 hours"));
    }

    @Test
    @DisplayName("Debe preservar stack trace con causa")
    void shouldPreserveStackTraceWithCause() {
        // Arrange
        String message = "Business operation failed";
        RuntimeException cause = new RuntimeException("Database error");

        // Act
        BusinessException exception = new BusinessException(message, cause);

        // Assert
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
        assertNotNull(exception.getCause().getStackTrace());
    }
}