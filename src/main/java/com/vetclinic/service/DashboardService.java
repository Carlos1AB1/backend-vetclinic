package com.vetclinic.service;

import com.vetclinic.dto.DashboardStatsDTO;
import com.vetclinic.entity.Appointment.AppointmentStatus;
import com.vetclinic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Dashboard Service
 * Provides statistics and metrics for the dashboard
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final OwnerRepository ownerRepository;
    private final AppointmentRepository appointmentRepository;
    private final InventoryItemRepository inventoryItemRepository;

    /**
     * Get dashboard statistics with real data
     */
    public DashboardStatsDTO getDashboardStats() {
        log.info("Obteniendo estadísticas del dashboard");
        
        // Contar usuarios activos
        long activeUsers = userRepository.count();
        
        // Contar pacientes activos
        long totalPatients = patientRepository.countByIsActiveTrue();
        
        // Contar propietarios activos
        long totalOwners = ownerRepository.countByIsActiveTrue();
        
        // Contar citas de hoy
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        log.info("Buscando citas de hoy - Desde: {}, Hasta: {}", startOfDay, endOfDay);
        
        List<com.vetclinic.entity.Appointment> allTodayAppointments = appointmentRepository.findByDateRange(startOfDay, endOfDay);
        log.info("Total de citas encontradas en el rango de hoy: {}", allTodayAppointments.size());
        if (!allTodayAppointments.isEmpty()) {
            for (com.vetclinic.entity.Appointment apt : allTodayAppointments) {
                log.info("Cita ID: {}, Fecha: {}, Estado: {}", apt.getId(), apt.getScheduledDate(), apt.getStatus());
            }
        }
        
        long todayAppointments = allTodayAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED || 
                            a.getStatus() == AppointmentStatus.CONFIRMED ||
                            a.getStatus() == AppointmentStatus.IN_PROGRESS)
                .count();
        log.info("Citas de hoy filtradas por estado (SCHEDULED/CONFIRMED/IN_PROGRESS): {}", todayAppointments);
        
        // Contar items con stock bajo
        long lowStockItems = inventoryItemRepository.countLowStockItems();
        
        // Calcular ingresos mensuales (basado en citas completadas del mes actual)
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now();
        long completedAppointmentsThisMonth = appointmentRepository
                .findByDateRange(startOfMonth, endOfMonth)
                .stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();
        // Estimación: promedio de $50 por cita completada (puede ajustarse según necesidades)
        double monthlyRevenue = completedAppointmentsThisMonth * 50.0;

        log.info("Estadísticas obtenidas - Pacientes: {}, Propietarios: {}, Citas hoy: {}, Stock bajo: {}, Ingresos: {}", 
                totalPatients, totalOwners, todayAppointments, lowStockItems, monthlyRevenue);

        return DashboardStatsDTO.builder()
                .totalPatients(totalPatients)
                .totalOwners(totalOwners)
                .todayAppointments(todayAppointments)
                .lowStockItems(lowStockItems)
                .monthlyRevenue(monthlyRevenue)
                .activeUsers(activeUsers)
                .build();
    }
}
