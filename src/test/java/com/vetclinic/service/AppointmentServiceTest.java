package com.vetclinic.service;

import com.vetclinic.dto.appointment.AppointmentDTO;
import com.vetclinic.dto.appointment.CreateAppointmentRequest;
import com.vetclinic.dto.appointment.UpdateAppointmentRequest;
import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.Appointment.AppointmentStatus;
import com.vetclinic.entity.Owner;
import com.vetclinic.entity.Patient;
import com.vetclinic.entity.User;
import com.vetclinic.exception.BusinessException;
import com.vetclinic.exception.ResourceNotFoundException;
import com.vetclinic.repository.AppointmentRepository;
import com.vetclinic.repository.OwnerRepository;
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

import java.time.LocalDate;
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
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Appointment testAppointment;
    private Patient testPatient;
    private Owner testOwner;
    private User testVeterinarian;
    private CreateAppointmentRequest createRequest;
    private UpdateAppointmentRequest updateRequest;

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
        testPatient.setOwner(testOwner);
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
        testAppointment.setPatient(testPatient);
        testAppointment.setOwner(testOwner);
        testAppointment.setVeterinarian(testVeterinarian);
        testAppointment.setScheduledDate(LocalDateTime.now().plusDays(1));
        testAppointment.setAppointmentType("CONSULTATION");
        testAppointment.setStatus(AppointmentStatus.SCHEDULED);
        testAppointment.setReason("Checkup");
        testAppointment.setDurationMinutes(30);
        testAppointment.setIsActive(true);

        createRequest = new CreateAppointmentRequest();
        createRequest.setPatientId(1L);
        createRequest.setOwnerId(1L);
        createRequest.setVeterinarianId(testVeterinarian.getId());
        createRequest.setScheduledDate(LocalDateTime.now().plusDays(1));
        createRequest.setAppointmentType("CONSULTATION");
        createRequest.setReason("Checkup");
        createRequest.setDurationMinutes(30);

        updateRequest = new UpdateAppointmentRequest();
        updateRequest.setReason("Follow-up");
        updateRequest.setDurationMinutes(45);
    }

    @Test
    void createAppointment_Success() {
        // Arrange
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testVeterinarian));
        when(appointmentRepository.existsConflict(any(UUID.class), any(LocalDateTime.class))).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // Act
        AppointmentDTO result = appointmentService.createAppointment(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testAppointment.getReason(), result.getReason());
        verify(patientRepository).findById(anyLong());
        verify(ownerRepository).findById(anyLong());
        verify(userRepository).findById(any(UUID.class));
        verify(appointmentRepository).existsConflict(any(UUID.class), any(LocalDateTime.class));
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void createAppointment_PatientNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.createAppointment(createRequest));

        verify(patientRepository).findById(anyLong());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void createAppointment_PatientNotActive_ThrowsBusinessException() {
        // Arrange
        testPatient.setIsActive(false);
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> appointmentService.createAppointment(createRequest));

        verify(patientRepository).findById(anyLong());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void createAppointment_OwnerNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.createAppointment(createRequest));

        verify(patientRepository).findById(anyLong());
        verify(ownerRepository).findById(anyLong());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void createAppointment_OwnerNotActive_ThrowsBusinessException() {
        // Arrange
        testOwner.setIsActive(false);
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> appointmentService.createAppointment(createRequest));

        verify(ownerRepository).findById(anyLong());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void createAppointment_VeterinarianNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.createAppointment(createRequest));

        verify(userRepository).findById(any(UUID.class));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void createAppointment_ScheduleConflict_ThrowsBusinessException() {
        // Arrange
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(ownerRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testVeterinarian));
        when(appointmentRepository.existsConflict(any(UUID.class), any(LocalDateTime.class))).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> appointmentService.createAppointment(createRequest));

        verify(appointmentRepository).existsConflict(any(UUID.class), any(LocalDateTime.class));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void getAppointmentById_Success() {
        // Arrange
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));

        // Act
        AppointmentDTO result = appointmentService.getAppointmentById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testAppointment.getReason(), result.getReason());
        verify(appointmentRepository).findById(anyLong());
    }

    @Test
    void getAppointmentById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.getAppointmentById(999L));

        verify(appointmentRepository).findById(anyLong());
    }

    @Test
    void getAppointmentsPage_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Appointment> appointmentPage = new PageImpl<>(List.of(testAppointment));
        when(appointmentRepository.findByIsActiveTrueOrderByScheduledDateDesc(pageable))
                .thenReturn(appointmentPage);

        // Act
        Page<AppointmentDTO> result = appointmentService.getAppointmentsPage(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(appointmentRepository).findByIsActiveTrueOrderByScheduledDateDesc(pageable);
    }

    @Test
    void getAppointmentsByPatient_Success() {
        // Arrange
        when(appointmentRepository.findByPatientIdAndIsActiveTrueOrderByScheduledDateDesc(anyLong()))
                .thenReturn(List.of(testAppointment));

        // Act
        List<AppointmentDTO> result = appointmentService.getAppointmentsByPatient(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentRepository).findByPatientIdAndIsActiveTrueOrderByScheduledDateDesc(anyLong());
    }

    @Test
    void getAppointmentsByOwner_Success() {
        // Arrange
        when(appointmentRepository.findByOwnerIdAndIsActiveTrueOrderByScheduledDateDesc(anyLong()))
                .thenReturn(List.of(testAppointment));

        // Act
        List<AppointmentDTO> result = appointmentService.getAppointmentsByOwner(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentRepository).findByOwnerIdAndIsActiveTrueOrderByScheduledDateDesc(anyLong());
    }

    @Test
    void getAppointmentsByVeterinarian_Success() {
        // Arrange
        when(appointmentRepository.findByVeterinarianIdAndIsActiveTrueOrderByScheduledDateDesc(any(UUID.class)))
                .thenReturn(List.of(testAppointment));

        // Act
        List<AppointmentDTO> result = appointmentService.getAppointmentsByVeterinarian(testVeterinarian.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentRepository).findByVeterinarianIdAndIsActiveTrueOrderByScheduledDateDesc(any(UUID.class));
    }

    @Test
    void getAppointmentsByDateRange_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);
        when(appointmentRepository.findByDateRange(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(testAppointment));

        // Act
        List<AppointmentDTO> result = appointmentService.getAppointmentsByDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentRepository).findByDateRange(
                any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getUpcomingAppointments_Success() {
        // Arrange
        when(appointmentRepository.findUpcomingAppointments(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(testAppointment));

        // Act
        List<AppointmentDTO> result = appointmentService.getUpcomingAppointments();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentRepository).findUpcomingAppointments(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void updateAppointment_Success() {
        // Arrange
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // Act
        AppointmentDTO result = appointmentService.updateAppointment(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(appointmentRepository).findById(anyLong());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void updateAppointment_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.updateAppointment(999L, updateRequest));

        verify(appointmentRepository).findById(anyLong());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void updateAppointment_ChangeVeterinarianWithConflict_ThrowsBusinessException() {
        // Arrange
        UUID newVetId = UUID.randomUUID();
        updateRequest.setVeterinarianId(newVetId);
        updateRequest.setScheduledDate(LocalDateTime.now().plusDays(2));

        User newVet = User.builder()
                .id(newVetId)
                .username("drnew")
                .build();

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));
        when(userRepository.findById(newVetId)).thenReturn(Optional.of(newVet));
        when(appointmentRepository.existsConflictExcluding(any(UUID.class), any(LocalDateTime.class), anyLong()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> appointmentService.updateAppointment(1L, updateRequest));

        verify(appointmentRepository).findById(anyLong());
        verify(userRepository).findById(newVetId);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_Success() {
        // Arrange
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // Act
        appointmentService.cancelAppointment(1L);

        // Assert
        verify(appointmentRepository).findById(anyLong());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.cancelAppointment(999L));

        verify(appointmentRepository).findById(anyLong());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void cancelAppointment_AlreadyCompleted_ThrowsBusinessException() {
        // Arrange
        testAppointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> appointmentService.cancelAppointment(1L));

        verify(appointmentRepository).findById(anyLong());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void deleteAppointment_Success() {
        // Arrange
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // Act
        appointmentService.deleteAppointment(1L);

        // Assert
        verify(appointmentRepository).findById(anyLong());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void deleteAppointment_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.deleteAppointment(999L));

        verify(appointmentRepository).findById(anyLong());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void countActiveAppointments_Success() {
        // Arrange
        when(appointmentRepository.countByIsActiveTrue()).thenReturn(15L);

        // Act
        long result = appointmentService.countActiveAppointments();

        // Assert
        assertEquals(15L, result);
        verify(appointmentRepository).countByIsActiveTrue();
    }
}