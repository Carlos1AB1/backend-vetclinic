// java
package com.vetclinic.patterns.adapter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringMailAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private SpringMailAdapter emailAdapter;

    private String testEmail;
    private String testSubject;
    private String testBody;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testSubject = "Test Subject";
        testBody = "Test Body";
    }

    @Test
    void sendEmail_ValidParameters_SendsSuccessfully() {
        // Act
        emailAdapter.sendEmail(testEmail, testSubject, testBody);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_CreatesCorrectMessage() {
        // Arrange & Act
        emailAdapter.sendEmail(testEmail, testSubject, testBody);

        // Assert using ArgumentCaptor to avoid overload ambiguity and type inference issues
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();

        assertNotNull(sent);
        assertArrayEquals(new String[]{testEmail}, sent.getTo());
        assertEquals(testSubject, sent.getSubject());
        assertEquals(testBody, sent.getText());
    }

    @Test
    void sendEmail_WithException_ThrowsRuntimeException() {
        // Arrange
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                emailAdapter.sendEmail(testEmail, testSubject, testBody)
        );
    }

    @Test
    void sendHtmlEmail_ValidParameters_SendsSuccessfully() throws MessagingException {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        String htmlBody = "<html><body>Test HTML</body></html>";

        // Act
        emailAdapter.sendHtmlEmail(testEmail, testSubject, htmlBody);

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendHtmlEmail_WithMessagingException_ThrowsRuntimeException() throws MessagingException {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("MIME error"))
                .when(mailSender).send(any(MimeMessage.class));
        String htmlBody = "<html><body>Test HTML</body></html>";

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                emailAdapter.sendHtmlEmail(testEmail, testSubject, htmlBody)
        );
    }

    @Test
    void sendEmailWithAttachment_ValidParameters_SendsSuccessfully() throws MessagingException {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        String attachmentPath = "/path/to/file.pdf";

        // Act
        emailAdapter.sendEmailWithAttachment(testEmail, testSubject, testBody, attachmentPath);

        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmailWithAttachment_WithMessagingException_ThrowsRuntimeException() throws MessagingException {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("MIME error"))
                .when(mailSender).send(any(MimeMessage.class));
        String attachmentPath = "/path/to/file.pdf";

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                emailAdapter.sendEmailWithAttachment(testEmail, testSubject, testBody, attachmentPath)
        );
    }

    @Test
    void sendEmail_NullEmail_StillSends() {
        // Act
        emailAdapter.sendEmail(null, testSubject, testBody);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }


    @Test
    void sendHtmlEmail_NullSubject_ThrowsException() throws MessagingException {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act & Assert
        assertThrows(Exception.class, () ->
                emailAdapter.sendHtmlEmail(testEmail, null, "<html></html>")
        );
    }

    @Test
    void implementsEmailServiceAdapter_Success() {
        // Assert
        assertInstanceOf(EmailServiceAdapter.class, emailAdapter);
    }
}
