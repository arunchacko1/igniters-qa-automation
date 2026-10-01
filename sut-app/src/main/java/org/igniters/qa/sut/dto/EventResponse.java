package org.igniters.qa.sut.dto;

import java.time.Instant;
import java.time.LocalDate;
import org.igniters.qa.sut.domain.Event;

public record EventResponse(
        Long id,
        String title,
        String description,
        LocalDate eventDate,
        String location,
        int capacity,
        int remainingCapacity,
        Instant createdAt) {

    public static EventResponse from(Event event, long registrationCount) {
        int remaining = (int) Math.max(0, event.getCapacity() - registrationCount);
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getEventDate(),
                event.getLocation(),
                event.getCapacity(),
                remaining,
                event.getCreatedAt());
    }
}
