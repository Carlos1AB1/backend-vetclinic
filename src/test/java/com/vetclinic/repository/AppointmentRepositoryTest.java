package com.vetclinic.repository;

import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.Appointment.AppointmentStatus;
import com.vetclinic.entity.Owner;
import com.vetclinic.entity.Patient;
import com.vetclinic.entity.Role;
import com.vetclinic.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("AppointmentRepository Tests")
class AppointmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Owner testOwner;
    private Patient testPatient;
    private User testVeterinarian;
    private Appointment scheduledAppointment;
    private Appointment confirmedAppointment;
    private Appointment completedAppointment;
    private LocalDateTime tomorrow;
    private LocalDateTime nextWeek;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();

        tomorrow = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        nextWeek = LocalDateTime.now().plusDays(7).withHour(14).withMinute(0);

        // Create test owner
        testOwner = new Owner();
        testOwner.setFirstName("Test");
        testOwner.setLastName("Owner");
        testOwner.setEmail("test.appt.owner@example.com");
        testOwner.setPhone("1234567890");
        testOwner.setIsActive(true);
        testOwner = entityManager.persist(testOwner);

        // Create test patient
        testPatient = new Patient();
        testPatient.setName("Max");
        testPatient.setSpecies("Perro");
        testPatient.setBreed("Labrador");
        testPatient.setBirthDate(LocalDate.of(2020, 1, 1));
        testPatient.setGender("MALE");
        testPatient.setWeight(new BigDecimal("25.0"));
        testPatient.setOwner(testOwner);
        testPatient.setIsActive(true);
        testPatient = entityManager.persist(testPatient);

        // Create test veterinarian
        Role vetRole = Role.builder()
                .name("TEST_APPT_VET")
                .description("Test Veterinarian")
                .isSystemRole(false)
                .permissions(new HashSet<>())
                .build();
        vetRole = entityManager.persist(vetRole);

        testVeterinarian = User.builder()
                .username("testvet")
                .email("testvet@example.com")
                .password("password")
                .firstName("Test")
                .lastName("Veterinarian")
                .isActive(true)
                .roles(new HashSet<>())
                .build();
        testVeterinarian.getRoles().add(vetRole);
        testVeterinarian = entityManager.persist(testVeterinarian);

        // Create appointments
        scheduledAppointment = new Appointment();
        scheduledAppointment.setPatient(testPatient);
        scheduledAppointment.setOwner(testOwner);
        scheduledAppointment.setVeterinarian(testVeterinarian);
        scheduledAppointment.setScheduledDate(tomorrow);
        scheduledAppointment.setAppointmentType("CONSULTATION");
        scheduledAppointment.setStatus(AppointmentStatus.SCHEDULED);
        scheduledAppointment.setReason("Chequeo general");
        scheduledAppointment.setDurationMinutes(30);
        scheduledAppointment.setIsActive(true);
        scheduledAppointment = entityManager.persist(scheduledAppointment);

        confirmedAppointment = new Appointment();
        confirmedAppointment.setPatient(testPatient);
        confirmedAppointment.setOwner(testOwner);
        confirmedAppointment.setVeterinarian(testVeterinarian);
        confirmedAppointment.setScheduledDate(nextWeek);
        confirmedAppointment.setAppointmentType("VACCINATION");
        confirmedAppointment.setStatus(AppointmentStatus.CONFIRMED);
        confirmedAppointment.setReason("Vacunación anual");
        confirmedAppointment.setDurationMinutes(15);
        confirmedAppointment.setIsActive(true);
        confirmedAppointment = entityManager.persist(confirmedAppointment);

        completedAppointment = new Appointment();
        completedAppointment.setPatient(testPatient);
        completedAppointment.setOwner(testOwner);
        completedAppointment.setVeterinarian(testVeterinarian);
        completedAppointment.setScheduledDate(LocalDateTime.now().minusDays(1));
        completedAppointment.setAppointmentType("CHECKUP");
        completedAppointment.setStatus(AppointmentStatus.COMPLETED);
        completedAppointment.setReason("Revisión post-operatoria");
        completedAppointment.setDurationMinutes(45);
        completedAppointment.setIsActive(true);
        completedAppointment = entityManager.persist(completedAppointment);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find active appointments with pagination")
    void shouldFindActiveAppointmentsWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Appointment> appointmentPage = appointmentRepository.findByIsActiveTrueOrderByScheduledDateDesc(pageable);

        assertThat(appointmentPage).isNotEmpty();
        assertThat(appointmentPage.getTotalElements()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should find appointments by patient")
    void shouldFindAppointmentsByPatient() {
        List<Appointment> appointments = appointmentRepository.findByPatientIdAndIsActiveTrueOrderByScheduledDateDesc(testPatient.getId());

        assertThat(appointments).isNotEmpty();
        assertThat(appointments).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should find appointments by owner")
    void shouldFindAppointmentsByOwner() {
        List<Appointment> appointments = appointmentRepository.findByOwnerIdAndIsActiveTrueOrderByScheduledDateDesc(testOwner.getId());

        assertThat(appointments).isNotEmpty();
        assertThat(appointments).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should find appointments by veterinarian")
    void shouldFindAppointmentsByVeterinarian() {
        List<Appointment> appointments = appointmentRepository.findByVeterinarianIdAndIsActiveTrueOrderByScheduledDateDesc(testVeterinarian.getId());

        assertThat(appointments).isNotEmpty();
        assertThat(appointments).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should find appointments by status")
    void shouldFindAppointmentsByStatus() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Appointment> appointments = appointmentRepository.findByStatusAndIsActiveTrueOrderByScheduledDateDesc(AppointmentStatus.SCHEDULED, pageable);

        assertThat(appointments).isNotEmpty();
        assertThat(appointments.getContent().get(0).getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Should find appointments by date range")
    void shouldFindAppointmentsByDateRange() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(10);

        List<Appointment> appointments = appointmentRepository.findByDateRange(start, end);

        assertThat(appointments).isNotEmpty();
        assertThat(appointments).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should find appointments by veterinarian and date range")
    void shouldFindAppointmentsByVeterinarianAndDateRange() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(10);

        List<Appointment> appointments = appointmentRepository.findByVeterinarianAndDateRange(
                testVeterinarian.getId(), start, end);

        assertThat(appointments).isNotEmpty();
        assertThat(appointments).hasSizeGreaterThanOrEqualTo(2);
    }


    @Test
    @DisplayName("Should not detect conflict when no appointment exists")
    void shouldNotDetectConflictWhenNoAppointmentExists() {
        LocalDateTime futureDate = LocalDateTime.now().plusMonths(1);

        boolean hasConflict = appointmentRepository.existsConflict(
                testVeterinarian.getId(), futureDate);

        assertThat(hasConflict).isFalse();
    }

    @Test
    @DisplayName("Should detect conflict excluding specific appointment")
    void shouldDetectConflictExcludingSpecificAppointment() {
        boolean hasConflict = appointmentRepository.existsConflictExcluding(
                testVeterinarian.getId(), tomorrow, scheduledAppointment.getId());

        assertThat(hasConflict).isFalse();
    }

    @Test
    @DisplayName("Should count active appointments")
    void shouldCountActiveAppointments() {
        long count = appointmentRepository.countByIsActiveTrue();

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should count appointments by status")
    void shouldCountAppointmentsByStatus() {
        long count = appointmentRepository.countByStatusAndIsActiveTrue(AppointmentStatus.SCHEDULED);

        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should find upcoming appointments")
    void shouldFindUpcomingAppointments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(7);

        List<Appointment> appointments = appointmentRepository.findUpcomingAppointments(now, endDate);

        assertThat(appointments).isNotEmpty();
    }

    @Test
    @DisplayName("Should find today appointments")
    void shouldFindTodayAppointments() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59);

        List<Appointment> appointments = appointmentRepository.findTodayAppointments(
                testVeterinarian.getId(), startOfDay, endOfDay);

        assertThat(appointments).isNotNull();
    }

    @Test
    @DisplayName("Should save appointment successfully")
    void shouldSaveAppointmentSuccessfully() {
        Appointment newAppointment = new Appointment();
        newAppointment.setPatient(testPatient);
        newAppointment.setOwner(testOwner);
        newAppointment.setVeterinarian(testVeterinarian);
        newAppointment.setScheduledDate(LocalDateTime.now().plusDays(2));
        newAppointment.setAppointmentType("EMERGENCY");
        newAppointment.setStatus(AppointmentStatus.SCHEDULED);
        newAppointment.setReason("Emergencia");
        newAppointment.setDurationMinutes(60);
        newAppointment.setIsActive(true);

        Appointment savedAppointment = appointmentRepository.save(newAppointment);

        assertThat(savedAppointment).isNotNull();
        assertThat(savedAppointment.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should update appointment successfully")
    void shouldUpdateAppointmentSuccessfully() {
        scheduledAppointment.setStatus(AppointmentStatus.CONFIRMED);

        Appointment updatedAppointment = appointmentRepository.save(scheduledAppointment);

        assertThat(updatedAppointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should delete appointment successfully")
    void shouldDeleteAppointmentSuccessfully() {
        appointmentRepository.delete(completedAppointment);
        entityManager.flush();

        List<Appointment> appointments = appointmentRepository.findByPatientIdAndIsActiveTrueOrderByScheduledDateDesc(testPatient.getId());

        assertThat(appointments).hasSizeLessThan(3);
    }
}