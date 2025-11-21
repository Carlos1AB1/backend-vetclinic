package com.vetclinic.patterns.abstractfactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Abstract Factory Pattern
 * Proveedor que selecciona la fábrica correcta según el rol del usuario
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceFactoryProvider {

    private final List<UserServiceFactory> factories;

    /**
     * Obtener la fábrica de servicios según el rol
     * 
     * @param roleType Tipo de rol: VETERINARIO, RECEPCIONISTA, ADMINISTRADOR
     * @return Fábrica de servicios correspondiente
     */
    public UserServiceFactory getFactory(String roleType) {
        if (roleType == null || roleType.trim().isEmpty()) {
            log.warn("Rol no especificado, usando RECEPCIONISTA por defecto");
            return getFactoryByType("RECEPCIONISTA");
        }

        String normalizedRole = roleType.toUpperCase().trim();
        return getFactoryByType(normalizedRole);
    }

    private UserServiceFactory getFactoryByType(String roleType) {
        return factories.stream()
            .filter(factory -> factory.getRoleType().equalsIgnoreCase(roleType))
            .findFirst()
            .orElseGet(() -> {
                log.warn("Fábrica no encontrada para rol: {}. Usando RECEPCIONISTA por defecto", roleType);
                return factories.stream()
                    .filter(f -> f.getRoleType().equalsIgnoreCase("RECEPCIONISTA"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No hay fábricas disponibles"));
            });
    }
}

