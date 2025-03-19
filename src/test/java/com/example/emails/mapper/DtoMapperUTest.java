package com.example.emails.mapper;

import com.example.emails.model.Email;
import com.example.emails.model.EmailPreference;
import com.example.emails.model.EmailStatus;
import com.example.emails.web.dto.EmailResponse;
import com.example.emails.web.dto.PreferenceResponse;
import com.example.emails.web.mapper.DtoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
public class DtoMapperUTest {

    @Test
    void whenMappingEmailPreferenceToPreferenceResponse() {
        UUID userId = UUID.randomUUID();

        EmailPreference emailPreference = EmailPreference.builder()
                .userId(userId)
                .contactEmail("user@abv.bg")
                .isEnabled(true)
                .build();

        PreferenceResponse preferenceResponse = DtoMapper.fromEmailPreference(emailPreference);

        assertEquals(emailPreference.getUserId(), preferenceResponse.getUserId());
        assertEquals(emailPreference.getContactEmail(), preferenceResponse.getContactEmail());
        assertEquals(emailPreference.isEnabled(), preferenceResponse.isEnabled());
    }

    @Test
    void whenMappingEmailToEmailResponse() {
        Email email = Email.builder()
                .subject("Subject")
                .status(EmailStatus.SUCCEEDED)
                .build();

        EmailResponse emailResponse = DtoMapper.fromEmail(email);

        assertEquals(email.getStatus(), emailResponse.getStatus());
        assertEquals(email.getSubject(), emailResponse.getSubject());
    }
}
