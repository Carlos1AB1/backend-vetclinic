package com.vetclinic.patterns.chain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ValidationHandlerTest {

    private ValidationHandler<String> handler;
    private ValidationHandler<String> nextHandler;

    @BeforeEach
    void setUp() {
        handler = new ValidationHandler<String>() {
            @Override
            public ValidationResult validate(String request) {
                if (request == null) {
                    return ValidationResult.failure("Request cannot be null");
                }
                return checkNext(request);
            }
        };

        nextHandler = new ValidationHandler<String>() {
            @Override
            public ValidationResult validate(String request) {
                return ValidationResult.success();
            }
        };
    }

    @Test
    void setNext_HandlerChained_Success() {
        // Act
        ValidationHandler<String> result = handler.setNext(nextHandler);

        // Assert
        assertNotNull(result);
        assertEquals(nextHandler, result);
    }

    @Test
    void validate_NullRequest_ReturnsFailure() {
        // Arrange
        handler.setNext(nextHandler);

        // Act
        ValidationResult result = handler.validate(null);

        // Assert
        assertFalse(result.isValid());
        assertEquals("Request cannot be null", result.getMessage());
    }

    @Test
    void validate_ValidRequest_PassesToNextHandler() {
        // Arrange
        handler.setNext(nextHandler);
        String validRequest = "valid";

        // Act
        ValidationResult result = handler.validate(validRequest);

        // Assert
        assertTrue(result.isValid());
    }

    @Test
    void validate_NoNextHandler_ReturnsSuccess() {
        // Arrange
        String validRequest = "valid";

        // Act
        ValidationResult result = handler.validate(validRequest);

        // Assert
        assertTrue(result.isValid());
    }
}
