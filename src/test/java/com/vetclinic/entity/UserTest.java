package com.vetclinic.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private Role adminRole;
    private Permission permission;

    @BeforeEach
    void setUp() {
        permission = Permission.builder()
                .id(UUID.randomUUID())
                .name("USER_READ")
                .resource("users")
                .action("read")
                .description("Read user information")
                .build();

        Set<Permission> permissions = new HashSet<>();
        permissions.add(permission);

        adminRole = Role.builder()
                .id(UUID.randomUUID())
                .name("ADMIN")
                .description("Administrator role")
                .permissions(permissions)
                .isSystemRole(true)
                .build();

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);

        user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .isActive(true)
                .isLocked(false)
                .failedLoginAttempts(0)
                .roles(roles)
                .build();
    }

    @Test
    void testUserBuilder() {
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("encodedPassword", user.getPassword());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("1234567890", user.getPhone());
        assertTrue(user.getIsActive());
        assertFalse(user.getIsLocked());
        assertEquals(0, user.getFailedLoginAttempts());
    }

    @Test
    void testDefaultValues() {
        User newUser = User.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password")
                .build();

        assertTrue(newUser.getIsActive());
        assertFalse(newUser.getIsLocked());
        assertEquals(0, newUser.getFailedLoginAttempts());
        assertNotNull(newUser.getRoles());
    }

    @Test
    void testGetAuthorities() {
        var authorities = user.getAuthorities();

        assertNotNull(authorities);
        assertFalse(authorities.isEmpty());

        boolean hasRoleAuthority = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        assertTrue(hasRoleAuthority);

        boolean hasPermissionAuthority = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("USER_READ"));
        assertTrue(hasPermissionAuthority);
    }

    @Test
    void testIsAccountNonExpired() {
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked() {
        assertTrue(user.isAccountNonLocked());

        user.setIsLocked(true);
        user.setLockTime(LocalDateTime.now());
        assertFalse(user.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired() {
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled() {
        assertTrue(user.isEnabled());

        user.setIsActive(false);
        assertFalse(user.isEnabled());
    }

    @Test
    void testGetFullName() {
        assertEquals("John Doe", user.getFullName());

        user.setFirstName(null);
        user.setLastName(null);
        assertEquals("testuser", user.getFullName());
    }

    @Test
    void testIncrementFailedLoginAttempts() {
        assertEquals(0, user.getFailedLoginAttempts());

        user.incrementFailedLoginAttempts();
        assertEquals(1, user.getFailedLoginAttempts());

        user.incrementFailedLoginAttempts();
        assertEquals(2, user.getFailedLoginAttempts());
    }

    @Test
    void testIncrementFailedLoginAttempts_AutoLock() {
        assertEquals(0, user.getFailedLoginAttempts());
        assertFalse(user.getIsLocked());

        for (int i = 0; i < 5; i++) {
            user.incrementFailedLoginAttempts();
        }

        assertEquals(5, user.getFailedLoginAttempts());
        assertTrue(user.getIsLocked());
        assertNotNull(user.getLockTime());
    }

    @Test
    void testResetFailedLoginAttempts() {
        user.setFailedLoginAttempts(5);
        user.setIsLocked(true);
        user.setLockTime(LocalDateTime.now());

        assertEquals(5, user.getFailedLoginAttempts());
        assertTrue(user.getIsLocked());

        user.resetFailedLoginAttempts();

        assertEquals(0, user.getFailedLoginAttempts());
        assertFalse(user.getIsLocked());
        assertNull(user.getLockTime());
    }

    @Test
    void testLockAccount() {
        assertFalse(user.getIsLocked());
        assertNull(user.getLockTime());

        user.setIsLocked(true);
        user.setLockTime(LocalDateTime.now());

        assertTrue(user.getIsLocked());
        assertNotNull(user.getLockTime());
    }

    @Test
    void testUnlockAccount() {
        user.setIsLocked(true);
        user.setLockTime(LocalDateTime.now());
        user.setFailedLoginAttempts(5);

        user.resetFailedLoginAttempts();

        assertFalse(user.getIsLocked());
        assertNull(user.getLockTime());
        assertEquals(0, user.getFailedLoginAttempts());
    }

    @Test
    void testUpdateLastLogin() {
        assertNull(user.getLastLogin());

        LocalDateTime loginTime = LocalDateTime.now();
        user.setLastLogin(loginTime);

        assertNotNull(user.getLastLogin());
        assertEquals(loginTime, user.getLastLogin());
    }

    @Test
    void testRoleManagement() {
        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(adminRole));

        Role newRole = Role.builder()
                .id(UUID.randomUUID())
                .name("USER")
                .build();

        user.getRoles().add(newRole);
        assertEquals(2, user.getRoles().size());
    }

    @Test
    void testSettersAndGetters() {
        UUID newId = UUID.randomUUID();
        user.setId(newId);
        assertEquals(newId, user.getId());

        user.setUsername("newusername");
        assertEquals("newusername", user.getUsername());

        user.setEmail("newemail@example.com");
        assertEquals("newemail@example.com", user.getEmail());

        user.setPassword("newpassword");
        assertEquals("newpassword", user.getPassword());

        user.setFirstName("Jane");
        assertEquals("Jane", user.getFirstName());

        user.setLastName("Smith");
        assertEquals("Smith", user.getLastName());

        user.setPhone("9876543210");
        assertEquals("9876543210", user.getPhone());

        user.setIsActive(false);
        assertFalse(user.getIsActive());

        user.setIsLocked(true);
        assertTrue(user.getIsLocked());
    }

    @Test
    void testTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        User emptyUser = new User();
        assertNotNull(emptyUser);
        assertNull(emptyUser.getId());
        assertNull(emptyUser.getUsername());
    }

    @Test
    void testAllArgsConstructor() {
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);

        User fullUser = new User(
                UUID.randomUUID(),
                "username",
                "email@test.com",
                "password",
                "First",
                "Last",
                "123456",
                true,
                false,
                0,
                null,
                null,
                roles,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        assertNotNull(fullUser);
        assertEquals("username", fullUser.getUsername());
        assertEquals("email@test.com", fullUser.getEmail());
    }

    @Test
    void testAutoUnlockAfter15Minutes() {
        user.setIsLocked(true);
        user.setLockTime(LocalDateTime.now().minusMinutes(20));

        assertTrue(user.isAccountNonLocked());
        assertFalse(user.getIsLocked());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockTime());
    }

    @Test
    void testStillLockedBefore15Minutes() {
        user.setIsLocked(true);
        user.setLockTime(LocalDateTime.now().minusMinutes(10));

        assertFalse(user.isAccountNonLocked());
    }
}