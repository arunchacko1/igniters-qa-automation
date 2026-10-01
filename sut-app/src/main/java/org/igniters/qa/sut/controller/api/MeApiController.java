package org.igniters.qa.sut.controller.api;

import java.util.List;
import org.igniters.qa.sut.dto.RegistrationResponse;
import org.igniters.qa.sut.security.AppUserPrincipal;
import org.igniters.qa.sut.service.RegistrationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeApiController {

    private final RegistrationService registrationService;

    public MeApiController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/registrations")
    public List<RegistrationResponse> myRegistrations(@AuthenticationPrincipal AppUserPrincipal principal) {
        return registrationService.myRegistrations(principal.getUser().getId());
    }
}
