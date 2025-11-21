package com.vetclinic.service;

import com.vetclinic.dto.patient.CreatePatientRequest;
import com.vetclinic.dto.patient.PatientDTO;
import com.vetclinic.dto.patient.UpdatePatientRequest;
import com.vetclinic.entity.Owner;
import com.vetclinic.entity.Patient;
import com.vetclinic.exception.DuplicateResourceException;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.repository.OwnerRepository;
import com.vetclinic.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient;
    private Owner testOwner;
    private CreatePatientRequest createRequest;
    private UpdatePatientRequest updateRequest;

    @BeforeEach
    void setUp() {
        testOwner = new Owner();
        testOwner.setId(1L);
        testOwner.setFirstName("John");
        testOwner.setLastName("Doe");
        testOwner.setEmail("john@example.com");
        testOwner.setPhone("1234567890");
        testOwner.setIsActive(true);

        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setName("Max");
        testPatient.setSpecies("Perro");
        testPatient.setBreed("Labrador");
        testPatient.setBirthDate(LocalDate.of(2020, 1, 1));
        testPatient.setGender("Macho");
        testPatient.setColor("Dorado");
        testPatient.setWeight(new BigDecimal("25.5"));
        testPatient.setMicrochipNumber("123456789");
        testPatient.setOwner(testOwner);
        testPatient.setIsActive(true);

        createRequest = new CreatePatientRequest();
        createRequest.setName("Max");
        createRequest.setSpecies("Perro");
        createRequest.setBreed("Labrador");
        createRequest.setBirthDate(LocalDate.of(2020, 1, 1));
        createRequest.setGender("Macho");
        createRequest.setColor("Dorado");
        createRequest.setWeight(new BigDecimal("25.5"));
        createRequest.setMicrochipNumber("123456789");
        createRequest.setOwnerId(1L);

        updateRequest = new UpdatePatientRequest();
        updateRequest.setName("Max Updated");
        updateRequest.setWeight(new BigDecimal("26.0"));
    }

    @Test
    void createPatient_Success() {
        // Arrange
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));
        when(patientRepository.existsByMicrochipNumber(anyString())).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act
        PatientDTO result = patientService.createPatient(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testPatient.getName(), result.getName());
        assertEquals(testPatient.getSpecies(), result.getSpecies());

        verify(ownerRepository).findById(anyLong());
        verify(patientRepository).existsByMicrochipNumber(anyString());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void createPatient_OwnerNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> patientService.createPatient(createRequest));

        verify(ownerRepository).findById(anyLong());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void createPatient_OwnerNotActive_ThrowsResourceNotFoundException() {
        // Arrange
        testOwner.setIsActive(false);
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> patientService.createPatient(createRequest));

        verify(ownerRepository).findById(anyLong());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void createPatient_DuplicateMicrochip_ThrowsDuplicateResourceException() {
        // Arrange
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));
        when(patientRepository.existsByMicrochipNumber(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> patientService.createPatient(createRequest));

        verify(ownerRepository).findById(anyLong());
        verify(patientRepository).existsByMicrochipNumber(anyString());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void createPatient_WithoutMicrochip_Success() {
        // Arrange
        createRequest.setMicrochipNumber(null);
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act
        PatientDTO result = patientService.createPatient(createRequest);

        // Assert
        assertNotNull(result);
        verify(ownerRepository).findById(anyLong());
        verify(patientRepository, never()).existsByMicrochipNumber(anyString());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void getPatientById_Success() {
        // Arrange
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));

        // Act
        PatientDTO result = patientService.getPatientById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testPatient.getName(), result.getName());

        verify(patientRepository).findById(anyLong());
    }

    @Test
    void getPatientById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> patientService.getPatientById(999L));

        verify(patientRepository).findById(anyLong());
    }

    @Test
    void getAllPatients_Success() {
        // Arrange
        List<Patient> patients = List.of(testPatient);
        when(patientRepository.findByIsActiveTrue()).thenReturn(patients);

        // Act
        List<PatientDTO> result = patientService.getAllPatients();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPatient.getName(), result.get(0).getName());

        verify(patientRepository).findByIsActiveTrue();
    }

    @Test
    void getAllPatients_Empty_ReturnsEmptyList() {
        // Arrange
        when(patientRepository.findByIsActiveTrue()).thenReturn(List.of());

        // Act
        List<PatientDTO> result = patientService.getAllPatients();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(patientRepository).findByIsActiveTrue();
    }

    @Test
    void getPatientsPage_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> patientPage = new PageImpl<>(List.of(testPatient));
        when(patientRepository.findByIsActiveTrue(pageable)).thenReturn(patientPage);

        // Act
        Page<PatientDTO> result = patientService.getPatientsPage(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(patientRepository).findByIsActiveTrue(pageable);
    }

    @Test
    void updatePatient_Success() {
        // Arrange
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act
        PatientDTO result = patientService.updatePatient(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(patientRepository).findById(anyLong());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void updatePatient_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> patientService.updatePatient(999L, updateRequest));

        verify(patientRepository).findById(anyLong());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void updatePatient_ChangeOwner_Success() {
        // Arrange
        Owner newOwner = new Owner();
        newOwner.setId(2L);
        newOwner.setIsActive(true);

        updateRequest.setOwnerId(2L);

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(ownerRepository.findById(2L)).thenReturn(Optional.of(newOwner));
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act
        PatientDTO result = patientService.updatePatient(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(ownerRepository).findById(2L);
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void updatePatient_NewOwnerNotActive_ThrowsResourceNotFoundException() {
        // Arrange
        Owner inactiveOwner = new Owner();
        inactiveOwner.setId(2L);
        inactiveOwner.setIsActive(false);

        updateRequest.setOwnerId(2L);

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(ownerRepository.findById(2L)).thenReturn(Optional.of(inactiveOwner));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> patientService.updatePatient(1L, updateRequest));

        verify(patientRepository).findById(anyLong());
        verify(ownerRepository).findById(2L);
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void deletePatient_Success() {
        // Arrange
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act
        patientService.deletePatient(1L);

        // Assert
        verify(patientRepository).findById(anyLong());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void deletePatient_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> patientService.deletePatient(999L));

        verify(patientRepository).findById(anyLong());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void searchPatientsByName_Success() {
        // Arrange
        when(patientRepository.searchByName(anyString())).thenReturn(List.of(testPatient));

        // Act
        List<PatientDTO> result = patientService.searchPatientsByName("Max");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(patientRepository).searchByName(anyString());
    }

    @Test
    void getPatientsByOwner_Success() {
        // Arrange
        when(patientRepository.findByOwnerId(anyLong())).thenReturn(List.of(testPatient));

        // Act
        List<PatientDTO> result = patientService.getPatientsByOwner(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(patientRepository).findByOwnerId(anyLong());
    }

    @Test
    void getPatientsBySpecies_Success() {
        // Arrange
        when(patientRepository.findBySpecies(anyString())).thenReturn(List.of(testPatient));

        // Act
        List<PatientDTO> result = patientService.getPatientsBySpecies("Perro");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(patientRepository).findBySpecies(anyString());
    }

    @Test
    void countActivePatients_Success() {
        // Arrange
        when(patientRepository.countByIsActiveTrue()).thenReturn(10L);

        // Act
        long result = patientService.countActivePatients();

        // Assert
        assertEquals(10L, result);

        verify(patientRepository).countByIsActiveTrue();
    }
}