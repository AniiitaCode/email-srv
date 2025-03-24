package com.example.emails.web;

import com.example.emails.model.Email;
import com.example.emails.model.EmailPreference;
import com.example.emails.model.EmailStatus;
import com.example.emails.repository.EmailPreferenceRepository;
import com.example.emails.service.EmailService;
import com.example.emails.web.dto.EmailRequest;
import com.example.emails.web.dto.EmailResponse;
import com.example.emails.web.dto.PreferenceResponse;
import com.example.emails.web.dto.UpsertPreference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailController.class)
public class EmailControllerApiTest {

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailPreferenceRepository emailPreferenceRepository;
    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Test
    void getUserPreference_happyPath() throws Exception {
        UUID userId = UUID.randomUUID();

        EmailPreference emailPreference = EmailPreference.builder()
                .userId(userId)
                .contactEmail("user@abv.bg")
                .isEnabled(true)
                .build();

        PreferenceResponse preferenceResponse = PreferenceResponse.builder()
                .enabled(emailPreference.isEnabled())
                .userId(emailPreference.getUserId())
                .contactEmail(emailPreference.getContactEmail())
                .id(emailPreference.getId())
                .build();

        when(emailService.getPreferenceByUserId(userId)).thenReturn(emailPreference);

        MockHttpServletRequestBuilder request =
                get("/api/v1/emails/preferences").param("userId", userId.toString());


        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("userId").isNotEmpty())
                .andExpect(jsonPath("enabled").isNotEmpty())
                .andExpect(jsonPath("contactEmail").isNotEmpty());
    }

    @Test
    void postUpsertPreference_returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        UpsertPreference upsertPreference = new UpsertPreference();
        upsertPreference.setUserId(userId);
        upsertPreference.setEnabled(true);
        upsertPreference.setContactEmail("contact@example.com");

        EmailPreference emailPreference = EmailPreference.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .isEnabled(true)
                .contactEmail("contact@example.com")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        PreferenceResponse preferenceResponse = PreferenceResponse.builder()
                .id(emailPreference.getId())
                .userId(emailPreference.getUserId())
                .enabled(emailPreference.isEnabled())
                .contactEmail(emailPreference.getContactEmail())
                .build();

        when(emailService.upsertPreference(upsertPreference)).thenReturn(emailPreference);

        MockHttpServletRequestBuilder request = post("/api/v1/emails/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper.writeValueAsString(upsertPreference));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(preferenceResponse.getId().toString()))
                .andExpect(jsonPath("$.userId").value(preferenceResponse.getUserId().toString()))
                .andExpect(jsonPath("$.enabled").value(preferenceResponse.isEnabled()))
                .andExpect(jsonPath("$.contactEmail").value(preferenceResponse.getContactEmail()));
    }

    @Test
    void postSendEmail() throws Exception {
        UUID userId = UUID.randomUUID();
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setUserId(userId);
        emailRequest.setBody("test");
        emailRequest.setSubject("test");

        Email email = Email.builder()
                .body("test")
                .subject("test")
                .userId(userId)
                .id(UUID.randomUUID())
                .status(EmailStatus.SUCCEEDED)
                .createdOn(LocalDateTime.now())
                .build();

        EmailResponse emailResponse = EmailResponse.builder()
                .status(email.getStatus())
                .subject(email.getSubject())
                .createdOn(email.getCreatedOn())
                .build();

        when(emailService.sendEmail(emailRequest)).thenReturn(email);

        MockHttpServletRequestBuilder request = post("/api/v1/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper.writeValueAsString(emailRequest));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject").value(emailResponse.getSubject()))
                .andExpect(jsonPath("status").isNotEmpty())
                .andExpect(jsonPath("createdOn").isNotEmpty());
    }

    @Test
    void putChangePreference() throws Exception {
        UUID userId = UUID.randomUUID();
        boolean enabled = true;

        EmailPreference emailPreference = EmailPreference.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .isEnabled(enabled)
                .contactEmail("contact@example.com")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();


        PreferenceResponse preferenceResponse = PreferenceResponse.builder()
                .id(emailPreference.getId())
                .userId(emailPreference.getUserId())
                .enabled(emailPreference.isEnabled())
                .contactEmail(emailPreference.getContactEmail())
                .build();

        when(emailService.changePreference(userId, enabled)).thenReturn(emailPreference);

        MockHttpServletRequestBuilder request = put("/api/v1/emails/preferences")
                .param("userId", userId.toString())
                .param("enabled", String.valueOf(enabled))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(preferenceResponse.getId().toString()))
                .andExpect(jsonPath("$.userId").value(preferenceResponse.getUserId().toString()))
                .andExpect(jsonPath("$.enabled").value(preferenceResponse.isEnabled()))
                .andExpect(jsonPath("$.contactEmail").value(preferenceResponse.getContactEmail()));
    }
}







