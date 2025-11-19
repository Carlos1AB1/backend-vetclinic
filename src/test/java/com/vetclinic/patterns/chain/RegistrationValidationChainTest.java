package com.vetclinic.patterns.chain;

import com.vetclinic.dto.auth.RegisterRequest;
import com.vetclinic.patterns.chain.validators.EmailUniquenessValidator;
import com.vetclinic.patterns.chain.validators.PasswordStrengthValidator;
import com.vetclinic.patterns.chain.validators.UsernameUniquenessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class RegistrationValidationChainTest {

    @Mock
    private UsernameUniquenessValidator usernameValidator;

    @Mock
    private EmailUniquenessValidator emailValidator;

    @Mock
    private PasswordStrengthValidator passwordValidator;

    @InjectMocks
    private RegistrationValidationChain validationChain;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("SecurePass123!")
                .build();
    }

    @Test
    void validate_AllValidatorsPass_ReturnsSuccess() {
        // Arrange
        when(usernameValidator.setNext(emailValidator)).thenReturn(emailValidator);
        when(emailValidator.setNext(passwordValidator)).thenReturn(passwordValidator);
        when(usernameValidator.validate(any(RegisterRequest.class))).thenReturn(ValidationResult.success());

        // Act
        ValidationResult result = validationChain.validate(validRequest);

        // Assert
        assertTrue(result.isValid());
        verify(usernameValidator).setNext(emailValidator);
        verify(emailValidator).setNext(passwordValidator);
        verify(usernameValidator).validate(validRequest);
    }

    @Test
    void validate_UsernameValidationFails_ReturnsFailure() {
        // Arrange
        when(usernameValidator.setNext(emailValidator)).thenReturn(emailValidator);
        when(emailValidator.setNext(passwordValidator)).thenReturn(passwordValidator);
        when(usernameValidator.validate(any(RegisterRequest.class)))
                .thenReturn(ValidationResult.failure("Username already exists"));

        // Act
        ValidationResult result = validationChain.validate(validRequest);

        // Assert
        assertFalse(result.isValid());
        assertEquals("Username already exists", result.getMessage());
    }

    @Test
    void validate_ChainIsConfiguredCorrectly() {
        // Arrange
        when(usernameValidator.setNext(emailValidator)).thenReturn(emailValidator);
        when(emailValidator.setNext(passwordValidator)).thenReturn(passwordValidator);
        when(usernameValidator.validate(any(RegisterRequest.class))).thenReturn(ValidationResult.success());

        // Act
        validationChain.validate(validRequest);

        // Assert
        var inOrder = inOrder(usernameValidator, emailValidator);
        verify(usernameValidator).setNext(emailValidator);
        verify(emailValidator).setNext(passwordValidator);
    }

    @Test
    void validate_NullRequest_HandledByChain() {
        // Arrange
        when(usernameValidator.setNext(emailValidator)).thenReturn(emailValidator);
        when(emailValidator.setNext(passwordValidator)).thenReturn(passwordValidator);
        when(usernameValidator.validate(null))
                .thenReturn(ValidationResult.failure("Request cannot be null"));

        // Act
        ValidationResult result = validationChain.validate(null);

        // Assert
        assertFalse(result.isValid());
        verify(usernameValidator).validate(null);
    }
}
