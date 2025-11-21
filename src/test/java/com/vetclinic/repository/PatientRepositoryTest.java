package com.vetclinic.repository;

import com.vetclinic.entity.Owner;
import com.vetclinic.entity.Patient;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("PatientRepository Tests")
class PatientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PatientRepository patientRepository;

    private Owner testOwner;
    private Patient activePatient1;
    private Patient activePatient2;
    private Patient inactivePatient;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();

        testOwner = new Owner();
        testOwner.setFirstName("John");
        testOwner.setLastName("Doe");
        testOwner.setEmail("test.patient.owner@example.com");
        testOwner.setPhone("1234567890");
        testOwner.setIsActive(true);
        testOwner = entityManager.persist(testOwner);

        activePatient1 = new Patient();
        activePatient1.setName("Max");
        activePatient1.setSpecies("Perro");
        activePatient1.setBreed("Labrador");
        activePatient1.setBirthDate(LocalDate.of(2020, 5, 15));
        activePatient1.setGender("MALE");
        activePatient1.setWeight(new BigDecimal("25.5"));
        activePatient1.setMicrochipNumber("TEST_PATIENT_CHIP123");
        activePatient1.setOwner(testOwner);
        activePatient1.setIsActive(true);
        activePatient1 = entityManager.persist(activePatient1);

        activePatient2 = new Patient();
        activePatient2.setName("Luna");
        activePatient2.setSpecies("Gato");
        activePatient2.setBreed("Siamés");
        activePatient2.setBirthDate(LocalDate.of(2021, 3, 20));
        activePatient2.setGender("FEMALE");
        activePatient2.setWeight(new BigDecimal("4.2"));
        activePatient2.setMicrochipNumber("TEST_PATIENT_CHIP456");
        activePatient2.setOwner(testOwner);
        activePatient2.setIsActive(true);
        activePatient2 = entityManager.persist(activePatient2);

        inactivePatient = new Patient();
        inactivePatient.setName("Rocky");
        inactivePatient.setSpecies("Perro");
        inactivePatient.setBreed("Bulldog");
        inactivePatient.setBirthDate(LocalDate.of(2019, 8, 10));
        inactivePatient.setGender("MALE");
        inactivePatient.setWeight(new BigDecimal("20.0"));
        inactivePatient.setMicrochipNumber("TEST_PATIENT_CHIP789");
        inactivePatient.setOwner(testOwner);
        inactivePatient.setIsActive(false);
        inactivePatient = entityManager.persist(inactivePatient);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find all active patients")
    void shouldFindAllActivePatients() {
        List<Patient> activePatients = patientRepository.findByIsActiveTrue();

        assertThat(activePatients).isNotEmpty();
        assertThat(activePatients).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should find patient by microchip number")
    void shouldFindPatientByMicrochipNumber() {
        Optional<Patient> foundPatient = patientRepository.findByMicrochipNumber("TEST_PATIENT_CHIP123");

        assertThat(foundPatient).isPresent();
        assertThat(foundPatient.get().getName()).isEqualTo("Max");
        assertThat(foundPatient.get().getSpecies()).isEqualTo("Perro");
    }

    @Test
    @DisplayName("Should return empty when microchip not found")
    void shouldReturnEmptyWhenMicrochipNotFound() {
        Optional<Patient> foundPatient = patientRepository.findByMicrochipNumber("NONEXISTENT");

        assertThat(foundPatient).isEmpty();
    }

    @Test
    @DisplayName("Should find patients by owner id")
    void shouldFindPatientsByOwnerId() {
        List<Patient> ownerPatients = patientRepository.findByOwnerId(testOwner.getId());

        assertThat(ownerPatients).isNotEmpty();
        assertThat(ownerPatients).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should find patients by species")
    void shouldFindPatientsBySpecies() {
        List<Patient> dogs = patientRepository.findBySpecies("Perro");

        assertThat(dogs).isNotEmpty();
        assertThat(dogs).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should search patients by name")
    void shouldSearchPatientsByName() {
        List<Patient> foundPatients = patientRepository.searchByName("Max");

        assertThat(foundPatients).isNotEmpty();
        assertThat(foundPatients.get(0).getName()).isEqualTo("Max");
    }

    @Test
    @DisplayName("Should search patients by partial name")
    void shouldSearchPatientsByPartialName() {
        List<Patient> foundPatients = patientRepository.searchByName("Lu");

        assertThat(foundPatients).isNotEmpty();
    }

    @Test
    @DisplayName("Should find active patients with pagination")
    void shouldFindActivePatientsWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Patient> patientPage = patientRepository.findByIsActiveTrue(pageable);

        assertThat(patientPage).isNotEmpty();
        assertThat(patientPage.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should count active patients")
    void shouldCountActivePatients() {
        long count = patientRepository.countByIsActiveTrue();

        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should return true when microchip exists")
    void shouldReturnTrueWhenMicrochipExists() {
        boolean exists = patientRepository.existsByMicrochipNumber("TEST_PATIENT_CHIP123");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when microchip does not exist")
    void shouldReturnFalseWhenMicrochipDoesNotExist() {
        boolean exists = patientRepository.existsByMicrochipNumber("NONEXISTENT");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should save patient successfully")
    void shouldSavePatientSuccessfully() {
        Patient newPatient = new Patient();
        newPatient.setName("Buddy");
        newPatient.setSpecies("Perro");
        newPatient.setBreed("Golden Retriever");
        newPatient.setBirthDate(LocalDate.of(2022, 1, 15));
        newPatient.setGender("MALE");
        newPatient.setWeight(new BigDecimal("30.0"));
        newPatient.setMicrochipNumber("TEST_PATIENT_NEW_CHIP");
        newPatient.setOwner(testOwner);
        newPatient.setIsActive(true);

        Patient savedPatient = patientRepository.save(newPatient);

        assertThat(savedPatient).isNotNull();
        assertThat(savedPatient.getId()).isNotNull();
        assertThat(savedPatient.getName()).isEqualTo("Buddy");
    }

    @Test
    @DisplayName("Should update patient successfully")
    void shouldUpdatePatientSuccessfully() {
        activePatient1.setWeight(new BigDecimal("26.0"));

        Patient updatedPatient = patientRepository.save(activePatient1);

        assertThat(updatedPatient.getWeight()).isEqualByComparingTo("26.0");
    }

    @Test
    @DisplayName("Should delete patient successfully")
    void shouldDeletePatientSuccessfully() {
        patientRepository.delete(activePatient2);
        patientRepository.flush();

        Optional<Patient> foundPatient = patientRepository.findById(activePatient2.getId());

        assertThat(foundPatient).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when searching nonexistent name")
    void shouldReturnEmptyListWhenSearchingNonexistentName() {
        List<Patient> foundPatients = patientRepository.searchByName("Nonexistent");

        assertThat(foundPatients).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list for owner with no patients")
    void shouldReturnEmptyListForOwnerWithNoPatients() {
        List<Patient> patients = patientRepository.findByOwnerId(999999L);

        assertThat(patients).isEmpty();
    }
}