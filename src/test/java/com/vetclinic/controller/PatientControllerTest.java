package com.vetclinic.controller;

import com.vetclinic.dto.patient.CreatePatientRequest;
import com.vetclinic.dto.patient.PatientDTO;
import com.vetclinic.dto.patient.UpdatePatientRequest;
import com.vetclinic.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    private PatientDTO patientDTO;
    private CreatePatientRequest createRequest;
    private UpdatePatientRequest updateRequest;

    @BeforeEach
    void setUp() {
        patientDTO = new PatientDTO();
        patientDTO.setId(1L);
        patientDTO.setName("Max");
        patientDTO.setSpecies("Perro");
        patientDTO.setBreed("Labrador");
        patientDTO.setGender("MALE");
        patientDTO.setIsActive(true);

        createRequest = new CreatePatientRequest();
        createRequest.setName("Max");
        createRequest.setSpecies("Perro");
        createRequest.setBreed("Labrador");
        createRequest.setBirthDate(LocalDate.of(2020, 1, 15));
        createRequest.setGender("MALE");
        createRequest.setOwnerId(1L);

        updateRequest = new UpdatePatientRequest();
        updateRequest.setName("Max Updated");
        updateRequest.setWeight(new BigDecimal("30.00"));
    }

    @Test
    void testCreatePatient_Success() {
        when(patientService.createPatient(any(CreatePatientRequest.class))).thenReturn(patientDTO);

        patientController.createPatient(createRequest);

        verify(patientService, times(1)).createPatient(any(CreatePatientRequest.class));
    }

    @Test
    void testGetAllPatients_Success() {
        Page<PatientDTO> page = new PageImpl<>(Collections.singletonList(patientDTO));
        when(patientService.getPatientsPage(any())).thenReturn(page);

        patientController.getAllPatients(0, 10, "id", "asc");

        verify(patientService, times(1)).getPatientsPage(any());
    }

    @Test
    void testGetPatientById_Success() {
        when(patientService.getPatientById(1L)).thenReturn(patientDTO);

        patientController.getPatientById(1L);

        verify(patientService, times(1)).getPatientById(1L);
    }

    @Test
    void testUpdatePatient_Success() {
        when(patientService.updatePatient(anyLong(), any())).thenReturn(patientDTO);

        patientController.updatePatient(1L, updateRequest);

        verify(patientService, times(1)).updatePatient(eq(1L), any(UpdatePatientRequest.class));
    }

    @Test
    void testDeletePatient_Success() {
        doNothing().when(patientService).deletePatient(1L);

        patientController.deletePatient(1L);

        verify(patientService, times(1)).deletePatient(1L);
    }

    @Test
    void testSearchPatients_Success() {
        List<PatientDTO> patients = Collections.singletonList(patientDTO);
        when(patientService.searchPatientsByName("Max")).thenReturn(patients);

        patientController.searchPatients("Max");

        verify(patientService, times(1)).searchPatientsByName("Max");
    }

    @Test
    void testGetPatientsByOwner_Success() {
        List<PatientDTO> patients = Collections.singletonList(patientDTO);
        when(patientService.getPatientsByOwner(1L)).thenReturn(patients);

        patientController.getPatientsByOwner(1L);

        verify(patientService, times(1)).getPatientsByOwner(1L);
    }

    @Test
    void testGetPatientsBySpecies_Success() {
        List<PatientDTO> patients = Collections.singletonList(patientDTO);
        when(patientService.getPatientsBySpecies("Perro")).thenReturn(patients);

        patientController.getPatientsBySpecies("Perro");

        verify(patientService, times(1)).getPatientsBySpecies("Perro");
    }

    @Test
    void testCountActivePatients_Success() {
        when(patientService.countActivePatients()).thenReturn(50L);

        patientController.countActivePatients();

        verify(patientService, times(1)).countActivePatients();
    }
}