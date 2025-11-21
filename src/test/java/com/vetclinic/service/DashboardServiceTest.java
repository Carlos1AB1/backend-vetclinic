package com.vetclinic.service;

import com.vetclinic.dto.DashboardStatsDTO;
import com.vetclinic.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        // Setup inicial si es necesario
    }

    @Test
    void getDashboardStats_Success() {
        // Arrange
        long expectedActiveUsers = 10L;
        when(userRepository.count()).thenReturn(expectedActiveUsers);

        // Act
        DashboardStatsDTO result = dashboardService.getDashboardStats();

        // Assert
        assertNotNull(result);
        assertEquals(expectedActiveUsers, result.getActiveUsers());
        assertEquals(0L, result.getTotalPatients());
        assertEquals(0L, result.getTotalOwners());
        assertEquals(0L, result.getTodayAppointments());
        assertEquals(0L, result.getLowStockItems());
        assertEquals(0.0, result.getMonthlyRevenue());

        verify(userRepository).count();
    }

    @Test
    void getDashboardStats_NoUsers_ReturnsZero() {
        // Arrange
        when(userRepository.count()).thenReturn(0L);

        // Act
        DashboardStatsDTO result = dashboardService.getDashboardStats();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getActiveUsers());

        verify(userRepository).count();
    }

    @Test
    void getDashboardStats_PlaceholderValues_AreZero() {
        // Arrange
        when(userRepository.count()).thenReturn(5L);

        // Act
        DashboardStatsDTO result = dashboardService.getDashboardStats();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getTotalPatients(),
                "Total patients should be 0 (placeholder for DEV 2)");
        assertEquals(0L, result.getTotalOwners(),
                "Total owners should be 0 (placeholder for DEV 2)");
        assertEquals(0L, result.getTodayAppointments(),
                "Today appointments should be 0 (placeholder for DEV 2)");
        assertEquals(0L, result.getLowStockItems(),
                "Low stock items should be 0 (placeholder for DEV 3)");
        assertEquals(0.0, result.getMonthlyRevenue(),
                "Monthly revenue should be 0.0 (placeholder)");
    }

    @Test
    void getDashboardStats_MultipleUsers_Success() {
        // Arrange
        long largeUserCount = 1000L;
        when(userRepository.count()).thenReturn(largeUserCount);

        // Act
        DashboardStatsDTO result = dashboardService.getDashboardStats();

        // Assert
        assertNotNull(result);
        assertEquals(largeUserCount, result.getActiveUsers());
        verify(userRepository, times(1)).count();
    }

    @Test
    void getDashboardStats_CalledMultipleTimes_EachCallQueriesRepository() {
        // Arrange
        when(userRepository.count()).thenReturn(5L, 10L, 15L);

        // Act
        DashboardStatsDTO result1 = dashboardService.getDashboardStats();
        DashboardStatsDTO result2 = dashboardService.getDashboardStats();
        DashboardStatsDTO result3 = dashboardService.getDashboardStats();

        // Assert
        assertEquals(5L, result1.getActiveUsers());
        assertEquals(10L, result2.getActiveUsers());
        assertEquals(15L, result3.getActiveUsers());
        verify(userRepository, times(3)).count();
    }
}