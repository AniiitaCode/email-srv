package com.example.emails.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class PreferenceResponse {

    private UUID id;

    private UUID userId;

    private boolean enabled;

    private String contactEmail;

}
