package com.vetclinic.patterns.adapter;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Twilio SMS Adapter
 * Adapts Twilio SDK to our SmsServiceAdapter interface
 */
@Component
@Slf4j
public class TwilioSmsAdapter implements SmsServiceAdapter {

    @Value("${app.twilio.account-sid:}")
    private String accountSid;

    @Value("${app.twilio.auth-token:}")
    private String authToken;

    @Value("${app.twilio.phone-number:}")
    private String phoneNumber;

    private boolean isConfigured = false;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isEmpty() &&
            authToken != null && !authToken.isEmpty() &&
            phoneNumber != null && !phoneNumber.isEmpty()) {
            try {
                Twilio.init(accountSid, authToken);
                isConfigured = true;
                log.info("Twilio SMS service initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Twilio SMS service", e);
                isConfigured = false;
            }
        } else {
            log.warn("Twilio SMS service not configured - missing credentials");
        }
    }

    @Override
    public void sendSms(String to, String message) {
        if (!isAvailable()) {
            log.warn("Twilio SMS service not available - SMS not sent to: {}", to);
            return;
        }

        try {
            // Ensure phone number is in E.164 format
            String formattedTo = formatPhoneNumber(to);
            
            Message twilioMessage = Message.creator(
                    new PhoneNumber(formattedTo),
                    new PhoneNumber(phoneNumber),
                    message
            ).create();

            log.info("SMS sent successfully to: {} - Message SID: {}", formattedTo, twilioMessage.getSid());
        } catch (Exception e) {
            log.error("Error sending SMS to: {}", to, e);
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return isConfigured;
    }

    /**
     * Format phone number to E.164 format
     * E.164 format: +[country code][number] (e.g., +573183487086)
     * Detects Colombian numbers (10 digits) and adds +57
     */
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        // Remove all non-digit characters except +
        String cleaned = phone.replaceAll("[^\\d+]", "");
        
        // If it already starts with +, return as is
        if (cleaned.startsWith("+")) {
            return cleaned;
        }
        
        // Detect Colombian numbers: 10 digits (cellphones start with 3, landlines with other digits)
        if (cleaned.length() == 10) {
            // Colombian numbers: add +57
            cleaned = "+57" + cleaned;
        } else if (cleaned.startsWith("57") && cleaned.length() == 12) {
            // Already has country code 57 but missing +
            cleaned = "+" + cleaned;
        } else if (cleaned.startsWith("1") && cleaned.length() == 11) {
            // US/Canada number starting with 1 (11 digits total)
            cleaned = "+" + cleaned;
        } else {
            // For other cases, try to add + if missing
            cleaned = "+" + cleaned;
        }
        
        return cleaned;
    }
}

