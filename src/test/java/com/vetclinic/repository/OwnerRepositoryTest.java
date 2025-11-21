package com.vetclinic.repository;

import com.vetclinic.entity.Owner;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("OwnerRepository Tests")
class OwnerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OwnerRepository ownerRepository;

    private Owner activeOwner1;
    private Owner activeOwner2;
    private Owner inactiveOwner;

    @BeforeEach
    void setUp() {
        ownerRepository.deleteAll();

        activeOwner1 = new Owner();
        activeOwner1.setFirstName("Juan");
        activeOwner1.setLastName("Pérez");
        activeOwner1.setEmail("juan.perez@example.com");
        activeOwner1.setPhone("1234567890");
        activeOwner1.setAddress("Calle 123");
        activeOwner1.setCity("Bogotá");
        activeOwner1.setPostalCode("110111");
        activeOwner1.setDocumentType("DNI");
        activeOwner1.setDocumentNumber("12345678");
        activeOwner1.setIsActive(true);
        activeOwner1 = entityManager.persist(activeOwner1);

        activeOwner2 = new Owner();
        activeOwner2.setFirstName("María");
        activeOwner2.setLastName("García");
        activeOwner2.setEmail("maria.garcia@example.com");
        activeOwner2.setPhone("9876543210");
        activeOwner2.setAddress("Avenida 456");
        activeOwner2.setCity("Medellín");
        activeOwner2.setPostalCode("050001");
        activeOwner2.setDocumentType("DNI");
        activeOwner2.setDocumentNumber("87654321");
        activeOwner2.setIsActive(true);
        activeOwner2 = entityManager.persist(activeOwner2);

        inactiveOwner = new Owner();
        inactiveOwner.setFirstName("Carlos");
        inactiveOwner.setLastName("Rodríguez");
        inactiveOwner.setEmail("carlos.rodriguez@example.com");
        inactiveOwner.setPhone("5555555555");
        inactiveOwner.setAddress("Carrera 789");
        inactiveOwner.setCity("Bogotá");
        inactiveOwner.setPostalCode("110111");
        inactiveOwner.setDocumentType("DNI");
        inactiveOwner.setDocumentNumber("11111111");
        inactiveOwner.setIsActive(false);
        inactiveOwner = entityManager.persist(inactiveOwner);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find owner by email and active")
    void shouldFindOwnerByEmailAndActive() {
        Optional<Owner> foundOwner = ownerRepository.findByEmailAndIsActiveTrue("juan.perez@example.com");

        assertThat(foundOwner).isPresent();
        assertThat(foundOwner.get().getFirstName()).isEqualTo("Juan");
        assertThat(foundOwner.get().getLastName()).isEqualTo("Pérez");
        assertThat(foundOwner.get().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should return empty when email not found or inactive")
    void shouldReturnEmptyWhenEmailNotFoundOrInactive() {
        Optional<Owner> foundOwner = ownerRepository.findByEmailAndIsActiveTrue("carlos.rodriguez@example.com");

        assertThat(foundOwner).isEmpty();
    }

    @Test
    @DisplayName("Should find owner by document number and active")
    void shouldFindOwnerByDocumentNumberAndActive() {
        Optional<Owner> foundOwner = ownerRepository.findByDocumentNumberAndIsActiveTrue("12345678");

        assertThat(foundOwner).isPresent();
        assertThat(foundOwner.get().getFirstName()).isEqualTo("Juan");
        assertThat(foundOwner.get().getDocumentNumber()).isEqualTo("12345678");
    }

    @Test
    @DisplayName("Should return empty when document number not found")
    void shouldReturnEmptyWhenDocumentNumberNotFound() {
        Optional<Owner> foundOwner = ownerRepository.findByDocumentNumberAndIsActiveTrue("99999999");

        assertThat(foundOwner).isEmpty();
    }

    @Test
    @DisplayName("Should return true when email exists and active")
    void shouldReturnTrueWhenEmailExistsAndActive() {
        boolean exists = ownerRepository.existsByEmailAndIsActiveTrue("juan.perez@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist or inactive")
    void shouldReturnFalseWhenEmailDoesNotExistOrInactive() {
        boolean exists = ownerRepository.existsByEmailAndIsActiveTrue("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should return true when document number exists and active")
    void shouldReturnTrueWhenDocumentNumberExistsAndActive() {
        boolean exists = ownerRepository.existsByDocumentNumberAndIsActiveTrue("12345678");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when document number does not exist")
    void shouldReturnFalseWhenDocumentNumberDoesNotExist() {
        boolean exists = ownerRepository.existsByDocumentNumberAndIsActiveTrue("99999999");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find all active owners ordered by last name")
    void shouldFindAllActiveOwnersOrderedByLastName() {
        List<Owner> owners = ownerRepository.findAllByIsActiveTrueOrderByLastNameAsc();

        assertThat(owners).isNotEmpty();
        assertThat(owners).hasSizeGreaterThanOrEqualTo(2);
        assertThat(owners.get(0).getLastName()).isEqualTo("García");
        assertThat(owners.get(1).getLastName()).isEqualTo("Pérez");
    }

    @Test
    @DisplayName("Should find active owners with pagination")
    void shouldFindActiveOwnersWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Owner> ownerPage = ownerRepository.findAllByIsActiveTrue(pageable);

        assertThat(ownerPage).isNotEmpty();
        assertThat(ownerPage.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should search owners by first name")
    void shouldSearchOwnersByFirstName() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Owner> results = ownerRepository.searchOwners("Juan", pageable);

        assertThat(results).isNotEmpty();
        assertThat(results.getContent().get(0).getFirstName()).isEqualTo("Juan");
    }

    @Test
    @DisplayName("Should search owners by last name")
    void shouldSearchOwnersByLastName() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Owner> results = ownerRepository.searchOwners("García", pageable);

        assertThat(results).isNotEmpty();
        assertThat(results.getContent().get(0).getLastName()).isEqualTo("García");
    }

    @Test
    @DisplayName("Should search owners by email")
    void shouldSearchOwnersByEmail() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Owner> results = ownerRepository.searchOwners("maria.garcia", pageable);

        assertThat(results).isNotEmpty();
        assertThat(results.getContent().get(0).getEmail()).contains("maria.garcia");
    }

    @Test
    @DisplayName("Should search owners by phone")
    void shouldSearchOwnersByPhone() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Owner> results = ownerRepository.searchOwners("1234567890", pageable);

        assertThat(results).isNotEmpty();
        assertThat(results.getContent().get(0).getPhone()).isEqualTo("1234567890");
    }

    @Test
    @DisplayName("Should return empty when search term not found")
    void shouldReturnEmptyWhenSearchTermNotFound() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Owner> results = ownerRepository.searchOwners("NonexistentName", pageable);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should find owners by city")
    void shouldFindOwnersByCity() {
        List<Owner> owners = ownerRepository.findByCityAndIsActiveTrueOrderByLastNameAsc("Bogotá");

        assertThat(owners).isNotEmpty();
        assertThat(owners).hasSize(1);
        assertThat(owners.get(0).getCity()).isEqualTo("Bogotá");
    }

    @Test
    @DisplayName("Should return empty list for city with no owners")
    void shouldReturnEmptyListForCityWithNoOwners() {
        List<Owner> owners = ownerRepository.findByCityAndIsActiveTrueOrderByLastNameAsc("Cali");

        assertThat(owners).isEmpty();
    }

    @Test
    @DisplayName("Should count active owners")
    void shouldCountActiveOwners() {
        long count = ownerRepository.countByIsActiveTrue();

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should save owner successfully")
    void shouldSaveOwnerSuccessfully() {
        Owner newOwner = new Owner();
        newOwner.setFirstName("Pedro");
        newOwner.setLastName("López");
        newOwner.setEmail("pedro.lopez@example.com");
        newOwner.setPhone("3331234567");
        newOwner.setAddress("Diagonal 999");
        newOwner.setCity("Cali");
        newOwner.setPostalCode("760001");
        newOwner.setDocumentType("DNI");
        newOwner.setDocumentNumber("33333333");
        newOwner.setIsActive(true);

        Owner savedOwner = ownerRepository.save(newOwner);

        assertThat(savedOwner).isNotNull();
        assertThat(savedOwner.getId()).isNotNull();
        assertThat(savedOwner.getFirstName()).isEqualTo("Pedro");
    }

    @Test
    @DisplayName("Should update owner successfully")
    void shouldUpdateOwnerSuccessfully() {
        activeOwner1.setPhone("9999999999");

        Owner updatedOwner = ownerRepository.save(activeOwner1);

        assertThat(updatedOwner.getPhone()).isEqualTo("9999999999");
    }

    @Test
    @DisplayName("Should delete owner successfully")
    void shouldDeleteOwnerSuccessfully() {
        ownerRepository.delete(activeOwner2);
        entityManager.flush();

        Optional<Owner> foundOwner = ownerRepository.findById(activeOwner2.getId());

        assertThat(foundOwner).isEmpty();
    }
}