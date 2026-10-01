package org.igniters.qa.sut.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record EventRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull LocalDate eventDate,
        @NotBlank String location,
        @Positive int capacity) {
}
