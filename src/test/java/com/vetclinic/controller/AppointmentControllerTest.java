package com.vetclinic.controller;

import com.vetclinic.dto.appointment.AppointmentDTO;
import com.vetclinic.dto.appointment.CreateAppointmentRequest;
import com.vetclinic.dto.appointment.UpdateAppointmentRequest;
import com.vetclinic.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    private AppointmentDTO appointmentDTO;
    private CreateAppointmentRequest createRequest;
    private UpdateAppointmentRequest updateRequest;

    @BeforeEach
    void setUp() {
        appointmentDTO = new AppointmentDTO();
        appointmentDTO.setId(1L);
        appointmentDTO.setPatientId(1L);
        appointmentDTO.setPatientName("Max");
        appointmentDTO.setAppointmentType("CONSULTATION");
        appointmentDTO.setStatus("SCHEDULED");

        createRequest = new CreateAppointmentRequest();
        createRequest.setPatientId(1L);
        createRequest.setOwnerId(1L);
        createRequest.setVeterinarianId(UUID.randomUUID());
        createRequest.setScheduledDate(LocalDateTime.now().plusDays(1));
        createRequest.setAppointmentType("CONSULTATION");
        createRequest.setReason("Chequeo anual");

        updateRequest = new UpdateAppointmentRequest();
        updateRequest.setScheduledDate(LocalDateTime.now().plusDays(2));
        updateRequest.setNotes("Updated notes");
    }

    @Test
    void testCreateAppointment_Success() {
        when(appointmentService.createAppointment(any(CreateAppointmentRequest.class)))
                .thenReturn(appointmentDTO);

        appointmentController.createAppointment(createRequest);

        verify(appointmentService, times(1)).createAppointment(any(CreateAppointmentRequest.class));
    }

    @Test
    void testGetAppointmentsPage_Success() {
        Page<AppointmentDTO> page = new PageImpl<>(Collections.singletonList(appointmentDTO));
        when(appointmentService.getAppointmentsPage(any())).thenReturn(page);

        appointmentController.getAppointmentsPage(0, 10, "id", "asc");

        verify(appointmentService, times(1)).getAppointmentsPage(any());
    }

    @Test
    void testGetAppointmentById_Success() {
        when(appointmentService.getAppointmentById(1L)).thenReturn(appointmentDTO);

        appointmentController.getAppointmentById(1L);

        verify(appointmentService, times(1)).getAppointmentById(1L);
    }

    @Test
    void testGetAppointmentsByPatient_Success() {
        List<AppointmentDTO> appointments = Collections.singletonList(appointmentDTO);
        when(appointmentService.getAppointmentsByPatient(1L)).thenReturn(appointments);

        appointmentController.getAppointmentsByPatient(1L);

        verify(appointmentService, times(1)).getAppointmentsByPatient(1L);
    }

    @Test
    void testGetAppointmentsByOwner_Success() {
        List<AppointmentDTO> appointments = Collections.singletonList(appointmentDTO);
        when(appointmentService.getAppointmentsByOwner(1L)).thenReturn(appointments);

        appointmentController.getAppointmentsByOwner(1L);

        verify(appointmentService, times(1)).getAppointmentsByOwner(1L);
    }

    @Test
    void testGetAppointmentsByVeterinarian_Success() {
        UUID vetId = UUID.randomUUID();
        List<AppointmentDTO> appointments = Collections.singletonList(appointmentDTO);
        when(appointmentService.getAppointmentsByVeterinarian(vetId)).thenReturn(appointments);

        appointmentController.getAppointmentsByVeterinarian(vetId);

        verify(appointmentService, times(1)).getAppointmentsByVeterinarian(vetId);
    }

    @Test
    void testUpdateAppointment_Success() {
        when(appointmentService.updateAppointment(anyLong(), any())).thenReturn(appointmentDTO);

        appointmentController.updateAppointment(1L, updateRequest);

        verify(appointmentService, times(1)).updateAppointment(eq(1L), any(UpdateAppointmentRequest.class));
    }

    @Test
    void testCancelAppointment_Success() {
        doNothing().when(appointmentService).cancelAppointment(1L);

        appointmentController.cancelAppointment(1L);

        verify(appointmentService, times(1)).cancelAppointment(1L);
    }

    @Test
    void testDeleteAppointment_Success() {
        doNothing().when(appointmentService).deleteAppointment(1L);

        appointmentController.deleteAppointment(1L);

        verify(appointmentService, times(1)).deleteAppointment(1L);
    }

    @Test
    void testCountAppointments_Success() {
        when(appointmentService.countActiveAppointments()).thenReturn(100L);

        appointmentController.countAppointments();

        verify(appointmentService, times(1)).countActiveAppointments();
    }
}