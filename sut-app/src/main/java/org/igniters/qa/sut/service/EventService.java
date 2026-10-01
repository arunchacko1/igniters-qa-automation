package org.igniters.qa.sut.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.igniters.qa.sut.config.QaDefectsProperties;
import org.igniters.qa.sut.domain.Event;
import org.igniters.qa.sut.dto.EventRequest;
import org.igniters.qa.sut.dto.EventResponse;
import org.igniters.qa.sut.error.BadRequestException;
import org.igniters.qa.sut.error.NotFoundException;
import org.igniters.qa.sut.repository.EventRepository;
import org.igniters.qa.sut.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final QaDefectsProperties qaDefects;

    public EventService(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository,
            QaDefectsProperties qaDefects) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.qaDefects = qaDefects;
    }

    public List<EventResponse> listEvents(String q, LocalDate date) {
        return eventRepository.findAll().stream()
                .filter(event -> matchesTitle(event, q))
                .filter(event -> date == null || event.getEventDate().equals(date))
                .sorted((a, b) -> a.getEventDate().compareTo(b.getEventDate()))
                .map(event -> toResponse(event))
                .toList();
    }

    // Seeded defect #2: when QA_DEFECTS_ENABLED=true, search becomes
    // case-sensitive because it skips lower-casing both sides.
    private boolean matchesTitle(Event event, String q) {
        if (q == null || q.isBlank()) {
            return true;
        }
        if (qaDefects.isEnabled()) {
            return event.getTitle().contains(q);
        }
        return event.getTitle().toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT));
    }

    public EventResponse getEvent(Long id) {
        return toResponse(requireEvent(id));
    }

    /** Used by {@link org.igniters.qa.sut.service.RegistrationService} too. */
    public Event requireEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("EVENT_NOT_FOUND", "No event with id " + id));
    }

    public EventResponse createEvent(EventRequest request, boolean enforceDateRule) {
        if (enforceDateRule) {
            requireFutureDate(request.eventDate());
        }
        Event event = new Event(
                request.title(), request.description(), request.eventDate(), request.location(), request.capacity());
        eventRepository.save(event);
        return toResponse(event);
    }

    public EventResponse updateEvent(Long id, EventRequest request) {
        return eventRepository.findById(id)
                .map(event -> {
                    requireFutureDate(request.eventDate());
                    event.setTitle(request.title());
                    event.setDescription(request.description());
                    event.setEventDate(request.eventDate());
                    event.setLocation(request.location());
                    event.setCapacity(request.capacity());
                    eventRepository.save(event);
                    return toResponse(event);
                })
                // Seeded defect #4: when enabled, a PUT for a nonexistent ID fakes a
                // 200 response instead of a real 404 — nothing is actually persisted.
                .orElseGet(() -> {
                    if (qaDefects.isEnabled()) {
                        return new EventResponse(
                                id,
                                request.title(),
                                request.description(),
                                request.eventDate(),
                                request.location(),
                                request.capacity(),
                                request.capacity(),
                                null);
                    }
                    throw new NotFoundException("EVENT_NOT_FOUND", "No event with id " + id);
                });
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = requireEvent(id);
        // Seeded defect #3: when enabled, this cleanup step is skipped, which
        // leaves orphaned rows in `registrations` once the event row is gone.
        if (!qaDefects.isEnabled()) {
            registrationRepository.deleteByEventId(id);
        }
        eventRepository.delete(event);
    }

    private void requireFutureDate(LocalDate date) {
        if (!date.isAfter(LocalDate.now())) {
            throw new BadRequestException("EVENT_DATE_IN_PAST", "Event date must be in the future.");
        }
    }

    private EventResponse toResponse(Event event) {
        long count = registrationRepository.countByEventId(event.getId());
        return EventResponse.from(event, count);
    }
}
