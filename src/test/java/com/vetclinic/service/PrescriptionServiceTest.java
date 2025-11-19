package com.vetclinic.service;

import com.vetclinic.dto.prescription.CreatePrescriptionRequest;
import com.vetclinic.dto.prescription.PrescriptionDTO;
import com.vetclinic.dto.prescription.UpdatePrescriptionRequest;
import com.vetclinic.entity.MedicalRecord;
import com.vetclinic.entity.Patient;
import com.vetclinic.entity.Prescription;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.repository.MedicalRecordRepository;
import com.vetclinic.repository.PatientRepository;
import com.vetclinic.repository.PrescriptionRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private Prescription testPrescription;
    private MedicalRecord testMedicalRecord;
    private Patient testPatient;
    private CreatePrescriptionRequest createRequest;
    private UpdatePrescriptionRequest updateRequest;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setName("Max");
        testPatient.setSpecies("Perro");
        testPatient.setIsActive(true);

        testMedicalRecord = new MedicalRecord();
        testMedicalRecord.setId(1L);
        testMedicalRecord.setPatient(testPatient);
        testMedicalRecord.setRecordDate(LocalDateTime.now());

        testPrescription = new Prescription();
        testPrescription.setId(1L);
        testPrescription.setMedicalRecord(testMedicalRecord);
        testPrescription.setPatient(testPatient);
        testPrescription.setMedicationName("Antibiótico");
        testPrescription.setDosage("10mg");
        testPrescription.setFrequency("2 veces al día");
        testPrescription.setDuration("7 días");
        testPrescription.setInstructions("Con comida");
        testPrescription.setStartDate(LocalDateTime.now());
        testPrescription.setEndDate(LocalDateTime.now().plusDays(7));
        testPrescription.setIsActive(true);

        createRequest = new CreatePrescriptionRequest();
        createRequest.setMedicalRecordId(1L);
        createRequest.setPatientId(1L);
        createRequest.setMedicationName("Antibiótico");
        createRequest.setDosage("10mg");
        createRequest.setFrequency("2 veces al día");
        createRequest.setDuration("7 días");
        createRequest.setInstructions("Con comida");
        createRequest.setStartDate(LocalDateTime.now());
        createRequest.setEndDate(LocalDateTime.now().plusDays(7));

        updateRequest = new UpdatePrescriptionRequest();
        updateRequest.setDosage("15mg");
        updateRequest.setFrequency("3 veces al día");
    }

    @Test
    void createPrescription_Success() {
        // Arrange
        when(medicalRecordRepository.findById(anyLong())).thenReturn(Optional.of(testMedicalRecord));
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(testPrescription);

        // Act
        PrescriptionDTO result = prescriptionService.createPrescription(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testPrescription.getMedicationName(), result.getMedicationName());
        verify(medicalRecordRepository).findById(anyLong());
        verify(patientRepository).findById(anyLong());
        verify(prescriptionRepository).save(any(Prescription.class));
    }

    @Test
    void createPrescription_MedicalRecordNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(medicalRecordRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> prescriptionService.createPrescription(createRequest));

        verify(medicalRecordRepository).findById(anyLong());
        verify(prescriptionRepository, never()).save(any(Prescription.class));
    }

    @Test
    void createPrescription_PatientNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(medicalRecordRepository.findById(anyLong())).thenReturn(Optional.of(testMedicalRecord));
        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> prescriptionService.createPrescription(createRequest));

        verify(medicalRecordRepository).findById(anyLong());
        verify(patientRepository).findById(anyLong());
        verify(prescriptionRepository, never()).save(any(Prescription.class));
    }

    @Test
    void getPrescriptionById_Success() {
        // Arrange
        when(prescriptionRepository.findById(anyLong())).thenReturn(Optional.of(testPrescription));

        // Act
        PrescriptionDTO result = prescriptionService.getPrescriptionById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testPrescription.getMedicationName(), result.getMedicationName());
        verify(prescriptionRepository).findById(anyLong());
    }

    @Test
    void getPrescriptionById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(prescriptionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> prescriptionService.getPrescriptionById(999L));

        verify(prescriptionRepository).findById(anyLong());
    }

    @Test
    void getPrescriptionsPage_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prescription> prescriptionPage = new PageImpl<>(List.of(testPrescription));
        when(prescriptionRepository.findByIsActiveTrueOrderByStartDateDesc(pageable))
                .thenReturn(prescriptionPage);

        // Act
        Page<PrescriptionDTO> result = prescriptionService.getPrescriptionsPage(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(prescriptionRepository).findByIsActiveTrueOrderByStartDateDesc(pageable);
    }

    @Test
    void getPrescriptionsByPatient_Success() {
        // Arrange
        when(prescriptionRepository.findByPatientIdAndIsActiveTrueOrderByStartDateDesc(anyLong()))
                .thenReturn(List.of(testPrescription));

        // Act
        List<PrescriptionDTO> result = prescriptionService.getPrescriptionsByPatient(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(prescriptionRepository).findByPatientIdAndIsActiveTrueOrderByStartDateDesc(anyLong());
    }

    @Test
    void getActivePrescriptionsByPatient_Success() {
        // Arrange
        when(prescriptionRepository.findActivePrescriptionsByPatient(anyLong()))
                .thenReturn(List.of(testPrescription));

        // Act
        List<PrescriptionDTO> result = prescriptionService.getActivePrescriptionsByPatient(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(prescriptionRepository).findActivePrescriptionsByPatient(anyLong());
    }

    @Test
    void getPrescriptionsByMedicalRecord_Success() {
        // Arrange
        when(prescriptionRepository.findByMedicalRecordIdAndIsActiveTrueOrderByStartDateDesc(anyLong()))
                .thenReturn(List.of(testPrescription));

        // Act
        List<PrescriptionDTO> result = prescriptionService.getPrescriptionsByMedicalRecord(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(prescriptionRepository).findByMedicalRecordIdAndIsActiveTrueOrderByStartDateDesc(anyLong());
    }

    @Test
    void searchByMedication_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prescription> prescriptionPage = new PageImpl<>(List.of(testPrescription));
        when(prescriptionRepository.searchByMedication(anyString(), any(Pageable.class)))
                .thenReturn(prescriptionPage);

        // Act
        Page<PrescriptionDTO> result = prescriptionService.searchByMedication("Antibiótico", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(prescriptionRepository).searchByMedication(anyString(), any(Pageable.class));
    }

    @Test
    void getExpiringPrescriptions_Success() {
        // Arrange
        when(prescriptionRepository.findExpiringPrescriptions(any(LocalDateTime.class)))
                .thenReturn(List.of(testPrescription));

        // Act
        List<PrescriptionDTO> result = prescriptionService.getExpiringPrescriptions();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(prescriptionRepository).findExpiringPrescriptions(any(LocalDateTime.class));
    }

    @Test
    void updatePrescription_Success() {
        // Arrange
        when(prescriptionRepository.findById(anyLong())).thenReturn(Optional.of(testPrescription));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(testPrescription);

        // Act
        PrescriptionDTO result = prescriptionService.updatePrescription(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(prescriptionRepository).findById(anyLong());
        verify(prescriptionRepository).save(any(Prescription.class));
    }

    @Test
    void updatePrescription_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(prescriptionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> prescriptionService.updatePrescription(999L, updateRequest));

        verify(prescriptionRepository).findById(anyLong());
        verify(prescriptionRepository, never()).save(any(Prescription.class));
    }

    @Test
    void deletePrescription_Success() {
        // Arrange
        when(prescriptionRepository.findById(anyLong())).thenReturn(Optional.of(testPrescription));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(testPrescription);

        // Act
        prescriptionService.deletePrescription(1L);

        // Assert
        verify(prescriptionRepository).findById(anyLong());
        verify(prescriptionRepository).save(any(Prescription.class));
    }

    @Test
    void deletePrescription_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(prescriptionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> prescriptionService.deletePrescription(999L));

        verify(prescriptionRepository).findById(anyLong());
        verify(prescriptionRepository, never()).save(any(Prescription.class));
    }

    @Test
    void countActivePrescriptions_Success() {
        // Arrange
        when(prescriptionRepository.countByIsActiveTrue()).thenReturn(25L);

        // Act
        long result = prescriptionService.countActivePrescriptions();

        // Assert
        assertEquals(25L, result);
        verify(prescriptionRepository).countByIsActiveTrue();
    }
}