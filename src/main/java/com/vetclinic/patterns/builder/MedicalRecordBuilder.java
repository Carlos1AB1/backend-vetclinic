package com.vetclinic.patterns.builder;

import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.MedicalRecord;
import com.vetclinic.entity.Patient;
import com.vetclinic.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Builder Pattern
 * Builder para construir objetos MedicalRecord de forma fluida y legible
 */
public class MedicalRecordBuilder {

    private MedicalRecord medicalRecord;

    public MedicalRecordBuilder() {
        this.medicalRecord = new MedicalRecord();
        this.medicalRecord.setIsActive(true);
        this.medicalRecord.setFollowUpRequired(false);
    }

    public MedicalRecordBuilder conPaciente(Patient patient) {
        this.medicalRecord.setPatient(patient);
        return this;
    }

    public MedicalRecordBuilder conVeterinario(User veterinarian) {
        this.medicalRecord.setVeterinarian(veterinarian);
        return this;
    }

    public MedicalRecordBuilder conCita(Appointment appointment) {
        this.medicalRecord.setAppointment(appointment);
        return this;
    }

    public MedicalRecordBuilder conFechaRegistro(LocalDateTime recordDate) {
        this.medicalRecord.setRecordDate(recordDate != null ? recordDate : LocalDateTime.now());
        return this;
    }

    public MedicalRecordBuilder conDiagnostico(String diagnosis) {
        this.medicalRecord.setDiagnosis(diagnosis);
        return this;
    }

    public MedicalRecordBuilder conTratamiento(String treatment) {
        this.medicalRecord.setTreatment(treatment);
        return this;
    }

    public MedicalRecordBuilder conSintomas(String symptoms) {
        this.medicalRecord.setSymptoms(symptoms);
        return this;
    }

    public MedicalRecordBuilder conSignosVitales(String vitalSigns) {
        this.medicalRecord.setVitalSigns(vitalSigns);
        return this;
    }

    public MedicalRecordBuilder conPeso(BigDecimal weight) {
        this.medicalRecord.setWeight(weight);
        return this;
    }

    public MedicalRecordBuilder conTemperatura(BigDecimal temperature) {
        this.medicalRecord.setTemperature(temperature);
        return this;
    }

    public MedicalRecordBuilder conFrecuenciaCardiaca(BigDecimal heartRate) {
        this.medicalRecord.setHeartRate(heartRate);
        return this;
    }

    public MedicalRecordBuilder conNotas(String notes) {
        this.medicalRecord.setNotes(notes);
        return this;
    }

    public MedicalRecordBuilder requiereSeguimiento(boolean followUpRequired) {
        this.medicalRecord.setFollowUpRequired(followUpRequired);
        return this;
    }

    public MedicalRecordBuilder conFechaSeguimiento(LocalDateTime followUpDate) {
        this.medicalRecord.setFollowUpDate(followUpDate);
        return this;
    }

    public MedicalRecordBuilder activo(boolean isActive) {
        this.medicalRecord.setIsActive(isActive);
        return this;
    }

    /**
     * Construir el objeto MedicalRecord
     * Valida que los campos requeridos estén presentes
     */
    public MedicalRecord build() {
        // Validaciones
        if (this.medicalRecord.getPatient() == null) {
            throw new IllegalStateException("El paciente es requerido");
        }
        if (this.medicalRecord.getVeterinarian() == null) {
            throw new IllegalStateException("El veterinario es requerido");
        }
        if (this.medicalRecord.getRecordDate() == null) {
            this.medicalRecord.setRecordDate(LocalDateTime.now());
        }
        if (this.medicalRecord.getDiagnosis() == null || this.medicalRecord.getDiagnosis().trim().isEmpty()) {
            throw new IllegalStateException("El diagnóstico es requerido");
        }
        if (this.medicalRecord.getTreatment() == null || this.medicalRecord.getTreatment().trim().isEmpty()) {
            throw new IllegalStateException("El tratamiento es requerido");
        }

        return this.medicalRecord;
    }
}

