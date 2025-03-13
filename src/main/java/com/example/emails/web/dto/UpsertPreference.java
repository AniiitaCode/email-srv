package com.example.emails.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpsertPreference {

    @NotNull
    private UUID userId;

    private boolean enabled;

    @NotBlank
    private String contactEmail;

}
