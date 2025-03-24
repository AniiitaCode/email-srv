package com.example.emails;

import com.example.emails.model.EmailPreference;
import com.example.emails.repository.EmailPreferenceRepository;
import com.example.emails.service.EmailService;
import com.example.emails.web.dto.UpsertPreference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class UpsertPreferenceITest {

    @Autowired
    private EmailPreferenceRepository emailPreferenceRepository;

    @Autowired
    private EmailService emailService;

    @Test
    void testUpsertPreference() {
        UUID userId = UUID.randomUUID();

        UpsertPreference upsertPreference = new UpsertPreference();
        upsertPreference.setUserId(userId);
        upsertPreference.setContactEmail("user123@example.com");
        upsertPreference.setEnabled(true);

        EmailPreference createdPreference =
                emailService.upsertPreference(upsertPreference);

        assertNotNull(createdPreference);
        assertEquals(upsertPreference.getUserId(), createdPreference.getUserId());
        assertEquals(upsertPreference.getContactEmail(), createdPreference.getContactEmail());
        assertTrue(createdPreference.isEnabled());
        assertNotNull(createdPreference.getCreatedOn());
        assertNotNull(createdPreference.getUpdatedOn());
    }

    @Test
    void testUpsertPreference_withExistingPreference_shouldUpdatePreference() {
        UUID userId = UUID.randomUUID();

        EmailPreference existingPreference = new EmailPreference();
        existingPreference.setUserId(userId);
        existingPreference.setContactEmail("oldEmail@example.com");
        existingPreference.setEnabled(false);
        existingPreference.setCreatedOn(LocalDateTime.now());
        existingPreference.setUpdatedOn(LocalDateTime.now());
        emailPreferenceRepository.save(existingPreference);

        UpsertPreference upsertPreference = new UpsertPreference();
        upsertPreference.setUserId(userId);
        upsertPreference.setContactEmail("user123@example.com");
        upsertPreference.setEnabled(true);

        EmailPreference updatedPreference =
                emailService.upsertPreference(upsertPreference);

        assertNotNull(updatedPreference);
        assertEquals(userId, updatedPreference.getUserId());
        assertEquals("user123@example.com", updatedPreference.getContactEmail());
        assertTrue(updatedPreference.isEnabled());
        assertNotEquals(existingPreference.getUpdatedOn(), updatedPreference.getUpdatedOn());
    }
}


