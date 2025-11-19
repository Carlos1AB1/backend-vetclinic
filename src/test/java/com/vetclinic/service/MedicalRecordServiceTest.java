package com.vetclinic.service;

import com.vetclinic.dto.medicalrecord.CreateMedicalRecordRequest;
import com.vetclinic.dto.medicalrecord.MedicalRecordDTO;
import com.vetclinic.dto.medicalrecord.UpdateMedicalRecordRequest;
import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.MedicalRecord;
import com.vetclinic.entity.Patient;
import com.vetclinic.entity.User;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.repository.AppointmentRepository;
import com.vetclinic.repository.MedicalRecordRepository;
import com.vetclinic.repository.PatientRepository;
import com.vetclinic.repository.UserRepository;
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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private MedicalRecord testMedicalRecord;
    private Patient testPatient;
    private User testVeterinarian;
    private Appointment testAppointment;
    private CreateMedicalRecordRequest createRequest;
    private UpdateMedicalRecordRequest updateRequest;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setName("Max");
        testPatient.setSpecies("Perro");
        testPatient.setIsActive(true);

        testVeterinarian = User.builder()
                .id(UUID.randomUUID())
                .username("drsmith")
                .email("drsmith@example.com")
                .firstName("John")
                .lastName("Smith")
                .roles(new HashSet<>())
                .isActive(true)
                .build();

        testAppointment = new Appointment();
        testAppointment.setId(1L);

        testMedicalRecord = new MedicalRecord();
        testMedicalRecord.setId(1L);
        testMedicalRecord.setPatient(testPatient);
        testMedicalRecord.setVeterinarian(testVeterinarian);
        testMedicalRecord.setAppointment(testAppointment);
        testMedicalRecord.setRecordDate(LocalDateTime.now());
        testMedicalRecord.setDiagnosis("Healthy");
        testMedicalRecord.setTreatment("Vaccination");
        testMedicalRecord.setSymptoms("None");
        testMedicalRecord.setWeight(new BigDecimal("25.5"));
        testMedicalRecord.setTemperature(new BigDecimal("38.5"));
        testMedicalRecord.setFollowUpRequired(false);
        testMedicalRecord.setIsActive(true);

        createRequest = new CreateMedicalRecordRequest();
        createRequest.setPatientId(1L);
        createRequest.setVeterinarianId(testVeterinarian.getId());
        createRequest.setAppointmentId(1L);
        createRequest.setRecordDate(LocalDateTime.now());
        createRequest.setDiagnosis("Healthy");
        createRequest.setTreatment("Vaccination");
        createRequest.setSymptoms("None");
        createRequest.setWeight(new BigDecimal("25.5"));
        createRequest.setTemperature(new BigDecimal("38.5"));
        createRequest.setFollowUpRequired(false);

        updateRequest = new UpdateMedicalRecordRequest();
        updateRequest.setDiagnosis("Updated diagnosis");
        updateRequest.setTreatment("Updated treatment");
    }

    @Test
    void createMedicalRecord_Success() {
        // Arrange
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testVeterinarian));
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(testMedicalRecord);

        // Act
        MedicalRecordDTO result = medicalRecordService.createMedicalRecord(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testMedicalRecord.getDiagnosis(), result.getDiagnosis());
        verify(patientRepository).findById(anyLong());
        verify(userRepository).findById(any(UUID.class));
        verify(appointmentRepository).findById(anyLong());
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }

    @Test
    void createMedicalRecord_PatientNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> medicalRecordService.createMedicalRecord(createRequest));

        verify(patientRepository).findById(anyLong());
        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    void createMedicalRecord_VeterinarianNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> medicalRecordService.createMedicalRecord(createRequest));

        verify(patientRepository).findById(anyLong());
        verify(userRepository).findById(any(UUID.class));
        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    void createMedicalRecord_WithoutAppointment_Success() {
        // Arrange
        createRequest.setAppointmentId(null);
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testVeterinarian));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(testMedicalRecord);

        // Act
        MedicalRecordDTO result = medicalRecordService.createMedicalRecord(createRequest);

        // Assert
        assertNotNull(result);
        verify(appointmentRepository, never()).findById(anyLong());
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }

    @Test
    void createMedicalRecord_AppointmentNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testVeterinarian));
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> medicalRecordService.createMedicalRecord(createRequest));

        verify(appointmentRepository).findById(anyLong());
        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    void getMedicalRecordById_Success() {
        // Arrange
        when(medicalRecordRepository.findById(anyLong())).thenReturn(Optional.of(testMedicalRecord));

        // Act
        MedicalRecordDTO result = medicalRecordService.getMedicalRecordById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testMedicalRecord.getDiagnosis(), result.getDiagnosis());
        verify(medicalRecordRepository).findById(anyLong());
    }

    @Test
    void getMedicalRecordById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(medicalRecordRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> medicalRecordService.getMedicalRecordById(999L));

        verify(medicalRecordRepository).findById(anyLong());
    }

    @Test
    void getMedicalRecordsPage_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<MedicalRecord> medicalRecordPage = new PageImpl<>(List.of(testMedicalRecord));
        when(medicalRecordRepository.findByIsActiveTrueOrderByRecordDateDesc(pageable))
                .thenReturn(medicalRecordPage);

        // Act
        Page<MedicalRecordDTO> result = medicalRecordService.getMedicalRecordsPage(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(medicalRecordRepository).findByIsActiveTrueOrderByRecordDateDesc(pageable);
    }

    @Test
    void getMedicalRecordsByPatient_Success() {
        // Arrange
        when(medicalRecordRepository.findByPatientIdAndIsActiveTrueOrderByRecordDateDesc(anyLong()))
                .thenReturn(List.of(testMedicalRecord));

        // Act
        List<MedicalRecordDTO> result = medicalRecordService.getMedicalRecordsByPatient(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(medicalRecordRepository).findByPatientIdAndIsActiveTrueOrderByRecordDateDesc(anyLong());
    }

    @Test
    void getMedicalRecordsByVeterinarian_Success() {
        // Arrange
        when(medicalRecordRepository.findByVeterinarianIdAndIsActiveTrueOrderByRecordDateDesc(any(UUID.class)))
                .thenReturn(List.of(testMedicalRecord));

        // Act
        List<MedicalRecordDTO> result = medicalRecordService.getMedicalRecordsByVeterinarian(testVeterinarian.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(medicalRecordRepository).findByVeterinarianIdAndIsActiveTrueOrderByRecordDateDesc(any(UUID.class));
    }

    @Test
    void getRecordsRequiringFollowUp_Success() {
        // Arrange
        testMedicalRecord.setFollowUpRequired(true);
        when(medicalRecordRepository.findRecordsRequiringFollowUp())
                .thenReturn(List.of(testMedicalRecord));

        // Act
        List<MedicalRecordDTO> result = medicalRecordService.getRecordsRequiringFollowUp();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(medicalRecordRepository).findRecordsRequiringFollowUp();
    }

    @Test
    void updateMedicalRecord_Success() {
        // Arrange
        when(medicalRecordRepository.findById(anyLong())).thenReturn(Optional.of(testMedicalRecord));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(testMedicalRecord);

        // Act
        MedicalRecordDTO result = medicalRecordService.updateMedicalRecord(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(medicalRecordRepository).findById(anyLong());
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }

    @Test
    void updateMedicalRecord_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(medicalRecordRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> medicalRecordService.updateMedicalRecord(999L, updateRequest));

        verify(medicalRecordRepository).findById(anyLong());
        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    void updateMedicalRecord_ChangeVeterinarian_Success() {
        // Arrange
        UUID newVetId = UUID.randomUUID();
        updateRequest.setVeterinarianId(newVetId);

        User newVet = User.builder()
                .id(newVetId)
                .username("drnew")
                .build();

        when(medicalRecordRepository.findById(anyLong())).thenReturn(Optional.of(testMedicalRecord));
        when(userRepository.findById(newVetId)).thenReturn(Optional.of(newVet));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(testMedicalRecord);

        // Act
        MedicalRecordDTO result = medicalRecordService.updateMedicalRecord(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(newVetId);
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }

    @Test
    void deleteMedicalRecord_Success() {
        // Arrange
        when(medicalRecordRepository.findById(anyLong())).thenReturn(Optional.of(testMedicalRecord));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(testMedicalRecord);

        // Act
        medicalRecordService.deleteMedicalRecord(1L);

        // Assert
        verify(medicalRecordRepository).findById(anyLong());
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }

    @Test
    void deleteMedicalRecord_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(medicalRecordRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> medicalRecordService.deleteMedicalRecord(999L));

        verify(medicalRecordRepository).findById(anyLong());
        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    void countActiveMedicalRecords_Success() {
        // Arrange
        when(medicalRecordRepository.countByIsActiveTrue()).thenReturn(20L);

        // Act
        long result = medicalRecordService.countActiveMedicalRecords();

        // Assert
        assertEquals(20L, result);
        verify(medicalRecordRepository).countByIsActiveTrue();
    }
}