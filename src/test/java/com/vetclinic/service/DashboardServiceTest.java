package com.vetclinic.service;

import com.vetclinic.dto.DashboardStatsDTO;
import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.Appointment.AppointmentStatus;
import com.vetclinic.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getDashboardStats_MultipleUsers_Success() {
        // Arrange
        long largeUserCount = 1000L;
        when(userRepository.count()).thenReturn(largeUserCount);
        when(patientRepository.countByIsActiveTrue()).thenReturn(500L);
        when(ownerRepository.countByIsActiveTrue()).thenReturn(300L);
        when(appointmentRepository.findByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());
        when(inventoryItemRepository.countLowStockItems()).thenReturn(10L);

        // Act
        DashboardStatsDTO result = dashboardService.getDashboardStats();

        // Assert
        assertNotNull(result);
        assertEquals(largeUserCount, result.getActiveUsers());
        assertEquals(500L, result.getTotalPatients());
        assertEquals(300L, result.getTotalOwners());
        verify(userRepository, times(1)).count();
    }

    @Test
    void getDashboardStats_CalledMultipleTimes_EachCallQueriesRepository() {
        // Arrange
        when(userRepository.count()).thenReturn(5L, 10L, 15L);
        when(patientRepository.countByIsActiveTrue()).thenReturn(20L, 25L, 30L);
        when(ownerRepository.countByIsActiveTrue()).thenReturn(10L, 12L, 14L);
        when(appointmentRepository.findByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());
        when(inventoryItemRepository.countLowStockItems()).thenReturn(3L);

        // Act
        DashboardStatsDTO result1 = dashboardService.getDashboardStats();
        DashboardStatsDTO result2 = dashboardService.getDashboardStats();
        DashboardStatsDTO result3 = dashboardService.getDashboardStats();

        // Assert
        assertEquals(5L, result1.getActiveUsers());
        assertEquals(10L, result2.getActiveUsers());
        assertEquals(15L, result3.getActiveUsers());
        verify(userRepository, times(3)).count();
        verify(patientRepository, times(3)).countByIsActiveTrue();
        verify(ownerRepository, times(3)).countByIsActiveTrue();
    }

    @Test
    void getDashboardStats_FiltersTodayAppointmentsByStatus() {
        // Arrange
        List<Appointment> todayAppointments = new ArrayList<>();

        Appointment scheduled = new Appointment();
        scheduled.setStatus(AppointmentStatus.SCHEDULED);
        todayAppointments.add(scheduled);

        Appointment confirmed = new Appointment();
        confirmed.setStatus(AppointmentStatus.CONFIRMED);
        todayAppointments.add(confirmed);

        Appointment inProgress = new Appointment();
        inProgress.setStatus(AppointmentStatus.IN_PROGRESS);
        todayAppointments.add(inProgress);

        Appointment completed = new Appointment();
        completed.setStatus(AppointmentStatus.COMPLETED);
        todayAppointments.add(completed);

        Appointment cancelled = new Appointment();
        cancelled.setStatus(AppointmentStatus.CANCELLED);
        todayAppointments.add(cancelled);

        when(userRepository.count()).thenReturn(5L);
        when(patientRepository.countByIsActiveTrue()).thenReturn(20L);
        when(ownerRepository.countByIsActiveTrue()).thenReturn(10L);
        when(appointmentRepository.findByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(todayAppointments);
        when(inventoryItemRepository.countLowStockItems()).thenReturn(2L);

        // Act
        DashboardStatsDTO result = dashboardService.getDashboardStats();

        // Assert
        assertEquals(3L, result.getTodayAppointments()); // Only SCHEDULED, CONFIRMED, IN_PROGRESS
    }
}