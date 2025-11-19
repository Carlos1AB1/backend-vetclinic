package com.vetclinic.patterns.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationStrategyTest {

    @Mock
    private NotificationStrategy strategy;

    @Test
    void notificationStrategy_IsInterface() {
        // Assert
        assertTrue(NotificationStrategy.class.isInterface());
    }

    @Test
    void notificationStrategy_HasSendMethod() {
        // Assert
        assertDoesNotThrow(() -> NotificationStrategy.class.getMethod("send", String.class, String.class, String.class));
    }
}
