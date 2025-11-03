package com.vetclinic.service;

import com.vetclinic.dto.DashboardStatsDTO;
import com.vetclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Dashboard Service
 * Provides statistics and metrics for the dashboard
 * Note: Full implementation pending DEV 2 and DEV 3 modules
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;

    /**
     * Get dashboard statistics
     * Returns default/mock values until DEV 2 and DEV 3 are completed
     */
    public DashboardStatsDTO getDashboardStats() {
        // Count active users (only available stat in DEV 1)
        long activeUsers = userRepository.count();

        // Return stats with available data and placeholders for DEV 2/3
        return DashboardStatsDTO.builder()
                .totalPatients(0L) // TODO: Implement in DEV 2
                .totalOwners(0L)   // TODO: Implement in DEV 2
                .todayAppointments(0L) // TODO: Implement in DEV 2
                .lowStockItems(0L) // TODO: Implement in DEV 3
                .monthlyRevenue(0.0) // TODO: Implement revenue calculation
                .activeUsers(activeUsers)
                .build();
    }
}
