package com.vetclinic.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PatientTest {

    private Patient patient;
    private Owner owner;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setId(1L);
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setEmail("john@example.com");
        owner.setPhone("1234567890");
        owner.setIsActive(true);

        patient = new Patient();
        patient.setId(1L);
        patient.setName("Max");
        patient.setSpecies("Perro");
        patient.setBreed("Labrador");
        patient.setBirthDate(LocalDate.of(2020, 1, 15));
        patient.setGender("MALE");
        patient.setColor("Golden");
        patient.setWeight(new BigDecimal("25.50"));
        patient.setMicrochipNumber("123456789");
        patient.setAllergies("Ninguna");
        patient.setMedicalHistory("Vacunación completa");
        patient.setIsActive(true);
        patient.setNotes("Paciente muy amigable");
        patient.setOwner(owner);
    }

    @Test
    void testPatientCreation() {
        assertNotNull(patient);
        assertEquals(1L, patient.getId());
        assertEquals("Max", patient.getName());
        assertEquals("Perro", patient.getSpecies());
        assertEquals("Labrador", patient.getBreed());
        assertEquals(LocalDate.of(2020, 1, 15), patient.getBirthDate());
        assertEquals("MALE", patient.getGender());
        assertEquals("Golden", patient.getColor());
        assertEquals(new BigDecimal("25.50"), patient.getWeight());
        assertEquals("123456789", patient.getMicrochipNumber());
        assertEquals("Ninguna", patient.getAllergies());
        assertEquals("Vacunación completa", patient.getMedicalHistory());
        assertTrue(patient.getIsActive());
        assertEquals("Paciente muy amigable", patient.getNotes());
        assertEquals(owner, patient.getOwner());
    }

    @Test
    void testDefaultIsActive() {
        Patient newPatient = new Patient();
        newPatient.setIsActive(true);
        assertTrue(newPatient.getIsActive());
    }

    @Test
    void testGetAge() {
        int currentYear = LocalDate.now().getYear();
        int expectedAge = currentYear - 2020;

        assertEquals(expectedAge, patient.getAge());
    }

    @Test
    void testGetAge_NullBirthDate() {
        patient.setBirthDate(null);
        assertNull(patient.getAge());
    }

    @Test
    void testSettersAndGetters() {
        patient.setId(2L);
        assertEquals(2L, patient.getId());

        patient.setName("Bella");
        assertEquals("Bella", patient.getName());

        patient.setSpecies("Gato");
        assertEquals("Gato", patient.getSpecies());

        patient.setBreed("Siamés");
        assertEquals("Siamés", patient.getBreed());

        LocalDate newBirthDate = LocalDate.of(2019, 6, 20);
        patient.setBirthDate(newBirthDate);
        assertEquals(newBirthDate, patient.getBirthDate());

        patient.setGender("FEMALE");
        assertEquals("FEMALE", patient.getGender());

        patient.setColor("White");
        assertEquals("White", patient.getColor());

        BigDecimal newWeight = new BigDecimal("5.25");
        patient.setWeight(newWeight);
        assertEquals(newWeight, patient.getWeight());

        patient.setMicrochipNumber("987654321");
        assertEquals("987654321", patient.getMicrochipNumber());

        patient.setAllergies("Polen");
        assertEquals("Polen", patient.getAllergies());

        patient.setMedicalHistory("Sin historial");
        assertEquals("Sin historial", patient.getMedicalHistory());

        patient.setIsActive(false);
        assertFalse(patient.getIsActive());

        patient.setNotes("Nueva nota");
        assertEquals("Nueva nota", patient.getNotes());

        Owner newOwner = new Owner();
        newOwner.setId(2L);
        patient.setOwner(newOwner);
        assertEquals(newOwner, patient.getOwner());
    }

    @Test
    void testTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        patient.setCreatedAt(now);
        patient.setUpdatedAt(now);

        assertEquals(now, patient.getCreatedAt());
        assertEquals(now, patient.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        Patient emptyPatient = new Patient();
        assertNotNull(emptyPatient);
        assertNull(emptyPatient.getId());
        assertNull(emptyPatient.getName());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Patient fullPatient = new Patient(
                1L,
                "Max",
                "Perro",
                "Labrador",
                LocalDate.of(2020, 1, 15),
                "MALE",
                "Golden",
                new BigDecimal("25.50"),
                "123456789",
                "Ninguna",
                "Historial completo",
                true,
                "Notas",
                owner,
                now,
                now
        );

        assertNotNull(fullPatient);
        assertEquals("Max", fullPatient.getName());
        assertEquals("Perro", fullPatient.getSpecies());
    }

    @Test
    void testOwnerRelationship() {
        assertNotNull(patient.getOwner());
        assertEquals(owner.getId(), patient.getOwner().getId());
        assertEquals("John", patient.getOwner().getFirstName());
    }
}