package com.example.emails.service;

import com.example.emails.model.Email;
import com.example.emails.model.EmailPreference;
import com.example.emails.model.EmailStatus;
import com.example.emails.repository.EmailPreferenceRepository;
import com.example.emails.repository.EmailRepository;
import com.example.emails.web.dto.EmailRequest;
import com.example.emails.web.dto.UpsertPreference;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailService {

    private final EmailPreferenceRepository emailPreferenceRepository;
    private final MailSender mailSender;
    private final EmailRepository emailRepository;

    public EmailService(EmailPreferenceRepository emailPreferenceRepository,
                        MailSender mailSender,
                        EmailRepository emailRepository) {
        this.emailPreferenceRepository = emailPreferenceRepository;
        this.mailSender = mailSender;
        this.emailRepository = emailRepository;
    }

    public EmailPreference upsertPreference(UpsertPreference upsertPreference) {

        Optional<EmailPreference> preferenceOptional =
                emailPreferenceRepository.findByUserId(upsertPreference.getUserId());

        if (preferenceOptional.isPresent()) {
            EmailPreference emailPreference = preferenceOptional.get();

            emailPreference.setContactEmail(upsertPreference.getContactEmail());
            emailPreference.setEnabled(upsertPreference.isEnabled());
            emailPreference.setUpdatedOn(LocalDateTime.now());

            return emailPreferenceRepository.save(emailPreference);
        }

        EmailPreference emailPreference = EmailPreference.builder()
                .userId(upsertPreference.getUserId())
                .isEnabled(upsertPreference.isEnabled())
                .contactEmail(upsertPreference.getContactEmail())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return emailPreferenceRepository.save(emailPreference);
    }

    public EmailPreference getPreferenceByUserId(UUID userId) {
        return emailPreferenceRepository
                .findByUserId(userId)
                .orElseThrow(() -> new NullPointerException("Email preference for user id [%s] was not found."
                        .formatted(userId)));
    }

    public Email sendEmail(EmailRequest emailRequest) {
        UUID userId = emailRequest.getUserId();
        EmailPreference userPreference = getPreferenceByUserId(userId);

        if (!userPreference.isEnabled()) {
            throw new IllegalArgumentException("User with id [%s] does not allow receive emails."
                    .formatted(userPreference.getUserId()));
        }

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(userPreference.getContactEmail());
        simpleMailMessage.setSubject(emailRequest.getSubject());
        simpleMailMessage.setText(emailRequest.getBody());

        Email email = Email.builder()
                        .subject(emailRequest.getSubject())
                        .body(emailRequest.getBody())
                        .createdOn(LocalDateTime.now())
                        .userId(userId)
                        .build();

        try {
            mailSender.send(simpleMailMessage);
            email.setStatus(EmailStatus.SUCCEEDED);
        }  catch (Exception e) {
            email.setStatus(EmailStatus.FAILED);
        }

        return emailRepository.save(email);
    }

    public EmailPreference changePreference(UUID userId, boolean enabled) {

        EmailPreference emailPreference = getPreferenceByUserId(userId);
        emailPreference.setEnabled(enabled);
        return emailPreferenceRepository.save(emailPreference);
    }
}
