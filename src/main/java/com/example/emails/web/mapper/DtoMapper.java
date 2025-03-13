package com.example.emails.web.mapper;

import com.example.emails.model.Email;
import com.example.emails.model.EmailPreference;
import com.example.emails.web.dto.EmailResponse;
import com.example.emails.web.dto.PreferenceResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static PreferenceResponse fromEmailPreference(EmailPreference emailPreference) {

        return PreferenceResponse.builder()
                .id(emailPreference.getId())
                .userId(emailPreference.getUserId())
                .enabled(emailPreference.isEnabled())
                .contactEmail(emailPreference.getContactEmail())
                .build();
    }

    public static EmailResponse fromEmail(Email email) {

        return EmailResponse.builder()
                .subject(email.getSubject())
                .createdOn(email.getCreatedOn())
                .status(email.getStatus())
                .build();
    }
}
