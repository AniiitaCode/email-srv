package com.example.emails.service;

import com.example.emails.model.Email;
import com.example.emails.model.EmailPreference;
import com.example.emails.model.EmailStatus;
import com.example.emails.repository.EmailPreferenceRepository;
import com.example.emails.repository.EmailRepository;
import com.example.emails.web.dto.EmailRequest;
import com.example.emails.web.dto.UpsertPreference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceUTest {

    @Mock
    private EmailPreferenceRepository emailPreferenceRepository;

    @Mock
    private MailSender mailSender;

    @Mock
    private EmailRepository emailRepository;

    @InjectMocks
    private EmailService emailService;


    @Test
    void whenChangePreferenceByUserId_doesNotExistPreference_throwsException() {
        UUID userId = UUID.randomUUID();
        boolean isEnable = true;

        when(emailPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());

        NullPointerException exception =
                assertThrows(NullPointerException.class, () ->
                        emailService.changePreference(userId, isEnable));

        assertEquals("Email preference for user id [%s] was not found.".formatted(userId),
                exception.getMessage());
    }

    @Test
    void whenChangePreferenceByUserId_doesExistPreference_thenSetEnable() {
        UUID userId = UUID.randomUUID();
        boolean isEnable = true;
        EmailPreference emailPreference = EmailPreference.builder()
                .isEnabled(false)
                .build();

        when(emailPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(emailPreference));

        emailService.changePreference(userId, isEnable);

        assertTrue(emailPreference.isEnabled());
        verify(emailPreferenceRepository, times(1)).save(emailPreference);
    }

    @Test
    void whenUpsertPreference_preferenceExist_thenUpdatePreference() {
        UUID userId = UUID.randomUUID();
        UpsertPreference upsertPreference = new UpsertPreference();
        upsertPreference.setContactEmail("new_email@abv.bg");
        upsertPreference.setEnabled(true);
        upsertPreference.setUserId(userId);

        EmailPreference excitingPreference = EmailPreference.builder()
                .userId(userId)
                .contactEmail("old_email@abv.bg")
                .isEnabled(false)
                .build();

        when(emailPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(excitingPreference));
        when(emailPreferenceRepository.save(any(EmailPreference.class))).thenReturn(excitingPreference);

        EmailPreference updatedPreference = emailService.upsertPreference(upsertPreference);

        assertEquals("new_email@abv.bg", updatedPreference.getContactEmail());
        assertTrue(updatedPreference.isEnabled());

        verify(emailPreferenceRepository, times(1)).save(excitingPreference);
    }

    @Test
    void whenUpsertPreference_preferenceDoesNotExist_thenInsertPreference() {
        UUID userId = UUID.randomUUID();
        String contactEmail = "new_email@abv.bg";
        boolean isEnable = true;

        UpsertPreference upsertPreference = new UpsertPreference();
        upsertPreference.setUserId(userId);
        upsertPreference.setContactEmail(contactEmail);
        upsertPreference.setEnabled(isEnable);

        when(emailPreferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());

        EmailPreference newPreference = EmailPreference.builder()
                .userId(userId)
                .contactEmail(contactEmail)
                .isEnabled(isEnable)
                .build();

        when(emailPreferenceRepository.save(any(EmailPreference.class))).thenReturn(newPreference);

        EmailPreference createdPreference = emailService.upsertPreference(upsertPreference);

        assertEquals(contactEmail, createdPreference.getContactEmail());
        assertEquals(isEnable, createdPreference.isEnabled());

        verify(emailPreferenceRepository, times(1)).save(any(EmailPreference.class));
    }

    @Test
    void testSendEmail_UserDisabled() {
        UUID userId = UUID.randomUUID();
        EmailPreference userPreference = new EmailPreference();
        userPreference.setUserId(userId);
        userPreference.setContactEmail("user@abv.bg");
        userPreference.setEnabled(false);

        when(emailPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(userPreference));

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setUserId(userId);
        emailRequest.setSubject("Subject");
        emailRequest.setBody("Body");

        assertThrows(IllegalArgumentException.class, () -> emailService.sendEmail(emailRequest));
    }

    @Test
    void testSendEmail_Success() throws Exception {
        UUID userId = UUID.randomUUID();
        EmailPreference userPreference = new EmailPreference();
        userPreference.setUserId(userId);
        userPreference.setContactEmail("user@abv.bg");
        userPreference.setEnabled(true);
        when(emailPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(userPreference));

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setUserId(userId);
        emailRequest.setBody("Body");
        emailRequest.setSubject("Subject");

        Email expectedEmail = new Email();
        expectedEmail.setStatus(EmailStatus.SUCCEEDED);
        expectedEmail.setBody("Body");
        expectedEmail.setSubject("Subject");
        expectedEmail.setUserId(userId);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(userPreference.getContactEmail());
        simpleMailMessage.setSubject(emailRequest.getSubject());
        simpleMailMessage.setText(emailRequest.getBody());

        doNothing().when(mailSender).send(simpleMailMessage);
        when(emailRepository.save(any(Email.class))).thenReturn(expectedEmail);

        Email result = emailService.sendEmail(emailRequest);

        assertEquals(EmailStatus.SUCCEEDED, result.getStatus());
        verify(mailSender).send(simpleMailMessage);
        verify(emailRepository).save(any(Email.class));
    }

    @Test
    void testSendEmail_Failure() throws Exception {
        UUID userId = UUID.randomUUID();
        EmailPreference userPreference = new EmailPreference();
        userPreference.setUserId(userId);
        userPreference.setContactEmail("user@abv.bg");
        userPreference.setEnabled(true);

        when(emailPreferenceRepository.findByUserId(userId)).thenReturn(Optional.of(userPreference));

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setSubject("Subject");
        emailRequest.setBody("Body");
        emailRequest.setUserId(userId);

        Email expectedEmail = new Email();
        expectedEmail.setUserId(userId);
        expectedEmail.setSubject("Subject");
        expectedEmail.setBody("Body");
        expectedEmail.setStatus(EmailStatus.FAILED);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(userPreference.getContactEmail());
        simpleMailMessage.setSubject(emailRequest.getSubject());
        simpleMailMessage.setText(emailRequest.getBody());

        doThrow(new RuntimeException("Sending email failed")).when(mailSender).send(simpleMailMessage);
        when(emailRepository.save(any(Email.class))).thenReturn(expectedEmail);

        Email result = emailService.sendEmail(emailRequest);

        assertEquals(EmailStatus.FAILED, result.getStatus());
        verify(mailSender).send(simpleMailMessage);
        verify(emailRepository).save(any(Email.class));
    }
}

