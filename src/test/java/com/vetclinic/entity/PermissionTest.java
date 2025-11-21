package com.vetclinic.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PermissionTest {

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
    }

    @Test
    void testPermissionBuilder() {
        assertNotNull(permission);
        assertEquals("USER_READ", permission.getName());
        assertEquals("users", permission.getResource());
        assertEquals("read", permission.getAction());
        assertEquals("Read user information", permission.getDescription());
    }

    @Test
    void testSettersAndGetters() {
        UUID newId = UUID.randomUUID();
        permission.setId(newId);
        assertEquals(newId, permission.getId());

        permission.setName("USER_WRITE");
        assertEquals("USER_WRITE", permission.getName());

        permission.setResource("patients");
        assertEquals("patients", permission.getResource());

        permission.setAction("write");
        assertEquals("write", permission.getAction());

        permission.setDescription("Write patient information");
        assertEquals("Write patient information", permission.getDescription());
    }

    @Test
    void testNoArgsConstructor() {
        Permission emptyPermission = new Permission();
        assertNotNull(emptyPermission);
        assertNull(emptyPermission.getId());
        assertNull(emptyPermission.getName());
        assertNull(emptyPermission.getResource());
        assertNull(emptyPermission.getAction());
    }

    @Test
    void testAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        Permission fullPermission = new Permission(
                id,
                "PATIENT_DELETE",
                "patients",
                "delete",
                "Delete patient records"
        );

        assertNotNull(fullPermission);
        assertEquals(id, fullPermission.getId());
        assertEquals("PATIENT_DELETE", fullPermission.getName());
        assertEquals("patients", fullPermission.getResource());
        assertEquals("delete", fullPermission.getAction());
        assertEquals("Delete patient records", fullPermission.getDescription());
    }

    @Test
    void testNullableFields() {
        Permission minimalPermission = Permission.builder()
                .name("MINIMAL_PERMISSION")
                .build();

        assertNotNull(minimalPermission);
        assertEquals("MINIMAL_PERMISSION", minimalPermission.getName());
        assertNull(minimalPermission.getResource());
        assertNull(minimalPermission.getAction());
        assertNull(minimalPermission.getDescription());
    }
}