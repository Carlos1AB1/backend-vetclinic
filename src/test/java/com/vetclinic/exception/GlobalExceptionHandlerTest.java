package com.vetclinic.exception;

import com.vetclinic.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        // Setup común si es necesario
    }

    @Test
    @DisplayName("Debe manejar ResourceNotFoundException correctamente")
    void shouldHandleResourceNotFoundException() {
        // Arrange
        String errorMessage = "User not found with id: 1";
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

        // Act
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleResourceNotFoundException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("Debe manejar ResourceNotFoundException con constructor de 3 parámetros")
    void shouldHandleResourceNotFoundExceptionWithThreeParams() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("User", "id", 1L);

        // Act
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleResourceNotFoundException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("User not found with id: '1'"));
    }

    @Test
    @DisplayName("Debe manejar BadRequestException correctamente")
    void shouldHandleBadRequestException() {
        // Arrange
        String errorMessage = "Invalid request parameters";
        BadRequestException exception = new BadRequestException(errorMessage);

        // Act
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleBadRequestException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("Debe manejar UnauthorizedException correctamente")
    void shouldHandleUnauthorizedException() {
        // Arrange
        String errorMessage = "User is not authorized";
        UnauthorizedException exception = new UnauthorizedException(errorMessage);

        // Act
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleUnauthorizedException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("Debe manejar DuplicateResourceException correctamente")
    void shouldHandleDuplicateResourceException() {
        // Arrange
        String errorMessage = "Email already exists";
        DuplicateResourceException exception = new DuplicateResourceException(errorMessage);

        // Act
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleDuplicateResourceException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("Debe manejar BusinessException correctamente")
    void shouldHandleBusinessException() {
        // Arrange
        String errorMessage = "Business logic validation failed";
        BusinessException exception = new BusinessException(errorMessage);

        // Act
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleBusinessException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("Debe manejar TokenExpiredException correctamente")
    void shouldHandleTokenExpiredException() {
        // Arrange
        String errorMessage = "JWT token has expired";
        TokenExpiredException exception = new TokenExpiredException(errorMessage);

        // Act
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleTokenExpiredException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("Debe manejar MethodArgumentNotValidException correctamente")
    void shouldHandleMethodArgumentNotValidException() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("user", "email", "Email is required");
        FieldError fieldError2 = new FieldError("user", "password", "Password must be at least 8 characters");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Act
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleValidationException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getErrors());
        assertTrue(response.getBody().getErrors().containsKey("email"));
        assertTrue(response.getBody().getErrors().containsKey("password"));
    }

    @Test
    @DisplayName("Debe manejar BadCredentialsException correctamente")
    void shouldHandleBadCredentialsException() {
        // Arrange
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // Act
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleBadCredentialsException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid username or password", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Debe manejar AccessDeniedException correctamente")
    void shouldHandleAccessDeniedException() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access is denied");

        // Act
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleAccessDeniedException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("You don't have permission to access this resource", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Debe manejar AuthenticationException genérica correctamente")
    void shouldHandleAuthenticationException() {
        // Arrange
        AuthenticationException exception = new AuthenticationException("Authentication failed") {};

        // Act
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleAuthenticationException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Authentication failed"));
    }

    @Test
    @DisplayName("Debe manejar Exception genérica correctamente")
    void shouldHandleGenericException() {
        // Arrange
        Exception exception = new Exception("Unexpected error");

        // Act
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleGlobalException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Debe verificar que todas las respuestas tienen timestamp")
    void shouldVerifyAllResponsesHaveTimestamp() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Test error");

        // Act
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleResourceNotFoundException(exception, webRequest);

        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Debe manejar múltiples errores de validación en el mismo campo")
    void shouldHandleMultipleValidationErrorsForSameField() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("user", "email", "Email is required");
        FieldError fieldError2 = new FieldError("user", "email", "Email format is invalid");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Act
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleValidationException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getErrors());
        assertTrue(response.getBody().getErrors().containsKey("email"));
        assertEquals(2, response.getBody().getErrors().get("email").size());
    }
}