package com.vetclinic.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MedicalRecordTest {

    private MedicalRecord medicalRecord;
    private Patient patient;
    private User veterinarian;
    private Appointment appointment;
    private Prescription prescription1;
    private Prescription prescription2;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setName("Max");

        veterinarian = User.builder()
                .id(UUID.randomUUID())
                .username("drsmith")
                .firstName("Dr. Jane")
                .lastName("Smith")
                .build();

        appointment = new Appointment();
        appointment.setId(1L);

        prescription1 = new Prescription();
        prescription1.setId(1L);
        prescription1.setMedicationName("Antibiótico");

        prescription2 = new Prescription();
        prescription2.setId(2L);
        prescription2.setMedicationName("Analgésico");

        medicalRecord = new MedicalRecord();
        medicalRecord.setId(1L);
        medicalRecord.setPatient(patient);
        medicalRecord.setVeterinarian(veterinarian);
        medicalRecord.setAppointment(appointment);
        medicalRecord.setRecordDate(LocalDateTime.now());
        medicalRecord.setDiagnosis("Infección respiratoria");
        medicalRecord.setTreatment("Antibióticos y reposo");
        medicalRecord.setSymptoms("Tos y estornudos");
        medicalRecord.setVitalSigns("Temperatura: 38.5°C, FC: 120");
        medicalRecord.setWeight(new BigDecimal("25.50"));
        medicalRecord.setTemperature(new BigDecimal("38.5"));
        medicalRecord.setNotes("Mejoría esperada en 7 días");
        medicalRecord.setFollowUpRequired(true);
        medicalRecord.setFollowUpDate(LocalDateTime.now().plusDays(7));
        medicalRecord.setIsActive(true);
        medicalRecord.setPrescriptions(new ArrayList<>());
    }

    @Test
    void testMedicalRecordCreation() {
        assertNotNull(medicalRecord);
        assertEquals(1L, medicalRecord.getId());
        assertEquals(patient, medicalRecord.getPatient());
        assertEquals(veterinarian, medicalRecord.getVeterinarian());
        assertEquals(appointment, medicalRecord.getAppointment());
        assertNotNull(medicalRecord.getRecordDate());
        assertEquals("Infección respiratoria", medicalRecord.getDiagnosis());
        assertEquals("Antibióticos y reposo", medicalRecord.getTreatment());
        assertEquals("Tos y estornudos", medicalRecord.getSymptoms());
        assertEquals("Temperatura: 38.5°C, FC: 120", medicalRecord.getVitalSigns());
        assertEquals(new BigDecimal("25.50"), medicalRecord.getWeight());
        assertEquals(new BigDecimal("38.5"), medicalRecord.getTemperature());
        assertEquals("Mejoría esperada en 7 días", medicalRecord.getNotes());
        assertTrue(medicalRecord.getFollowUpRequired());
        assertNotNull(medicalRecord.getFollowUpDate());
        assertTrue(medicalRecord.getIsActive());
        assertNotNull(medicalRecord.getPrescriptions());
    }

    @Test
    void testDefaultFollowUpRequired() {
        MedicalRecord newRecord = new MedicalRecord();
        newRecord.setFollowUpRequired(false);
        assertFalse(newRecord.getFollowUpRequired());
    }

    @Test
    void testDefaultIsActive() {
        MedicalRecord newRecord = new MedicalRecord();
        newRecord.setIsActive(true);
        assertTrue(newRecord.getIsActive());
    }

    @Test
    void testAddPrescription() {
        assertEquals(0, medicalRecord.getPrescriptions().size());

        medicalRecord.addPrescription(prescription1);

        assertEquals(1, medicalRecord.getPrescriptions().size());
        assertTrue(medicalRecord.getPrescriptions().contains(prescription1));
        assertEquals(medicalRecord, prescription1.getMedicalRecord());
    }

    @Test
    void testAddMultiplePrescriptions() {
        medicalRecord.addPrescription(prescription1);
        medicalRecord.addPrescription(prescription2);

        assertEquals(2, medicalRecord.getPrescriptions().size());
        assertTrue(medicalRecord.getPrescriptions().contains(prescription1));
        assertTrue(medicalRecord.getPrescriptions().contains(prescription2));
    }

    @Test
    void testRemovePrescription() {
        medicalRecord.addPrescription(prescription1);
        medicalRecord.addPrescription(prescription2);
        assertEquals(2, medicalRecord.getPrescriptions().size());

        medicalRecord.removePrescription(prescription1);

        assertEquals(1, medicalRecord.getPrescriptions().size());
        assertFalse(medicalRecord.getPrescriptions().contains(prescription1));
        assertNull(prescription1.getMedicalRecord());
        assertTrue(medicalRecord.getPrescriptions().contains(prescription2));
    }

    @Test
    void testNeedsFollowUp_True() {
        medicalRecord.setFollowUpRequired(true);
        medicalRecord.setFollowUpDate(LocalDateTime.now().plusDays(1));

        assertTrue(medicalRecord.needsFollowUp());
    }

    @Test
    void testNeedsFollowUp_False_NotRequired() {
        medicalRecord.setFollowUpRequired(false);
        medicalRecord.setFollowUpDate(LocalDateTime.now().plusDays(1));

        assertFalse(medicalRecord.needsFollowUp());
    }

    @Test
    void testNeedsFollowUp_False_PastDate() {
        medicalRecord.setFollowUpRequired(true);
        medicalRecord.setFollowUpDate(LocalDateTime.now().minusDays(1));

        assertFalse(medicalRecord.needsFollowUp());
    }

    @Test
    void testNeedsFollowUp_False_NullDate() {
        medicalRecord.setFollowUpRequired(true);
        medicalRecord.setFollowUpDate(null);

        assertFalse(medicalRecord.needsFollowUp());
    }

    @Test
    void testSettersAndGetters() {
        medicalRecord.setId(2L);
        assertEquals(2L, medicalRecord.getId());

        Patient newPatient = new Patient();
        newPatient.setId(2L);
        medicalRecord.setPatient(newPatient);
        assertEquals(newPatient, medicalRecord.getPatient());

        User newVet = User.builder().id(UUID.randomUUID()).build();
        medicalRecord.setVeterinarian(newVet);
        assertEquals(newVet, medicalRecord.getVeterinarian());

        Appointment newAppointment = new Appointment();
        newAppointment.setId(2L);
        medicalRecord.setAppointment(newAppointment);
        assertEquals(newAppointment, medicalRecord.getAppointment());

        LocalDateTime newDate = LocalDateTime.now();
        medicalRecord.setRecordDate(newDate);
        assertEquals(newDate, medicalRecord.getRecordDate());

        medicalRecord.setDiagnosis("Nuevo diagnóstico");
        assertEquals("Nuevo diagnóstico", medicalRecord.getDiagnosis());

        medicalRecord.setTreatment("Nuevo tratamiento");
        assertEquals("Nuevo tratamiento", medicalRecord.getTreatment());

        medicalRecord.setSymptoms("Nuevos síntomas");
        assertEquals("Nuevos síntomas", medicalRecord.getSymptoms());

        medicalRecord.setVitalSigns("Nuevos signos vitales");
        assertEquals("Nuevos signos vitales", medicalRecord.getVitalSigns());

        BigDecimal newWeight = new BigDecimal("30.00");
        medicalRecord.setWeight(newWeight);
        assertEquals(newWeight, medicalRecord.getWeight());

        BigDecimal newTemp = new BigDecimal("39.0");
        medicalRecord.setTemperature(newTemp);
        assertEquals(newTemp, medicalRecord.getTemperature());

        medicalRecord.setNotes("Nuevas notas");
        assertEquals("Nuevas notas", medicalRecord.getNotes());

        medicalRecord.setFollowUpRequired(false);
        assertFalse(medicalRecord.getFollowUpRequired());

        LocalDateTime newFollowUpDate = LocalDateTime.now().plusDays(14);
        medicalRecord.setFollowUpDate(newFollowUpDate);
        assertEquals(newFollowUpDate, medicalRecord.getFollowUpDate());

        medicalRecord.setIsActive(false);
        assertFalse(medicalRecord.getIsActive());
    }

    @Test
    void testTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        medicalRecord.setCreatedAt(now);
        medicalRecord.setUpdatedAt(now);

        assertEquals(now, medicalRecord.getCreatedAt());
        assertEquals(now, medicalRecord.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        MedicalRecord emptyRecord = new MedicalRecord();
        assertNotNull(emptyRecord);
        assertNull(emptyRecord.getId());
        assertNull(emptyRecord.getPatient());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        MedicalRecord fullRecord = new MedicalRecord(
                1L,
                patient,
                appointment,
                veterinarian,
                now,
                "Diagnóstico",
                "Tratamiento",
                "Síntomas",
                "Signos vitales",
                new BigDecimal("25.0"),
                new BigDecimal("38.0"),
                new BigDecimal("80.0"), // heartRate
                "Notas",
                true,
                now.plusDays(7),
                true,
                new ArrayList<>(),
                now,
                now
        );

        assertNotNull(fullRecord);
        assertEquals(1L, fullRecord.getId());
        assertEquals("Diagnóstico", fullRecord.getDiagnosis());
    }
}