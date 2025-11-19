package com.vetclinic.controller;

import com.vetclinic.dto.medicalrecord.CreateMedicalRecordRequest;
import com.vetclinic.dto.medicalrecord.MedicalRecordDTO;
import com.vetclinic.dto.medicalrecord.UpdateMedicalRecordRequest;
import com.vetclinic.service.MedicalRecordService;
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
class MedicalRecordControllerTest {

    @Mock
    private MedicalRecordService medicalRecordService;

    @InjectMocks
    private MedicalRecordController medicalRecordController;

    private MedicalRecordDTO medicalRecordDTO;
    private CreateMedicalRecordRequest createRequest;
    private UpdateMedicalRecordRequest updateRequest;

    @BeforeEach
    void setUp() {
        medicalRecordDTO = new MedicalRecordDTO();
        medicalRecordDTO.setId(1L);
        medicalRecordDTO.setPatientId(1L);
        medicalRecordDTO.setPatientName("Max");
        medicalRecordDTO.setDiagnosis("Infección respiratoria");
        medicalRecordDTO.setTreatment("Antibióticos");

        createRequest = new CreateMedicalRecordRequest();
        createRequest.setPatientId(1L);
        createRequest.setVeterinarianId(UUID.randomUUID());
        createRequest.setRecordDate(LocalDateTime.now());
        createRequest.setDiagnosis("Infección respiratoria");
        createRequest.setTreatment("Antibióticos");
        createRequest.setSymptoms("Tos y estornudos");

        updateRequest = new UpdateMedicalRecordRequest();
        updateRequest.setDiagnosis("Updated diagnosis");
        updateRequest.setTreatment("Updated treatment");
    }

    @Test
    void testCreateMedicalRecord_Success() {
        when(medicalRecordService.createMedicalRecord(any(CreateMedicalRecordRequest.class)))
                .thenReturn(medicalRecordDTO);

        medicalRecordController.createMedicalRecord(createRequest);

        verify(medicalRecordService, times(1)).createMedicalRecord(any(CreateMedicalRecordRequest.class));
    }

    @Test
    void testGetMedicalRecordsPage_Success() {
        Page<MedicalRecordDTO> page = new PageImpl<>(Collections.singletonList(medicalRecordDTO));
        when(medicalRecordService.getMedicalRecordsPage(any())).thenReturn(page);

        medicalRecordController.getMedicalRecordsPage(0, 10, "id", "asc");

        verify(medicalRecordService, times(1)).getMedicalRecordsPage(any());
    }

    @Test
    void testGetMedicalRecordById_Success() {
        when(medicalRecordService.getMedicalRecordById(1L)).thenReturn(medicalRecordDTO);

        medicalRecordController.getMedicalRecordById(1L);

        verify(medicalRecordService, times(1)).getMedicalRecordById(1L);
    }

    @Test
    void testGetMedicalRecordsByPatient_Success() {
        List<MedicalRecordDTO> records = Collections.singletonList(medicalRecordDTO);
        when(medicalRecordService.getMedicalRecordsByPatient(1L)).thenReturn(records);

        medicalRecordController.getMedicalRecordsByPatient(1L);

        verify(medicalRecordService, times(1)).getMedicalRecordsByPatient(1L);
    }

    @Test
    void testGetMedicalRecordsByVeterinarian_Success() {
        UUID vetId = UUID.randomUUID();
        List<MedicalRecordDTO> records = Collections.singletonList(medicalRecordDTO);
        when(medicalRecordService.getMedicalRecordsByVeterinarian(vetId)).thenReturn(records);

        medicalRecordController.getMedicalRecordsByVeterinarian(vetId);

        verify(medicalRecordService, times(1)).getMedicalRecordsByVeterinarian(vetId);
    }

    @Test
    void testGetMedicalRecordsByDateRange_Success() {
        List<MedicalRecordDTO> records = Collections.singletonList(medicalRecordDTO);
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);
        when(medicalRecordService.getMedicalRecordsByDateRange(any(), any())).thenReturn(records);

        medicalRecordController.getMedicalRecordsByDateRange(start, end);

        verify(medicalRecordService, times(1)).getMedicalRecordsByDateRange(any(), any());
    }

    @Test
    void testSearchByDiagnosis_Success() {
        Page<MedicalRecordDTO> page = new PageImpl<>(Collections.singletonList(medicalRecordDTO));
        when(medicalRecordService.searchByDiagnosis(any(), any())).thenReturn(page);

        medicalRecordController.searchByDiagnosis("Diabetes", 0, 10);

        verify(medicalRecordService, times(1)).searchByDiagnosis(any(), any());
    }

    @Test
    void testGetRecordsRequiringFollowUp_Success() {
        List<MedicalRecordDTO> records = Collections.singletonList(medicalRecordDTO);
        when(medicalRecordService.getRecordsRequiringFollowUp()).thenReturn(records);

        medicalRecordController.getRecordsRequiringFollowUp();

        verify(medicalRecordService, times(1)).getRecordsRequiringFollowUp();
    }

    @Test
    void testUpdateMedicalRecord_Success() {
        when(medicalRecordService.updateMedicalRecord(anyLong(), any())).thenReturn(medicalRecordDTO);

        medicalRecordController.updateMedicalRecord(1L, updateRequest);

        verify(medicalRecordService, times(1)).updateMedicalRecord(eq(1L), any(UpdateMedicalRecordRequest.class));
    }

    @Test
    void testDeleteMedicalRecord_Success() {
        doNothing().when(medicalRecordService).deleteMedicalRecord(1L);

        medicalRecordController.deleteMedicalRecord(1L);

        verify(medicalRecordService, times(1)).deleteMedicalRecord(1L);
    }

    @Test
    void testCountMedicalRecords_Success() {
        when(medicalRecordService.countActiveMedicalRecords()).thenReturn(200L);

        medicalRecordController.countMedicalRecords();

        verify(medicalRecordService, times(1)).countActiveMedicalRecords();
    }
}