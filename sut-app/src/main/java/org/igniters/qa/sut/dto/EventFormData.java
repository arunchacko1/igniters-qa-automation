package org.igniters.qa.sut.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Mutable, JavaBean-style twin of {@link EventRequest} for the Thymeleaf
 * form. {@code th:field} binding needs getters/setters and a no-arg
 * constructor, which a record can't provide — so the web layer gets its
 * own form-backing object instead of reusing the API's record DTO.
 */
public class EventFormData {

    @NotBlank(message = "Title is required.")
    private String title;

    @NotBlank(message = "Description is required.")
    private String description;

    @NotNull(message = "Event date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eventDate;

    @NotBlank(message = "Location is required.")
    private String location;

    @NotNull(message = "Capacity is required.")
    @Positive(message = "Capacity must be greater than zero.")
    private Integer capacity;

    public static EventFormData from(EventResponse event) {
        EventFormData form = new EventFormData();
        form.title = event.title();
        form.description = event.description();
        form.eventDate = event.eventDate();
        form.location = event.location();
        form.capacity = event.capacity();
        return form;
    }

    public EventRequest toRequest() {
        return new EventRequest(title, description, eventDate, location, capacity == null ? 0 : capacity);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
