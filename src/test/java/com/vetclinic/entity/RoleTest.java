package com.vetclinic.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    private Role role;
    private Permission permission1;
    private Permission permission2;

    @BeforeEach
    void setUp() {
        permission1 = Permission.builder()
                .id(UUID.randomUUID())
                .name("USER_READ")
                .resource("users")
                .action("read")
                .description("Read user information")
                .build();

        permission2 = Permission.builder()
                .id(UUID.randomUUID())
                .name("USER_WRITE")
                .resource("users")
                .action("write")
                .description("Write user information")
                .build();

        Set<Permission> permissions = new HashSet<>();
        permissions.add(permission1);
        permissions.add(permission2);

        role = Role.builder()
                .id(UUID.randomUUID())
                .name("ADMIN")
                .description("Administrator with full access")
                .permissions(permissions)
                .isSystemRole(true)
                .build();
    }

    @Test
    void testRoleBuilder() {
        assertNotNull(role);
        assertEquals("ADMIN", role.getName());
        assertEquals("Administrator with full access", role.getDescription());
        assertTrue(role.getIsSystemRole());
        assertEquals(2, role.getPermissions().size());
    }

    @Test
    void testDefaultValues() {
        Role newRole = Role.builder()
                .name("USER")
                .build();

        assertFalse(newRole.getIsSystemRole());
        assertNotNull(newRole.getPermissions());
        assertTrue(newRole.getPermissions().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        UUID newId = UUID.randomUUID();
        role.setId(newId);
        assertEquals(newId, role.getId());

        role.setName("MODERATOR");
        assertEquals("MODERATOR", role.getName());

        role.setDescription("Moderator role");
        assertEquals("Moderator role", role.getDescription());

        role.setIsSystemRole(false);
        assertFalse(role.getIsSystemRole());
    }

    @Test
    void testPermissionManagement() {
        assertEquals(2, role.getPermissions().size());
        assertTrue(role.getPermissions().contains(permission1));
        assertTrue(role.getPermissions().contains(permission2));

        Permission newPermission = Permission.builder()
                .id(UUID.randomUUID())
                .name("USER_DELETE")
                .resource("users")
                .action("delete")
                .build();

        role.getPermissions().add(newPermission);
        assertEquals(3, role.getPermissions().size());
        assertTrue(role.getPermissions().contains(newPermission));

        role.getPermissions().remove(permission1);
        assertEquals(2, role.getPermissions().size());
        assertFalse(role.getPermissions().contains(permission1));
    }

    @Test
    void testNoArgsConstructor() {
        Role emptyRole = new Role();
        assertNotNull(emptyRole);
        assertNull(emptyRole.getId());
        assertNull(emptyRole.getName());
    }

    @Test
    void testAllArgsConstructor() {
        Set<Permission> permissions = new HashSet<>();
        permissions.add(permission1);

        Role fullRole = new Role(
                UUID.randomUUID(),
                "VETERINARIAN",
                "Veterinarian role",
                permissions,
                false
        );

        assertNotNull(fullRole);
        assertEquals("VETERINARIAN", fullRole.getName());
        assertEquals("Veterinarian role", fullRole.getDescription());
        assertFalse(fullRole.getIsSystemRole());
        assertEquals(1, fullRole.getPermissions().size());
    }

    @Test
    void testEmptyPermissions() {
        Role roleWithNoPermissions = Role.builder()
                .name("GUEST")
                .description("Guest role")
                .build();

        assertNotNull(roleWithNoPermissions.getPermissions());
        assertTrue(roleWithNoPermissions.getPermissions().isEmpty());
    }
}