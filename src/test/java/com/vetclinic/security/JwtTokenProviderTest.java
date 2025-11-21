package com.vetclinic.security;

import com.vetclinic.entity.Role;
import com.vetclinic.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private User testUser;
    private Authentication authentication;

    private static final String JWT_SECRET = "dGhpc2lzYXZlcnlzZWN1cmVzZWNyZXRrZXlmb3Jqd3R0b2tlbmdlbmVyYXRpb25hbmR2YWxpZGF0aW9udGVzdGluZzEyMzQ1Ng==";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();

        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpirationMs", 86400000L);

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

        authentication = new UsernamePasswordAuthenticationToken(
                testUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Test
    void generateToken_Success() {
        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateTokenFromUsername_AccessToken_Success() {
        // Act
        String token = jwtTokenProvider.generateTokenFromUsername("testuser", false);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verify it's an access token
        assertFalse(jwtTokenProvider.isRefreshToken(token));
    }

    @Test
    void generateTokenFromUsername_RefreshToken_Success() {
        // Act
        String token = jwtTokenProvider.generateTokenFromUsername("testuser", true);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verify it's a refresh token
        assertTrue(jwtTokenProvider.isRefreshToken(token));
    }

    @Test
    void generateRefreshToken_Success() {
        // Act
        String refreshToken = jwtTokenProvider.generateRefreshToken("testuser");

        // Assert
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertTrue(jwtTokenProvider.isRefreshToken(refreshToken));
    }

    @Test
    void generateTokenWithClaims_Success() {
        // Act
        String token = jwtTokenProvider.generateTokenWithClaims(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getUsernameFromToken_Success() {
        // Arrange
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void getUsernameFromToken_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(MalformedJwtException.class,
                () -> jwtTokenProvider.getUsernameFromToken(invalidToken));
    }

    @Test
    void getClaimsFromToken_Success() {
        // Arrange
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Assert
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void getClaimsFromToken_RefreshToken_ContainsTypeRefresh() {
        // Arrange
        String refreshToken = jwtTokenProvider.generateRefreshToken("testuser");

        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken(refreshToken);

        // Assert
        assertEquals("refresh", claims.get("type"));
    }

    @Test
    void getClaimsFromToken_AccessToken_ContainsTypeAccess() {
        // Arrange
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Assert
        assertEquals("access", claims.get("type"));
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        // Arrange
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_EmptyToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_NullToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        // Arrange - Create provider with very short expiration
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtExpirationMs", 1L); // 1 millisecond
        ReflectionTestUtils.setField(shortExpirationProvider, "refreshExpirationMs", 1L);

        String token = shortExpirationProvider.generateToken(authentication);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenExpired_ValidToken_ReturnsFalse() {
        // Arrange
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    void isTokenExpired_ExpiredToken_ReturnsTrue() {
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtExpirationMs", 1L);
        ReflectionTestUtils.setField(shortExpirationProvider, "refreshExpirationMs", 1L);

        String token = shortExpirationProvider.generateToken(authentication);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Assert
        assertTrue(isExpired);
    }

    @Test
    void getExpirationDateFromToken_Success() {
        // Arrange
        String token = jwtTokenProvider.generateToken(authentication);

        // Act
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);

        // Assert
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void isRefreshToken_RefreshToken_ReturnsTrue() {
        // Arrange
        String refreshToken = jwtTokenProvider.generateRefreshToken("testuser");

        // Act
        boolean isRefresh = jwtTokenProvider.isRefreshToken(refreshToken);

        // Assert
        assertTrue(isRefresh);
    }

    @Test
    void isRefreshToken_AccessToken_ReturnsFalse() {
        // Arrange
        String accessToken = jwtTokenProvider.generateToken(authentication);

        // Act
        boolean isRefresh = jwtTokenProvider.isRefreshToken(accessToken);

        // Assert
        assertFalse(isRefresh);
    }

    @Test
    void generateToken_MultipleTimes_GeneratesDifferentTokens() {
        // Act
        String token1 = jwtTokenProvider.generateToken(authentication);
        String token2 = jwtTokenProvider.generateToken(authentication);

        // Assert - Both tokens should be valid
        assertTrue(jwtTokenProvider.validateToken(token1));
        assertTrue(jwtTokenProvider.validateToken(token2));

        // Extract usernames to verify both tokens are properly formed
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token1));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token2));
    }



    @Test
    void generateToken_SameUsername_DifferentUsers_BothValid() {
        // Arrange
        Authentication auth1 = new UsernamePasswordAuthenticationToken(
                testUser, null, testUser.getAuthorities());

        User testUser2 = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test2@example.com")
                .roles(testUser.getRoles())
                .build();

        Authentication auth2 = new UsernamePasswordAuthenticationToken(
                testUser2, null, testUser2.getAuthorities());

        // Act
        String token1 = jwtTokenProvider.generateToken(auth1);
        String token2 = jwtTokenProvider.generateToken(auth2);

        // Assert
        assertTrue(jwtTokenProvider.validateToken(token1));
        assertTrue(jwtTokenProvider.validateToken(token2));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token1));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token2));
    }
}
