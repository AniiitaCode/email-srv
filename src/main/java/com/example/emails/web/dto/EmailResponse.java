package com.example.emails.web.dto;

import com.example.emails.model.EmailStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class EmailResponse {

    private String subject;

    private LocalDateTime createdOn;

    private EmailStatus status;

}
