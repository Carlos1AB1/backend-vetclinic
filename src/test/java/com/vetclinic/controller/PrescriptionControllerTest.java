package com.vetclinic.controller;

import com.vetclinic.dto.prescription.CreatePrescriptionRequest;
import com.vetclinic.dto.prescription.PrescriptionDTO;
import com.vetclinic.dto.prescription.UpdatePrescriptionRequest;
import com.vetclinic.service.PrescriptionService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionControllerTest {

    @Mock
    private PrescriptionService prescriptionService;

    @InjectMocks
    private PrescriptionController prescriptionController;

    private PrescriptionDTO prescriptionDTO;
    private CreatePrescriptionRequest createRequest;
    private UpdatePrescriptionRequest updateRequest;

    @BeforeEach
    void setUp() {
        prescriptionDTO = new PrescriptionDTO();
        prescriptionDTO.setId(1L);
        prescriptionDTO.setPatientId(1L);
        prescriptionDTO.setPatientName("Max");
        prescriptionDTO.setMedicationName("Amoxicilina");
        prescriptionDTO.setDosage("500mg");
        prescriptionDTO.setFrequency("Cada 8 horas");

        createRequest = new CreatePrescriptionRequest();
        createRequest.setMedicalRecordId(1L);
        createRequest.setPatientId(1L);
        createRequest.setMedicationName("Amoxicilina");
        createRequest.setDosage("500mg");
        createRequest.setFrequency("Cada 8 horas");
        createRequest.setDuration("7 días");
        createRequest.setStartDate(LocalDateTime.now());

        updateRequest = new UpdatePrescriptionRequest();
        updateRequest.setDosage("750mg");
        updateRequest.setFrequency("Cada 12 horas");
    }

    @Test
    void testCreatePrescription_Success() {
        when(prescriptionService.createPrescription(any(CreatePrescriptionRequest.class)))
                .thenReturn(prescriptionDTO);

        prescriptionController.createPrescription(createRequest);

        verify(prescriptionService, times(1)).createPrescription(any(CreatePrescriptionRequest.class));
    }

    @Test
    void testGetPrescriptionsPage_Success() {
        Page<PrescriptionDTO> page = new PageImpl<>(Collections.singletonList(prescriptionDTO));
        when(prescriptionService.getPrescriptionsPage(any())).thenReturn(page);

        prescriptionController.getPrescriptionsPage(0, 10, "id", "asc");

        verify(prescriptionService, times(1)).getPrescriptionsPage(any());
    }

    @Test
    void testGetPrescriptionById_Success() {
        when(prescriptionService.getPrescriptionById(1L)).thenReturn(prescriptionDTO);

        prescriptionController.getPrescriptionById(1L);

        verify(prescriptionService, times(1)).getPrescriptionById(1L);
    }

    @Test
    void testGetPrescriptionsByPatient_Success() {
        List<PrescriptionDTO> prescriptions = Collections.singletonList(prescriptionDTO);
        when(prescriptionService.getPrescriptionsByPatient(1L)).thenReturn(prescriptions);

        prescriptionController.getPrescriptionsByPatient(1L);

        verify(prescriptionService, times(1)).getPrescriptionsByPatient(1L);
    }

    @Test
    void testGetActivePrescriptionsByPatient_Success() {
        List<PrescriptionDTO> prescriptions = Collections.singletonList(prescriptionDTO);
        when(prescriptionService.getActivePrescriptionsByPatient(1L)).thenReturn(prescriptions);

        prescriptionController.getActivePrescriptionsByPatient(1L);

        verify(prescriptionService, times(1)).getActivePrescriptionsByPatient(1L);
    }

    @Test
    void testGetPrescriptionsByMedicalRecord_Success() {
        List<PrescriptionDTO> prescriptions = Collections.singletonList(prescriptionDTO);
        when(prescriptionService.getPrescriptionsByMedicalRecord(1L)).thenReturn(prescriptions);

        prescriptionController.getPrescriptionsByMedicalRecord(1L);

        verify(prescriptionService, times(1)).getPrescriptionsByMedicalRecord(1L);
    }

    @Test
    void testSearchByMedication_Success() {
        Page<PrescriptionDTO> page = new PageImpl<>(Collections.singletonList(prescriptionDTO));
        when(prescriptionService.searchByMedication(any(), any())).thenReturn(page);

        prescriptionController.searchByMedication("Aspirina", 0, 10);

        verify(prescriptionService, times(1)).searchByMedication(any(), any());
    }

    @Test
    void testGetExpiringPrescriptions_Success() {
        List<PrescriptionDTO> prescriptions = Collections.singletonList(prescriptionDTO);
        when(prescriptionService.getExpiringPrescriptions()).thenReturn(prescriptions);

        prescriptionController.getExpiringPrescriptions();

        verify(prescriptionService, times(1)).getExpiringPrescriptions();
    }

    @Test
    void testUpdatePrescription_Success() {
        when(prescriptionService.updatePrescription(anyLong(), any())).thenReturn(prescriptionDTO);

        prescriptionController.updatePrescription(1L, updateRequest);

        verify(prescriptionService, times(1)).updatePrescription(eq(1L), any(UpdatePrescriptionRequest.class));
    }

    @Test
    void testDeletePrescription_Success() {
        doNothing().when(prescriptionService).deletePrescription(1L);

        prescriptionController.deletePrescription(1L);

        verify(prescriptionService, times(1)).deletePrescription(1L);
    }

    @Test
    void testCountPrescriptions_Success() {
        when(prescriptionService.countActivePrescriptions()).thenReturn(75L);

        prescriptionController.countPrescriptions();

        verify(prescriptionService, times(1)).countActivePrescriptions();
    }
}