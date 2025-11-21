package com.vetclinic.repository;

import com.vetclinic.entity.*;
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
@DisplayName("PrescriptionRepository Tests")
class PrescriptionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    private Owner testOwner;
    private Patient testPatient;
    private User testVeterinarian;
    private MedicalRecord testMedicalRecord;
    private Prescription activePrescription;
    private Prescription expiringPrescription;
    private Prescription expiredPrescription;

    @BeforeEach
    void setUp() {
        prescriptionRepository.deleteAll();

        // Create test owner
        testOwner = new Owner();
        testOwner.setFirstName("Test");
        testOwner.setLastName("Owner");
        testOwner.setEmail("test.presc.owner@example.com");
        testOwner.setPhone("1234567890");
        testOwner.setIsActive(true);
        testOwner = entityManager.persist(testOwner);

        // Create test patient
        testPatient = new Patient();
        testPatient.setName("Buddy");
        testPatient.setSpecies("Perro");
        testPatient.setBreed("Golden Retriever");
        testPatient.setBirthDate(LocalDate.of(2019, 5, 15));
        testPatient.setGender("MALE");
        testPatient.setWeight(new BigDecimal("30.0"));
        testPatient.setOwner(testOwner);
        testPatient.setIsActive(true);
        testPatient = entityManager.persist(testPatient);

        // Create test veterinarian
        Role vetRole = Role.builder()
                .name("TEST_PRESC_VET")
                .description("Test Veterinarian")
                .isSystemRole(false)
                .permissions(new HashSet<>())
                .build();
        vetRole = entityManager.persist(vetRole);

        testVeterinarian = User.builder()
                .username("testprescvet")
                .email("testprescvet@example.com")
                .password("password")
                .firstName("Dr.")
                .lastName("Johnson")
                .isActive(true)
                .roles(new HashSet<>())
                .build();
        testVeterinarian.getRoles().add(vetRole);
        testVeterinarian = entityManager.persist(testVeterinarian);

        // Create test medical record
        testMedicalRecord = new MedicalRecord();
        testMedicalRecord.setPatient(testPatient);
        testMedicalRecord.setVeterinarian(testVeterinarian);
        testMedicalRecord.setRecordDate(LocalDateTime.now().minusDays(5));
        testMedicalRecord.setDiagnosis("Artritis");
        testMedicalRecord.setTreatment("Antiinflamatorios");
        testMedicalRecord.setIsActive(true);
        testMedicalRecord = entityManager.persist(testMedicalRecord);

        // Create prescriptions
        activePrescription = new Prescription();
        activePrescription.setMedicalRecord(testMedicalRecord);
        activePrescription.setPatient(testPatient);
        activePrescription.setMedicationName("Carprofeno");
        activePrescription.setDosage("75mg");
        activePrescription.setFrequency("Cada 12 horas");
        activePrescription.setDuration("30 días");
        activePrescription.setInstructions("Administrar con comida");
        activePrescription.setStartDate(LocalDateTime.now().minusDays(5));
        activePrescription.setEndDate(LocalDateTime.now().plusDays(25));
        activePrescription.setIsActive(true);
        activePrescription = entityManager.persist(activePrescription);

        expiringPrescription = new Prescription();
        expiringPrescription.setMedicalRecord(testMedicalRecord);
        expiringPrescription.setPatient(testPatient);
        expiringPrescription.setMedicationName("Glucosamina");
        expiringPrescription.setDosage("500mg");
        expiringPrescription.setFrequency("Una vez al día");
        expiringPrescription.setDuration("7 días");
        expiringPrescription.setInstructions("Tomar en la mañana");
        expiringPrescription.setStartDate(LocalDateTime.now().minusDays(1));
        expiringPrescription.setEndDate(LocalDateTime.now().plusDays(3));
        expiringPrescription.setIsActive(true);
        expiringPrescription = entityManager.persist(expiringPrescription);

        expiredPrescription = new Prescription();
        expiredPrescription.setMedicalRecord(testMedicalRecord);
        expiredPrescription.setPatient(testPatient);
        expiredPrescription.setMedicationName("Amoxicilina");
        expiredPrescription.setDosage("250mg");
        expiredPrescription.setFrequency("Cada 8 horas");
        expiredPrescription.setDuration("10 días");
        expiredPrescription.setInstructions("Completar tratamiento");
        expiredPrescription.setStartDate(LocalDateTime.now().minusDays(15));
        expiredPrescription.setEndDate(LocalDateTime.now().minusDays(5));
        expiredPrescription.setIsActive(true);
        expiredPrescription = entityManager.persist(expiredPrescription);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find active prescriptions with pagination")
    void shouldFindActivePrescriptionsWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Prescription> prescriptionPage = prescriptionRepository.findByIsActiveTrueOrderByStartDateDesc(pageable);

        assertThat(prescriptionPage).isNotEmpty();
        assertThat(prescriptionPage.getTotalElements()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should find prescriptions by patient")
    void shouldFindPrescriptionsByPatient() {
        List<Prescription> prescriptions = prescriptionRepository.findByPatientIdAndIsActiveTrueOrderByStartDateDesc(testPatient.getId());

        assertThat(prescriptions).isNotEmpty();
        assertThat(prescriptions).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should find prescriptions by medical record")
    void shouldFindPrescriptionsByMedicalRecord() {
        List<Prescription> prescriptions = prescriptionRepository.findByMedicalRecordIdAndIsActiveTrueOrderByStartDateDesc(testMedicalRecord.getId());

        assertThat(prescriptions).isNotEmpty();
        assertThat(prescriptions).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should find active prescriptions by patient")
    void shouldFindActivePrescriptionsByPatient() {
        List<Prescription> prescriptions = prescriptionRepository.findActivePrescriptionsByPatient(testPatient.getId());

        assertThat(prescriptions).isNotEmpty();
        // Should only return prescriptions within their active period
        prescriptions.forEach(prescription -> {
            assertThat(prescription.getStartDate()).isBeforeOrEqualTo(LocalDateTime.now());
            if (prescription.getEndDate() != null) {
                assertThat(prescription.getEndDate()).isAfterOrEqualTo(LocalDateTime.now());
            }
        });
    }

    @Test
    @DisplayName("Should search prescriptions by medication")
    void shouldSearchPrescriptionsByMedication() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Prescription> prescriptions = prescriptionRepository.searchByMedication("Carprofeno", pageable);

        assertThat(prescriptions).isNotEmpty();
        assertThat(prescriptions.getContent().get(0).getMedicationName()).containsIgnoringCase("Carprofeno");
    }

    @Test
    @DisplayName("Should find expiring prescriptions")
    void shouldFindExpiringPrescriptions() {
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);

        List<Prescription> prescriptions = prescriptionRepository.findExpiringPrescriptions(endDate);

        assertThat(prescriptions).isNotEmpty();
        prescriptions.forEach(prescription -> {
            assertThat(prescription.getEndDate()).isBetween(LocalDateTime.now(), endDate);
        });
    }

    @Test
    @DisplayName("Should find expired prescriptions")
    void shouldFindExpiredPrescriptions() {
        List<Prescription> prescriptions = prescriptionRepository.findExpiredPrescriptions();

        assertThat(prescriptions).isNotEmpty();
        assertThat(prescriptions.get(0).getEndDate()).isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should count active prescriptions")
    void shouldCountActivePrescriptions() {
        long count = prescriptionRepository.countByIsActiveTrue();

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should count active prescriptions by patient")
    void shouldCountActivePrescriptionsByPatient() {
        long count = prescriptionRepository.countActivePrescriptionsByPatient(testPatient.getId());

        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should save prescription successfully")
    void shouldSavePrescriptionSuccessfully() {
        Prescription newPrescription = new Prescription();
        newPrescription.setMedicalRecord(testMedicalRecord);
        newPrescription.setPatient(testPatient);
        newPrescription.setMedicationName("Tramadol");
        newPrescription.setDosage("50mg");
        newPrescription.setFrequency("Cada 12 horas");
        newPrescription.setDuration("5 días");
        newPrescription.setInstructions("Para el dolor");
        newPrescription.setStartDate(LocalDateTime.now());
        newPrescription.setEndDate(LocalDateTime.now().plusDays(5));
        newPrescription.setIsActive(true);

        Prescription savedPrescription = prescriptionRepository.save(newPrescription);

        assertThat(savedPrescription).isNotNull();
        assertThat(savedPrescription.getId()).isNotNull();
        assertThat(savedPrescription.getMedicationName()).isEqualTo("Tramadol");
    }

    @Test
    @DisplayName("Should update prescription successfully")
    void shouldUpdatePrescriptionSuccessfully() {
        activePrescription.setNotes("Paciente tolera bien la medicación");

        Prescription updatedPrescription = prescriptionRepository.save(activePrescription);

        assertThat(updatedPrescription.getNotes()).isEqualTo("Paciente tolera bien la medicación");
    }

    @Test
    @DisplayName("Should delete prescription successfully")
    void shouldDeletePrescriptionSuccessfully() {
        prescriptionRepository.delete(expiredPrescription);
        entityManager.flush();

        List<Prescription> prescriptions = prescriptionRepository.findByPatientIdAndIsActiveTrueOrderByStartDateDesc(testPatient.getId());

        assertThat(prescriptions).hasSizeLessThan(3);
    }
}