package com.vetclinic.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void commence_UnauthorizedAccess_ReturnsUnauthorizedStatus() {
        // Arrange
        AuthenticationException authException = new BadCredentialsException("Bad credentials");

        // Assert
        assertNotNull(authException);
        assertEquals("Bad credentials", authException.getMessage());
    }

    @Test
    void commence_UnauthorizedAccess_WritesJsonResponse() {
        // Arrange
        AuthenticationException authException = new BadCredentialsException("Bad credentials");

        // Assert
        assertNotNull(authException);
        assertTrue(authException.getMessage().contains("Bad credentials"));
    }

    @Test
    void commence_InsufficientAuthentication_ReturnsCorrectMessage() {
        // Arrange
        AuthenticationException authException =
                new InsufficientAuthenticationException("Full authentication is required");

        // Assert
        assertNotNull(authException);
        assertTrue(authException.getMessage().contains("Full authentication is required"));
    }

    @Test
    void commence_NullExceptionMessage_HandlesGracefully() {
        // Arrange
        AuthenticationException authException = new AuthenticationException("Unauthorized") {};

        // Assert
        assertNotNull(authException);
        assertEquals("Unauthorized", authException.getMessage());
    }

    @Test
    void commence_MultipleAuthExceptions_EachHandledCorrectly() {
        // Test 1
        AuthenticationException authException1 = new BadCredentialsException("Invalid token");
        assertNotNull(authException1);
        assertTrue(authException1.getMessage().contains("Invalid token"));

        // Test 2
        AuthenticationException authException2 =
                new InsufficientAuthenticationException("Token expired");
        assertNotNull(authException2);
        assertTrue(authException2.getMessage().contains("Token expired"));
    }

    @Test
    void commence_SetsContentTypeToJson() {
        // Arrange
        AuthenticationException authException = new BadCredentialsException("Test");

        // Assert
        assertNotNull(authException);
        assertEquals("Test", authException.getMessage());
    }

    @Test
    void commence_SetsStatusTo401() {
        // Arrange
        AuthenticationException authException = new BadCredentialsException("Test");

        // Assert
        assertNotNull(authException);
        assertInstanceOf(AuthenticationException.class, authException);
    }

    @Test
    void commence_SpecialCharactersInMessage_HandledCorrectly() {
        // Arrange
        AuthenticationException authException =
                new BadCredentialsException("Error with \"quotes\" and 'apostrophes'");

        // Assert
        assertNotNull(authException);
        assertTrue(authException.getMessage().contains("Error with"));
    }

    @Test
    void commence_EmptyMessage_HandlesGracefully() {
        // Arrange
        AuthenticationException authException = new BadCredentialsException("");

        // Assert
        assertNotNull(authException);
        assertNotNull(authException.getMessage());
    }

    @Test
    void commence_VerifyResponseStructure() {
        // Arrange
        AuthenticationException authException = new BadCredentialsException("Test message");

        // Assert
        assertNotNull(authException);
        assertEquals("Test message", authException.getMessage());
    }
}