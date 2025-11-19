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
@DisplayName("MedicalRecordRepository Tests")
class MedicalRecordRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    private Owner testOwner;
    private Patient testPatient;
    private User testVeterinarian;
    private MedicalRecord medicalRecord1;
    private MedicalRecord medicalRecord2;
    private MedicalRecord medicalRecordWithFollowUp;

    @BeforeEach
    void setUp() {
        medicalRecordRepository.deleteAll();

        // Create test owner
        testOwner = new Owner();
        testOwner.setFirstName("Test");
        testOwner.setLastName("Owner");
        testOwner.setEmail("test.mr.owner@example.com");
        testOwner.setPhone("1234567890");
        testOwner.setIsActive(true);
        testOwner = entityManager.persist(testOwner);

        // Create test patient
        testPatient = new Patient();
        testPatient.setName("Luna");
        testPatient.setSpecies("Gato");
        testPatient.setBreed("Siamés");
        testPatient.setBirthDate(LocalDate.of(2021, 1, 1));
        testPatient.setGender("FEMALE");
        testPatient.setWeight(new BigDecimal("4.5"));
        testPatient.setOwner(testOwner);
        testPatient.setIsActive(true);
        testPatient = entityManager.persist(testPatient);

        // Create test veterinarian
        Role vetRole = Role.builder()
                .name("TEST_MR_VET")
                .description("Test Veterinarian")
                .isSystemRole(false)
                .permissions(new HashSet<>())
                .build();
        vetRole = entityManager.persist(vetRole);

        testVeterinarian = User.builder()
                .username("testmrvet")
                .email("testmrvet@example.com")
                .password("password")
                .firstName("Dr.")
                .lastName("Smith")
                .isActive(true)
                .roles(new HashSet<>())
                .build();
        testVeterinarian.getRoles().add(vetRole);
        testVeterinarian = entityManager.persist(testVeterinarian);

        // Create medical records
        medicalRecord1 = new MedicalRecord();
        medicalRecord1.setPatient(testPatient);
        medicalRecord1.setVeterinarian(testVeterinarian);
        medicalRecord1.setRecordDate(LocalDateTime.now().minusDays(10));
        medicalRecord1.setDiagnosis("Infección respiratoria leve");
        medicalRecord1.setTreatment("Antibióticos por 7 días");
        medicalRecord1.setSymptoms("Tos y estornudos");
        medicalRecord1.setVitalSigns("Temperatura: 38.5°C, Frecuencia cardíaca: 120 bpm");
        medicalRecord1.setWeight(new BigDecimal("4.3"));
        medicalRecord1.setTemperature(new BigDecimal("38.5"));
        medicalRecord1.setFollowUpRequired(false);
        medicalRecord1.setIsActive(true);
        medicalRecord1 = entityManager.persist(medicalRecord1);

        medicalRecord2 = new MedicalRecord();
        medicalRecord2.setPatient(testPatient);
        medicalRecord2.setVeterinarian(testVeterinarian);
        medicalRecord2.setRecordDate(LocalDateTime.now().minusDays(5));
        medicalRecord2.setDiagnosis("Gastritis aguda");
        medicalRecord2.setTreatment("Dieta blanda y medicación");
        medicalRecord2.setSymptoms("Vómitos y pérdida de apetito");
        medicalRecord2.setVitalSigns("Temperatura: 38.0°C");
        medicalRecord2.setWeight(new BigDecimal("4.2"));
        medicalRecord2.setTemperature(new BigDecimal("38.0"));
        medicalRecord2.setFollowUpRequired(false);
        medicalRecord2.setIsActive(true);
        medicalRecord2 = entityManager.persist(medicalRecord2);

        medicalRecordWithFollowUp = new MedicalRecord();
        medicalRecordWithFollowUp.setPatient(testPatient);
        medicalRecordWithFollowUp.setVeterinarian(testVeterinarian);
        medicalRecordWithFollowUp.setRecordDate(LocalDateTime.now().minusDays(2));
        medicalRecordWithFollowUp.setDiagnosis("Dermatitis alérgica");
        medicalRecordWithFollowUp.setTreatment("Antihistamínicos y baño medicado");
        medicalRecordWithFollowUp.setSymptoms("Picazón y enrojecimiento");
        medicalRecordWithFollowUp.setWeight(new BigDecimal("4.4"));
        medicalRecordWithFollowUp.setFollowUpRequired(true);
        medicalRecordWithFollowUp.setFollowUpDate(LocalDateTime.now().plusDays(7));
        medicalRecordWithFollowUp.setIsActive(true);
        medicalRecordWithFollowUp = entityManager.persist(medicalRecordWithFollowUp);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find active medical records with pagination")
    void shouldFindActiveMedicalRecordsWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<MedicalRecord> recordPage = medicalRecordRepository.findByIsActiveTrueOrderByRecordDateDesc(pageable);

        assertThat(recordPage).isNotEmpty();
        assertThat(recordPage.getTotalElements()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should find medical records by patient")
    void shouldFindMedicalRecordsByPatient() {
        List<MedicalRecord> records = medicalRecordRepository.findByPatientIdAndIsActiveTrueOrderByRecordDateDesc(testPatient.getId());

        assertThat(records).isNotEmpty();
        assertThat(records).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should find medical records by veterinarian")
    void shouldFindMedicalRecordsByVeterinarian() {
        List<MedicalRecord> records = medicalRecordRepository.findByVeterinarianIdAndIsActiveTrueOrderByRecordDateDesc(testVeterinarian.getId());

        assertThat(records).isNotEmpty();
        assertThat(records).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should find medical records by date range")
    void shouldFindMedicalRecordsByDateRange() {
        LocalDateTime start = LocalDateTime.now().minusDays(15);
        LocalDateTime end = LocalDateTime.now();

        List<MedicalRecord> records = medicalRecordRepository.findByDateRange(start, end);

        assertThat(records).isNotEmpty();
        assertThat(records).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should search medical records by diagnosis")
    void shouldSearchMedicalRecordsByDiagnosis() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<MedicalRecord> records = medicalRecordRepository.searchByDiagnosis("respiratoria", pageable);

        assertThat(records).isNotEmpty();
        assertThat(records.getContent().get(0).getDiagnosis()).containsIgnoringCase("respiratoria");
    }

    @Test
    @DisplayName("Should find records requiring follow up")
    void shouldFindRecordsRequiringFollowUp() {
        List<MedicalRecord> records = medicalRecordRepository.findRecordsRequiringFollowUp();

        assertThat(records).isNotEmpty();
        assertThat(records.get(0).getFollowUpRequired()).isTrue();
        assertThat(records.get(0).getFollowUpDate()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should find overdue follow ups")
    void shouldFindOverdueFollowUps() {
        // Create a record with overdue follow-up
        MedicalRecord overdueRecord = new MedicalRecord();
        overdueRecord.setPatient(testPatient);
        overdueRecord.setVeterinarian(testVeterinarian);
        overdueRecord.setRecordDate(LocalDateTime.now().minusDays(30));
        overdueRecord.setDiagnosis("Control pendiente");
        overdueRecord.setTreatment("Seguimiento");
        overdueRecord.setFollowUpRequired(true);
        overdueRecord.setFollowUpDate(LocalDateTime.now().minusDays(1));
        overdueRecord.setIsActive(true);
        entityManager.persist(overdueRecord);
        entityManager.flush();

        List<MedicalRecord> records = medicalRecordRepository.findOverdueFollowUps();

        assertThat(records).isNotEmpty();
        assertThat(records.get(0).getFollowUpDate()).isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should count active medical records")
    void shouldCountActiveMedicalRecords() {
        long count = medicalRecordRepository.countByIsActiveTrue();

        assertThat(count).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should find latest medical record by patient")
    void shouldFindLatestMedicalRecordByPatient() {
        MedicalRecord latestRecord = medicalRecordRepository.findLatestByPatientId(testPatient.getId());

        assertThat(latestRecord).isNotNull();
        assertThat(latestRecord.getDiagnosis()).isEqualTo("Dermatitis alérgica");
    }

    @Test
    @DisplayName("Should save medical record successfully")
    void shouldSaveMedicalRecordSuccessfully() {
        MedicalRecord newRecord = new MedicalRecord();
        newRecord.setPatient(testPatient);
        newRecord.setVeterinarian(testVeterinarian);
        newRecord.setRecordDate(LocalDateTime.now());
        newRecord.setDiagnosis("Chequeo rutinario");
        newRecord.setTreatment("Ninguno requerido");
        newRecord.setSymptoms("Ninguno");
        newRecord.setWeight(new BigDecimal("4.5"));
        newRecord.setFollowUpRequired(false);
        newRecord.setIsActive(true);

        MedicalRecord savedRecord = medicalRecordRepository.save(newRecord);

        assertThat(savedRecord).isNotNull();
        assertThat(savedRecord.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should update medical record successfully")
    void shouldUpdateMedicalRecordSuccessfully() {
        medicalRecord1.setNotes("Evolución favorable");

        MedicalRecord updatedRecord = medicalRecordRepository.save(medicalRecord1);

        assertThat(updatedRecord.getNotes()).isEqualTo("Evolución favorable");
    }

    @Test
    @DisplayName("Should delete medical record successfully")
    void shouldDeleteMedicalRecordSuccessfully() {
        medicalRecordRepository.delete(medicalRecord2);
        entityManager.flush();

        List<MedicalRecord> records = medicalRecordRepository.findByPatientIdAndIsActiveTrueOrderByRecordDateDesc(testPatient.getId());

        assertThat(records).hasSizeLessThan(3);
    }
}