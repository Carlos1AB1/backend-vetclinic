package com.vetclinic.patterns.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SMS notification strategy implementation (placeholder)
 */
@Component
@Slf4j
public class SmsNotificationStrategy implements NotificationStrategy {

    @Override
    public void send(String recipient, String subject, String message) {
        // TODO: Implement SMS service integration (Twilio, AWS SNS, etc.)
        log.info("SMS notification would be sent to: {} - {}", recipient, message);
        log.warn("SMS service not yet implemented");
    }

    @Override
    public String getChannelType() {
        return "SMS";
    }

    @Override
    public boolean isAvailable() {
        return false; // Not yet implemented
    }
}
