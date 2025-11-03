package com.vetclinic.patterns.strategy;

import com.vetclinic.patterns.adapter.EmailServiceAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Email notification strategy implementation
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationStrategy implements NotificationStrategy {

    private final EmailServiceAdapter emailServiceAdapter;

    @Override
    public void send(String recipient, String subject, String message) {
        try {
            emailServiceAdapter.sendEmail(recipient, subject, message);
            log.info("Email notification sent to: {}", recipient);
        } catch (Exception e) {
            log.error("Failed to send email notification to: {}", recipient, e);
        }
    }

    @Override
    public String getChannelType() {
        return "EMAIL";
    }

    @Override
    public boolean isAvailable() {
        return true; // Can add more sophisticated checks
    }
}
