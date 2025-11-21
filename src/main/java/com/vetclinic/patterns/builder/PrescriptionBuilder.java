package com.vetclinic.patterns.builder;

import com.vetclinic.entity.MedicalRecord;
import com.vetclinic.entity.Patient;
import com.vetclinic.entity.Prescription;

import java.time.LocalDateTime;

/**
 * Builder Pattern
 * Builder para construir objetos Prescription de forma fluida y legible
 */
public class PrescriptionBuilder {

    private Prescription prescription;

    public PrescriptionBuilder() {
        this.prescription = new Prescription();
        this.prescription.setIsActive(true);
        this.prescription.setStartDate(LocalDateTime.now());
    }

    public PrescriptionBuilder conRegistroMedico(MedicalRecord medicalRecord) {
        this.prescription.setMedicalRecord(medicalRecord);
        return this;
    }

    public PrescriptionBuilder conPaciente(Patient patient) {
        this.prescription.setPatient(patient);
        return this;
    }

    public PrescriptionBuilder conMedicamento(String medicationName) {
        this.prescription.setMedicationName(medicationName);
        return this;
    }

    public PrescriptionBuilder conDosis(String dosage) {
        this.prescription.setDosage(dosage);
        return this;
    }

    public PrescriptionBuilder conFrecuencia(String frequency) {
        this.prescription.setFrequency(frequency);
        return this;
    }

    public PrescriptionBuilder conDuracion(String duration) {
        this.prescription.setDuration(duration);
        return this;
    }

    public PrescriptionBuilder conInstrucciones(String instructions) {
        this.prescription.setInstructions(instructions);
        return this;
    }

    public PrescriptionBuilder conFechaInicio(LocalDateTime startDate) {
        this.prescription.setStartDate(startDate != null ? startDate : LocalDateTime.now());
        return this;
    }

    public PrescriptionBuilder conFechaFin(LocalDateTime endDate) {
        this.prescription.setEndDate(endDate);
        return this;
    }

    public PrescriptionBuilder conNotas(String notes) {
        this.prescription.setNotes(notes);
        return this;
    }

    public PrescriptionBuilder activo(boolean isActive) {
        this.prescription.setIsActive(isActive);
        return this;
    }

    /**
     * Construir el objeto Prescription
     * Valida que los campos requeridos estén presentes
     */
    public Prescription build() {
        // Validaciones
        if (this.prescription.getMedicalRecord() == null) {
            throw new IllegalStateException("El registro médico es requerido");
        }
        if (this.prescription.getPatient() == null) {
            throw new IllegalStateException("El paciente es requerido");
        }
        if (this.prescription.getMedicationName() == null || this.prescription.getMedicationName().trim().isEmpty()) {
            throw new IllegalStateException("El nombre del medicamento es requerido");
        }
        if (this.prescription.getDosage() == null || this.prescription.getDosage().trim().isEmpty()) {
            throw new IllegalStateException("La dosis es requerida");
        }
        if (this.prescription.getFrequency() == null || this.prescription.getFrequency().trim().isEmpty()) {
            throw new IllegalStateException("La frecuencia es requerida");
        }
        if (this.prescription.getDuration() == null || this.prescription.getDuration().trim().isEmpty()) {
            throw new IllegalStateException("La duración es requerida");
        }
        if (this.prescription.getStartDate() == null) {
            this.prescription.setStartDate(LocalDateTime.now());
        }

        return this.prescription;
    }
}

