package com.example.emails.web;

import com.example.emails.model.Email;
import com.example.emails.model.EmailPreference;
import com.example.emails.service.EmailService;
import com.example.emails.web.dto.EmailRequest;
import com.example.emails.web.dto.EmailResponse;
import com.example.emails.web.dto.PreferenceResponse;
import com.example.emails.web.dto.UpsertPreference;
import com.example.emails.web.mapper.DtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emails")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/preferences")
    public ResponseEntity<PreferenceResponse> upsertPreference(@RequestBody UpsertPreference upsertPreference) {

        EmailPreference emailPreference =
                emailService.upsertPreference(upsertPreference);


        PreferenceResponse response = DtoMapper.fromEmailPreference(emailPreference);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/preferences")
    public ResponseEntity<PreferenceResponse> getUserPreference(@RequestParam(name = "userId") UUID userId) {

        EmailPreference emailPreference =
                emailService.getPreferenceByUserId(userId);

        PreferenceResponse response = DtoMapper.fromEmailPreference(emailPreference);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping
    public ResponseEntity<EmailResponse> sendEmail(@RequestBody EmailRequest emailRequest) {

        Email email = emailService.sendEmail(emailRequest);

        EmailResponse response =
                DtoMapper.fromEmail(email);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/preferences")
    public ResponseEntity<PreferenceResponse> changePreference
            (@RequestParam(name = "userId") UUID userId,
             @RequestParam(name = "enabled") boolean enabled) {

        EmailPreference emailPreference = emailService.changePreference(userId, enabled);

        PreferenceResponse response = DtoMapper.fromEmailPreference(emailPreference);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
