package com.vetclinic.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PrescriptionTest {

    private Prescription prescription;
    private MedicalRecord medicalRecord;
    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setName("Max");

        medicalRecord = new MedicalRecord();
        medicalRecord.setId(1L);

        prescription = new Prescription();
        prescription.setId(1L);
        prescription.setMedicalRecord(medicalRecord);
        prescription.setPatient(patient);
        prescription.setMedicationName("Amoxicilina");
        prescription.setDosage("500mg");
        prescription.setFrequency("Cada 8 horas");
        prescription.setDuration("7 días");
        prescription.setInstructions("Tomar con alimentos");
        prescription.setStartDate(LocalDateTime.now());
        prescription.setEndDate(LocalDateTime.now().plusDays(7));
        prescription.setNotes("Completar tratamiento");
        prescription.setIsActive(true);
    }

    @Test
    void testPrescriptionCreation() {
        assertNotNull(prescription);
        assertEquals(1L, prescription.getId());
        assertEquals(medicalRecord, prescription.getMedicalRecord());
        assertEquals(patient, prescription.getPatient());
        assertEquals("Amoxicilina", prescription.getMedicationName());
        assertEquals("500mg", prescription.getDosage());
        assertEquals("Cada 8 horas", prescription.getFrequency());
        assertEquals("7 días", prescription.getDuration());
        assertEquals("Tomar con alimentos", prescription.getInstructions());
        assertNotNull(prescription.getStartDate());
        assertNotNull(prescription.getEndDate());
        assertEquals("Completar tratamiento", prescription.getNotes());
        assertTrue(prescription.getIsActive());
    }

    @Test
    void testDefaultIsActive() {
        Prescription newPrescription = new Prescription();
        newPrescription.setIsActive(true);
        assertTrue(newPrescription.getIsActive());
    }

    @Test
    void testIsCurrentlyActive_True() {
        prescription.setStartDate(LocalDateTime.now().minusDays(1));
        prescription.setEndDate(LocalDateTime.now().plusDays(1));
        prescription.setIsActive(true);

        assertTrue(prescription.isCurrentlyActive());
    }

    @Test
    void testIsCurrentlyActive_False_NotStarted() {
        prescription.setStartDate(LocalDateTime.now().plusDays(1));
        prescription.setEndDate(LocalDateTime.now().plusDays(7));
        prescription.setIsActive(true);

        assertFalse(prescription.isCurrentlyActive());
    }

    @Test
    void testIsCurrentlyActive_False_Expired() {
        prescription.setStartDate(LocalDateTime.now().minusDays(7));
        prescription.setEndDate(LocalDateTime.now().minusDays(1));
        prescription.setIsActive(true);

        assertFalse(prescription.isCurrentlyActive());
    }

    @Test
    void testIsCurrentlyActive_False_NotActive() {
        prescription.setStartDate(LocalDateTime.now().minusDays(1));
        prescription.setEndDate(LocalDateTime.now().plusDays(1));
        prescription.setIsActive(false);

        assertFalse(prescription.isCurrentlyActive());
    }

    @Test
    void testIsCurrentlyActive_True_NoEndDate() {
        prescription.setStartDate(LocalDateTime.now().minusDays(1));
        prescription.setEndDate(null);
        prescription.setIsActive(true);

        assertTrue(prescription.isCurrentlyActive());
    }

    @Test
    void testIsExpired_True() {
        prescription.setEndDate(LocalDateTime.now().minusDays(1));

        assertTrue(prescription.isExpired());
    }

    @Test
    void testIsExpired_False() {
        prescription.setEndDate(LocalDateTime.now().plusDays(1));

        assertFalse(prescription.isExpired());
    }

    @Test
    void testIsExpired_False_NullEndDate() {
        prescription.setEndDate(null);

        assertFalse(prescription.isExpired());
    }

    @Test
    void testSettersAndGetters() {
        prescription.setId(2L);
        assertEquals(2L, prescription.getId());

        MedicalRecord newRecord = new MedicalRecord();
        newRecord.setId(2L);
        prescription.setMedicalRecord(newRecord);
        assertEquals(newRecord, prescription.getMedicalRecord());

        Patient newPatient = new Patient();
        newPatient.setId(2L);
        prescription.setPatient(newPatient);
        assertEquals(newPatient, prescription.getPatient());

        prescription.setMedicationName("Paracetamol");
        assertEquals("Paracetamol", prescription.getMedicationName());

        prescription.setDosage("250mg");
        assertEquals("250mg", prescription.getDosage());

        prescription.setFrequency("Cada 6 horas");
        assertEquals("Cada 6 horas", prescription.getFrequency());

        prescription.setDuration("5 días");
        assertEquals("5 días", prescription.getDuration());

        prescription.setInstructions("Nueva instrucción");
        assertEquals("Nueva instrucción", prescription.getInstructions());

        LocalDateTime newStartDate = LocalDateTime.now().plusDays(1);
        prescription.setStartDate(newStartDate);
        assertEquals(newStartDate, prescription.getStartDate());

        LocalDateTime newEndDate = LocalDateTime.now().plusDays(8);
        prescription.setEndDate(newEndDate);
        assertEquals(newEndDate, prescription.getEndDate());

        prescription.setNotes("Nuevas notas");
        assertEquals("Nuevas notas", prescription.getNotes());

        prescription.setIsActive(false);
        assertFalse(prescription.getIsActive());
    }

    @Test
    void testTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        prescription.setCreatedAt(now);
        prescription.setUpdatedAt(now);

        assertEquals(now, prescription.getCreatedAt());
        assertEquals(now, prescription.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        Prescription emptyPrescription = new Prescription();
        assertNotNull(emptyPrescription);
        assertNull(emptyPrescription.getId());
        assertNull(emptyPrescription.getMedicationName());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Prescription fullPrescription = new Prescription(
                1L,
                medicalRecord,
                patient,
                "Medicamento",
                "Dosis",
                "Frecuencia",
                "Duración",
                "Instrucciones",
                now,
                now.plusDays(7),
                "Notas",
                true,
                now,
                now
        );

        assertNotNull(fullPrescription);
        assertEquals(1L, fullPrescription.getId());
        assertEquals("Medicamento", fullPrescription.getMedicationName());
    }

    @Test
    void testRelationships() {
        assertNotNull(prescription.getMedicalRecord());
        assertEquals(medicalRecord.getId(), prescription.getMedicalRecord().getId());

        assertNotNull(prescription.getPatient());
        assertEquals(patient.getId(), prescription.getPatient().getId());
    }
}