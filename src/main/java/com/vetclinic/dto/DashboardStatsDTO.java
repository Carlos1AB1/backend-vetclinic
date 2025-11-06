package com.vetclinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dashboard Statistics DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private Long totalPatients;
    private Long totalOwners;
    private Long todayAppointments;
    private Long lowStockItems;
    private Double monthlyRevenue;
    private Long activeUsers;
}
