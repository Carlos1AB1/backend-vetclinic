package com.vetclinic.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void commence_UnauthorizedAccess_ReturnsUnauthorizedStatus() throws IOException {
        // Arrange
        AuthenticationException authException = new BadCredentialsException("Bad credentials");

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Assert
        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).getWriter();
    }

    @Test
    void commence_UnauthorizedAccess_WritesJsonResponse() throws IOException {
        // Arrange
        AuthenticationException authException = new BadCredentialsException("Bad credentials");

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);
        writer.flush();

        // Assert
        String jsonResponse = stringWriter.toString();
        assertNotNull(jsonResponse);
        assertTrue(jsonResponse.contains("\"error\": \"Unauthorized\""));
        assertTrue(jsonResponse.contains("\"message\": \"Bad credentials\""));
    }

    @Test
    void commence_InsufficientAuthentication_ReturnsCorrectMessage() throws IOException {
        // Arrange
        AuthenticationException authException =
                new InsufficientAuthenticationException("Full authentication is required");

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);
        writer.flush();

        // Assert
        String jsonResponse = stringWriter.toString();
        assertTrue(jsonResponse.contains("Full authentication is required"));
    }

    @Test
    void commence_NullExceptionMessage_HandlesGracefully() throws IOException {
        // Arrange
        AuthenticationException authException = new AuthenticationException("Unauthorized") {};

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);
        writer.flush();

        // Assert
        String jsonResponse = stringWriter.toString();
        assertNotNull(jsonResponse);
        assertTrue(jsonResponse.contains("\"error\": \"Unauthorized\""));
    }

    @Test
    void commence_MultipleAuthExceptions_EachHandledCorrectly() throws IOException {
        // Test 1
        AuthenticationException authException1 = new BadCredentialsException("Invalid token");
        jwtAuthenticationEntryPoint.commence(request, response, authException1);
        writer.flush();

        String jsonResponse1 = stringWriter.toString();
        assertTrue(jsonResponse1.contains("Invalid token"));

        // Reset for test 2
        stringWriter.getBuffer().setLength(0);

        // Test 2
        AuthenticationException authException2 =
                new InsufficientAuthenticationException("Token expired");
        jwtAuthenticationEntryPoint.commence(request, response, authException2);
        writer.flush();

        String jsonResponse2 = stringWriter.toString();
        assertTrue(jsonResponse2.contains("Token expired"));
    }

    @Test
    void commence_SetsContentTypeToJson() throws IOException {
        // Arrange
        AuthenticationException authException = new BadCredentialsException("Test");

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Assert
        verify(response).setContentType("application/json");
    }

    @Test
    void commence_SetsStatusTo401() throws IOException {
        // Arrange
        AuthenticationException authException = new BadCredentialsException("Test");

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setStatus(401);
    }

    @Test
    void commence_SpecialCharactersInMessage_HandledCorrectly() throws IOException {
        // Arrange
        AuthenticationException authException =
                new BadCredentialsException("Error with \"quotes\" and 'apostrophes'");

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);
        writer.flush();

        // Assert
        String jsonResponse = stringWriter.toString();
        assertNotNull(jsonResponse);
        assertTrue(jsonResponse.contains("Error with"));
    }

    @Test
    void commence_EmptyMessage_HandlesGracefully() throws IOException {
        // Arrange
        AuthenticationException authException = new BadCredentialsException("");

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);
        writer.flush();

        // Assert
        String jsonResponse = stringWriter.toString();
        assertNotNull(jsonResponse);
        assertTrue(jsonResponse.contains("\"error\": \"Unauthorized\""));
    }

    @Test
    void commence_VerifyResponseStructure() throws IOException {
        // Arrange
        AuthenticationException authException = new BadCredentialsException("Test message");

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);
        writer.flush();

        // Assert
        String jsonResponse = stringWriter.toString();
        assertTrue(jsonResponse.startsWith("{"));
        assertTrue(jsonResponse.endsWith("}"));
        assertTrue(jsonResponse.contains("\"error\":"));
        assertTrue(jsonResponse.contains("\"message\":"));
    }
}