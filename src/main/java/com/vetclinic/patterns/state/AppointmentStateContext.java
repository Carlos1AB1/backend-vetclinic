package com.vetclinic.patterns.state;

import com.vetclinic.entity.Appointment;
import com.vetclinic.entity.Appointment.AppointmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * State Pattern
 * Contexto que mantiene el estado actual de la cita y delega las operaciones
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentStateContext {

    private final ScheduledState scheduledState;
    private final ConfirmedState confirmedState;
    private final CancelledState cancelledState;
    private final CompletedState completedState;

    /**
     * Obtener el estado correspondiente según el AppointmentStatus
     */
    public AppointmentState getState(Appointment appointment) {
        AppointmentStatus status = appointment.getStatus();
        
        return switch (status) {
            case SCHEDULED -> scheduledState;
            case CONFIRMED -> confirmedState;
            case CANCELLED -> cancelledState;
            case COMPLETED -> completedState;
            case IN_PROGRESS -> {
                // Para IN_PROGRESS, usamos un estado temporal
                yield new InProgressState();
            }
            case NO_SHOW -> {
                // Para NO_SHOW, usamos un estado temporal
                yield new NoShowState();
            }
        };
    }

    /**
     * Confirmar la cita usando el patrón State
     */
    public void confirm(Appointment appointment) {
        AppointmentState state = getState(appointment);
        state.confirm(appointment);
    }

    /**
     * Cancelar la cita usando el patrón State
     */
    public void cancel(Appointment appointment) {
        AppointmentState state = getState(appointment);
        state.cancel(appointment);
    }

    /**
     * Iniciar la cita usando el patrón State
     */
    public void start(Appointment appointment) {
        AppointmentState state = getState(appointment);
        state.start(appointment);
    }

    /**
     * Completar la cita usando el patrón State
     */
    public void complete(Appointment appointment) {
        AppointmentState state = getState(appointment);
        state.complete(appointment);
    }

    /**
     * Verificar si se pueden enviar recordatorios
     */
    public boolean canSendReminders(Appointment appointment) {
        AppointmentState state = getState(appointment);
        return state.canSendReminders();
    }

    /**
     * Verificar si la cita puede ser reprogramada
     */
    public boolean canBeRescheduled(Appointment appointment) {
        AppointmentState state = getState(appointment);
        return state.canBeRescheduled();
    }

    /**
     * Estado temporal para IN_PROGRESS
     */
    private static class InProgressState implements AppointmentState {
        @Override
        public void confirm(Appointment appointment) {
            throw new IllegalStateException("No se puede confirmar una cita en progreso");
        }

        @Override
        public void cancel(Appointment appointment) {
            throw new IllegalStateException("No se puede cancelar una cita en progreso");
        }

        @Override
        public void start(Appointment appointment) {
            // Ya está iniciada
        }

        @Override
        public void complete(Appointment appointment) {
            appointment.setStatus(AppointmentStatus.COMPLETED);
        }

        @Override
        public String getStateName() {
            return "IN_PROGRESS";
        }

        @Override
        public boolean canSendReminders() {
            return false;
        }

        @Override
        public boolean canBeRescheduled() {
            return false;
        }
    }

    /**
     * Estado temporal para NO_SHOW
     */
    private static class NoShowState implements AppointmentState {
        @Override
        public void confirm(Appointment appointment) {
            throw new IllegalStateException("No se puede confirmar una cita con NO_SHOW");
        }

        @Override
        public void cancel(Appointment appointment) {
            throw new IllegalStateException("No se puede cancelar una cita con NO_SHOW");
        }

        @Override
        public void start(Appointment appointment) {
            throw new IllegalStateException("No se puede iniciar una cita con NO_SHOW");
        }

        @Override
        public void complete(Appointment appointment) {
            throw new IllegalStateException("No se puede completar una cita con NO_SHOW");
        }

        @Override
        public String getStateName() {
            return "NO_SHOW";
        }

        @Override
        public boolean canSendReminders() {
            return false;
        }

        @Override
        public boolean canBeRescheduled() {
            return true; // Se puede reprogramar
        }
    }
}

