package com.vetclinic.patterns.strategy;

import com.vetclinic.patterns.adapter.EmailServiceAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationStrategyTest {

    @Mock
    private EmailServiceAdapter emailAdapter;

    @InjectMocks
    private EmailNotificationStrategy emailStrategy;

    private String testRecipient;
    private String testSubject;
    private String testMessage;

    @BeforeEach
    void setUp() {
        testRecipient = "user@example.com";
        testSubject = "Test Subject";
        testMessage = "Test notification message";
    }

    @Test
    void send_ValidParameters_SendsEmailSuccessfully() {
        // Act
        emailStrategy.send(testRecipient, testSubject, testMessage);

        // Assert
        verify(emailAdapter, times(1)).sendEmail(testRecipient, testSubject, testMessage);
    }

    @Test
    void send_UsesCorrectRecipient() {
        // Act
        emailStrategy.send(testRecipient, testSubject, testMessage);

        // Assert
        verify(emailAdapter).sendEmail(eq(testRecipient), anyString(), anyString());
    }

    @Test
    void send_ImplementsNotificationStrategy() {
        // Assert
        assertInstanceOf(NotificationStrategy.class, emailStrategy);
    }

    @Test
    void send_WithNullRecipient_DoesNotSend() {
        // Act
        emailStrategy.send(null, testSubject, testMessage);

        // Assert
        verify(emailAdapter, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void send_WithEmptyMessage_StillSends() {
        // Act
        emailStrategy.send(testRecipient, testSubject, "");

        // Assert
        verify(emailAdapter, times(1)).sendEmail(eq(testRecipient), eq(testSubject), eq(""));
    }

    @Test
    void getChannelType_ReturnsEmail() {
        // Assert
        assertEquals("EMAIL", emailStrategy.getChannelType());
    }

    @Test
    void isAvailable_ReturnsTrue() {
        // Assert
        assertTrue(emailStrategy.isAvailable());
    }
}
