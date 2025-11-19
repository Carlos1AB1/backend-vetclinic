package com.vetclinic.patterns.chain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {

    @Test
    void success_CreatesValidResult() {
        // Act
        ValidationResult result = ValidationResult.success();

        // Assert
        assertTrue(result.isValid());
        assertNull(result.getMessage());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void failure_CreatesInvalidResultWithMessage() {
        // Arrange
        String errorMessage = "Validation failed";

        // Act
        ValidationResult result = ValidationResult.failure(errorMessage);

        // Assert
        assertFalse(result.isValid());
        assertEquals(errorMessage, result.getMessage());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void failure_WithMessageAndErrors_CreatesInvalidResult() {
        // Arrange
        String errorMessage = "Multiple errors found";
        List<String> errors = Arrays.asList("Error 1", "Error 2");

        // Act
        ValidationResult result = ValidationResult.failure(errorMessage, errors);

        // Assert
        assertFalse(result.isValid());
        assertEquals(errorMessage, result.getMessage());
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getErrors().containsAll(errors));
    }

    @Test
    void addError_AddsErrorToList() {
        // Arrange
        ValidationResult result = ValidationResult.failure("Initial message");
        String newError = "Additional error";

        // Act
        result.addError(newError);

        // Assert
        assertEquals(1, result.getErrors().size());
        assertEquals(newError, result.getErrors().get(0));
    }

    @Test
    void addError_MultipleErrors_AllAdded() {
        // Arrange
        ValidationResult result = ValidationResult.failure("Initial message");

        // Act
        result.addError("Error 1");
        result.addError("Error 2");
        result.addError("Error 3");

        // Assert
        assertEquals(3, result.getErrors().size());
    }

    @Test
    void addError_NullErrorsList_InitializesAndAdds() {
        // Arrange
        ValidationResult result = ValidationResult.builder()
                .valid(false)
                .message("Test")
                .build();

        // Act
        result.addError("New error");

        // Assert
        assertNotNull(result.getErrors());
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void isValid_SuccessResult_ReturnsTrue() {
        // Arrange
        ValidationResult result = ValidationResult.success();

        // Act & Assert
        assertTrue(result.isValid());
    }

    @Test
    void isValid_FailureResult_ReturnsFalse() {
        // Arrange
        ValidationResult result = ValidationResult.failure("Error");

        // Act & Assert
        assertFalse(result.isValid());
    }
}
