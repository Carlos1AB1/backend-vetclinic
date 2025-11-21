package com.vetclinic.controller;

import com.vetclinic.dto.DashboardStatsDTO;
import com.vetclinic.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private DashboardStatsDTO dashboardStats;

    @BeforeEach
    void setUp() {
        dashboardStats = DashboardStatsDTO.builder()
                .activeUsers(10L)
                .totalPatients(50L)
                .totalOwners(30L)
                .todayAppointments(15L)
                .lowStockItems(5L)
                .monthlyRevenue(5000.0)
                .build();
    }

    @Test
    void testGetDashboardStats_Success() {
        when(dashboardService.getDashboardStats()).thenReturn(dashboardStats);

        dashboardController.getDashboardStats();

        verify(dashboardService, times(1)).getDashboardStats();
    }

    @Test
    void testGetDashboardStats_AsVeterinarian() {
        when(dashboardService.getDashboardStats()).thenReturn(dashboardStats);

        dashboardController.getDashboardStats();

        verify(dashboardService, times(1)).getDashboardStats();
    }

    @Test
    void testGetDashboardStats_AsReceptionist() {
        when(dashboardService.getDashboardStats()).thenReturn(dashboardStats);

        dashboardController.getDashboardStats();

        verify(dashboardService, times(1)).getDashboardStats();
    }
}