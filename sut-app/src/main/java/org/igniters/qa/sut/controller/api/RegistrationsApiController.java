package org.igniters.qa.sut.controller.api;

import org.igniters.qa.sut.dto.RegistrationResponse;
import org.igniters.qa.sut.security.AppUserPrincipal;
import org.igniters.qa.sut.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events/{eventId}/registrations")
public class RegistrationsApiController {

    private final RegistrationService registrationService;

    public RegistrationsApiController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    public ResponseEntity<RegistrationResponse> register(
            @PathVariable Long eventId, @AuthenticationPrincipal AppUserPrincipal principal) {
        RegistrationResponse response = registrationService.register(eventId, principal.getUser().getId());
        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> cancel(
            @PathVariable Long eventId, @AuthenticationPrincipal AppUserPrincipal principal) {
        registrationService.cancel(eventId, principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
