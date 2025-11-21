package com.vetclinic.security;

import com.vetclinic.entity.Role;
import com.vetclinic.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User testUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        Role testRole = Role.builder()
                .id(UUID.randomUUID())
                .name("ADMIN")
                .description("Admin role")
                .isSystemRole(true)
                .permissions(new HashSet<>())
                .build();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .phone("1234567890")
                .roles(Set.of(testRole))
                .isActive(true)
                .build();
    }

    @Test
    void doFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        // Arrange
        String validToken = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(tokenProvider.validateToken(validToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testuser", SecurityContextHolder.getContext().getAuthentication().getName());

        verify(tokenProvider).validateToken(validToken);
        verify(tokenProvider).getUsernameFromToken(validToken);
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        String invalidToken = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(tokenProvider.validateToken(invalidToken)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(tokenProvider).validateToken(invalidToken);
        verify(tokenProvider, never()).getUsernameFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NoAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(tokenProvider, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_EmptyAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(tokenProvider, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidBearerFormat_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(tokenProvider, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_BearerWithoutToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(tokenProvider, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ExceptionDuringValidation_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        String validToken = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(tokenProvider.validateToken(validToken)).thenThrow(new RuntimeException("Validation error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_UserNotFound_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        String validToken = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(tokenProvider.validateToken(validToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(validToken)).thenReturn("nonexistent");
        when(userDetailsService.loadUserByUsername("nonexistent"))
                .thenThrow(new RuntimeException("User not found"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ValidToken_ContinuesFilterChain() throws ServletException, IOException {
        // Arrange
        String validToken = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(tokenProvider.validateToken(validToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_MultipleRoles_SetsAuthenticationWithAllRoles() throws ServletException, IOException {
        // Arrange
        Role adminRole = Role.builder()
                .id(UUID.randomUUID())
                .name("ADMIN")
                .build();

        Role userRole = Role.builder()
                .id(UUID.randomUUID())
                .name("USER")
                .build();

        testUser.setRoles(Set.of(adminRole, userRole));

        String validToken = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(tokenProvider.validateToken(validToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(validToken)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().size() >= 2);
        verify(filterChain).doFilter(request, response);
    }
}