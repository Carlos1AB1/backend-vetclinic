package com.vetclinic.entity;

import com.vetclinic.entity.Appointment.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentTest {

    private Appointment appointment;
    private Patient patient;
    private Owner owner;
    private User veterinarian;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setName("Max");

        owner = new Owner();
        owner.setId(1L);
        owner.setFirstName("John");
        owner.setLastName("Doe");

        veterinarian = User.builder()
                .id(UUID.randomUUID())
                .username("drsmith")
                .firstName("Dr. Jane")
                .lastName("Smith")
                .build();

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setPatient(patient);
        appointment.setOwner(owner);
        appointment.setVeterinarian(veterinarian);
        appointment.setScheduledDate(LocalDateTime.now().plusDays(1));
        appointment.setAppointmentType("CONSULTATION");
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReason("Chequeo anual");
        appointment.setNotes("Primera consulta");
        appointment.setDurationMinutes(30);
        appointment.setIsActive(true);
    }

    @Test
    void testAppointmentCreation() {
        assertNotNull(appointment);
        assertEquals(1L, appointment.getId());
        assertEquals(patient, appointment.getPatient());
        assertEquals(owner, appointment.getOwner());
        assertEquals(veterinarian, appointment.getVeterinarian());
        assertNotNull(appointment.getScheduledDate());
        assertEquals("CONSULTATION", appointment.getAppointmentType());
        assertEquals(AppointmentStatus.SCHEDULED, appointment.getStatus());
        assertEquals("Chequeo anual", appointment.getReason());
        assertEquals("Primera consulta", appointment.getNotes());
        assertEquals(30, appointment.getDurationMinutes());
        assertTrue(appointment.getIsActive());
    }

    @Test
    void testDefaultIsActive() {
        Appointment newAppointment = new Appointment();
        newAppointment.setIsActive(true);
        assertTrue(newAppointment.getIsActive());
    }

    @Test
    void testIsPending_Scheduled() {
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        assertTrue(appointment.isPending());
    }

    @Test
    void testIsPending_Confirmed() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        assertTrue(appointment.isPending());
    }

    @Test
    void testIsPending_InProgress() {
        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        assertFalse(appointment.isPending());
    }

    @Test
    void testIsPending_Completed() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        assertFalse(appointment.isPending());
    }

    @Test
    void testIsFinished_Completed() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        assertTrue(appointment.isFinished());
    }

    @Test
    void testIsFinished_Cancelled() {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        assertTrue(appointment.isFinished());
    }

    @Test
    void testIsFinished_NoShow() {
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        assertTrue(appointment.isFinished());
    }

    @Test
    void testIsFinished_Scheduled() {
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        assertFalse(appointment.isFinished());
    }

    @Test
    void testCanBeCancelled_Scheduled() {
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        assertTrue(appointment.canBeCancelled());
    }

    @Test
    void testCanBeCancelled_Confirmed() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        assertTrue(appointment.canBeCancelled());
    }

    @Test
    void testCanBeCancelled_InProgress() {
        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        assertFalse(appointment.canBeCancelled());
    }

    @Test
    void testCanBeCancelled_Completed() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        assertFalse(appointment.canBeCancelled());
    }

    @Test
    void testSettersAndGetters() {
        appointment.setId(2L);
        assertEquals(2L, appointment.getId());

        Patient newPatient = new Patient();
        newPatient.setId(2L);
        appointment.setPatient(newPatient);
        assertEquals(newPatient, appointment.getPatient());

        Owner newOwner = new Owner();
        newOwner.setId(2L);
        appointment.setOwner(newOwner);
        assertEquals(newOwner, appointment.getOwner());

        User newVet = User.builder().id(UUID.randomUUID()).build();
        appointment.setVeterinarian(newVet);
        assertEquals(newVet, appointment.getVeterinarian());

        LocalDateTime newDate = LocalDateTime.now().plusDays(2);
        appointment.setScheduledDate(newDate);
        assertEquals(newDate, appointment.getScheduledDate());

        appointment.setAppointmentType("SURGERY");
        assertEquals("SURGERY", appointment.getAppointmentType());

        appointment.setStatus(AppointmentStatus.COMPLETED);
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());

        appointment.setReason("Cirugía");
        assertEquals("Cirugía", appointment.getReason());

        appointment.setNotes("Notas actualizadas");
        assertEquals("Notas actualizadas", appointment.getNotes());

        appointment.setDurationMinutes(60);
        assertEquals(60, appointment.getDurationMinutes());

        appointment.setIsActive(false);
        assertFalse(appointment.getIsActive());
    }

    @Test
    void testTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);

        assertEquals(now, appointment.getCreatedAt());
        assertEquals(now, appointment.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        Appointment emptyAppointment = new Appointment();
        assertNotNull(emptyAppointment);
        assertNull(emptyAppointment.getId());
        assertNull(emptyAppointment.getPatient());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Appointment fullAppointment = new Appointment(
                1L,
                patient,
                owner,
                veterinarian,
                now,
                "CHECKUP",
                AppointmentStatus.SCHEDULED,
                "Razón",
                "Notas",
                45,
                true,
                now,
                now
        );

        assertNotNull(fullAppointment);
        assertEquals(1L, fullAppointment.getId());
        assertEquals("CHECKUP", fullAppointment.getAppointmentType());
    }

    @Test
    void testAllAppointmentStatuses() {
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        assertEquals(AppointmentStatus.SCHEDULED, appointment.getStatus());

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        assertEquals(AppointmentStatus.IN_PROGRESS, appointment.getStatus());

        appointment.setStatus(AppointmentStatus.COMPLETED);
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());

        appointment.setStatus(AppointmentStatus.CANCELLED);
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        assertEquals(AppointmentStatus.NO_SHOW, appointment.getStatus());
    }
}