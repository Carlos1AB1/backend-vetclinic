package com.vetclinic.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class OwnerTest {

    private Owner owner;
    private Patient patient1;
    private Patient patient2;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setId(1L);
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setEmail("john@example.com");
        owner.setPhone("1234567890");
        owner.setAlternativePhone("0987654321");
        owner.setAddress("123 Main St");
        owner.setCity("Springfield");
        owner.setPostalCode("12345");
        owner.setDocumentType("DNI");
        owner.setDocumentNumber("12345678A");
        owner.setNotes("Cliente VIP");
        owner.setIsActive(true);
        owner.setPatients(new ArrayList<>());

        patient1 = new Patient();
        patient1.setId(1L);
        patient1.setName("Max");
        patient1.setSpecies("Perro");

        patient2 = new Patient();
        patient2.setId(2L);
        patient2.setName("Luna");
        patient2.setSpecies("Gato");
    }

    @Test
    void testOwnerCreation() {
        assertNotNull(owner);
        assertEquals(1L, owner.getId());
        assertEquals("John", owner.getFirstName());
        assertEquals("Doe", owner.getLastName());
        assertEquals("john@example.com", owner.getEmail());
        assertEquals("1234567890", owner.getPhone());
        assertEquals("0987654321", owner.getAlternativePhone());
        assertEquals("123 Main St", owner.getAddress());
        assertEquals("Springfield", owner.getCity());
        assertEquals("12345", owner.getPostalCode());
        assertEquals("DNI", owner.getDocumentType());
        assertEquals("12345678A", owner.getDocumentNumber());
        assertEquals("Cliente VIP", owner.getNotes());
        assertTrue(owner.getIsActive());
        assertNotNull(owner.getPatients());
    }

    @Test
    void testDefaultIsActive() {
        Owner newOwner = new Owner();
        newOwner.setIsActive(true);
        assertTrue(newOwner.getIsActive());
    }

    @Test
    void testGetFullName() {
        assertEquals("John Doe", owner.getFullName());

        owner.setFirstName("Jane");
        owner.setLastName("Smith");
        assertEquals("Jane Smith", owner.getFullName());
    }

    @Test
    void testAddPatient() {
        assertEquals(0, owner.getPatients().size());

        owner.addPatient(patient1);

        assertEquals(1, owner.getPatients().size());
        assertTrue(owner.getPatients().contains(patient1));
        assertEquals(owner, patient1.getOwner());
    }

    @Test
    void testAddMultiplePatients() {
        owner.addPatient(patient1);
        owner.addPatient(patient2);

        assertEquals(2, owner.getPatients().size());
        assertTrue(owner.getPatients().contains(patient1));
        assertTrue(owner.getPatients().contains(patient2));
    }

    @Test
    void testRemovePatient() {
        owner.addPatient(patient1);
        owner.addPatient(patient2);
        assertEquals(2, owner.getPatients().size());

        owner.removePatient(patient1);

        assertEquals(1, owner.getPatients().size());
        assertFalse(owner.getPatients().contains(patient1));
        assertNull(patient1.getOwner());
        assertTrue(owner.getPatients().contains(patient2));
    }

    @Test
    void testSettersAndGetters() {
        owner.setId(2L);
        assertEquals(2L, owner.getId());

        owner.setFirstName("Jane");
        assertEquals("Jane", owner.getFirstName());

        owner.setLastName("Smith");
        assertEquals("Smith", owner.getLastName());

        owner.setEmail("jane@example.com");
        assertEquals("jane@example.com", owner.getEmail());

        owner.setPhone("5555555555");
        assertEquals("5555555555", owner.getPhone());

        owner.setAlternativePhone("4444444444");
        assertEquals("4444444444", owner.getAlternativePhone());

        owner.setAddress("456 Oak Ave");
        assertEquals("456 Oak Ave", owner.getAddress());

        owner.setCity("Shelbyville");
        assertEquals("Shelbyville", owner.getCity());

        owner.setPostalCode("54321");
        assertEquals("54321", owner.getPostalCode());

        owner.setDocumentType("Pasaporte");
        assertEquals("Pasaporte", owner.getDocumentType());

        owner.setDocumentNumber("ABC123456");
        assertEquals("ABC123456", owner.getDocumentNumber());

        owner.setNotes("Nueva nota");
        assertEquals("Nueva nota", owner.getNotes());

        owner.setIsActive(false);
        assertFalse(owner.getIsActive());
    }

    @Test
    void testTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        owner.setCreatedAt(now);
        owner.setUpdatedAt(now);

        assertEquals(now, owner.getCreatedAt());
        assertEquals(now, owner.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        Owner emptyOwner = new Owner();
        assertNotNull(emptyOwner);
        assertNull(emptyOwner.getId());
        assertNull(emptyOwner.getFirstName());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Owner fullOwner = new Owner(
                1L,
                "John",
                "Doe",
                "john@example.com",
                "1234567890",
                "0987654321",
                "123 Main St",
                "Springfield",
                "12345",
                "DNI",
                "12345678A",
                "Notas",
                true,
                new ArrayList<>(),
                now,
                now
        );

        assertNotNull(fullOwner);
        assertEquals("John", fullOwner.getFirstName());
        assertEquals("Doe", fullOwner.getLastName());
    }

    @Test
    void testPatientsRelationship() {
        assertNotNull(owner.getPatients());
        assertTrue(owner.getPatients().isEmpty());

        owner.getPatients().add(patient1);
        assertEquals(1, owner.getPatients().size());
    }
}