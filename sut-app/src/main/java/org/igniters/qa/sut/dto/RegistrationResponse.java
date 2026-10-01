package org.igniters.qa.sut.dto;

import java.time.Instant;
import java.time.LocalDate;

public record RegistrationResponse(
        Long id,
        Long eventId,
        String eventTitle,
        LocalDate eventDate,
        Instant registeredAt) {
}
