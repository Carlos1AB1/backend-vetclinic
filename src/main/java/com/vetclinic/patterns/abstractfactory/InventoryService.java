package com.vetclinic.patterns.abstractfactory;

/**
 * Abstract Factory Pattern
 * Interfaz para servicios de inventario específicos por rol
 */
public interface InventoryService {
    
    boolean checkAvailability(String item, int quantity);
    
    String getServiceType();
}

