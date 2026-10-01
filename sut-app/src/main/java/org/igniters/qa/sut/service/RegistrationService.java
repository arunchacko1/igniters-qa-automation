package org.igniters.qa.sut.service;

import java.util.List;
import org.igniters.qa.sut.config.QaDefectsProperties;
import org.igniters.qa.sut.domain.Event;
import org.igniters.qa.sut.domain.Registration;
import org.igniters.qa.sut.dto.RegistrationResponse;
import org.igniters.qa.sut.error.ConflictException;
import org.igniters.qa.sut.error.NotFoundException;
import org.igniters.qa.sut.repository.EventRepository;
import org.igniters.qa.sut.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final QaDefectsProperties qaDefects;

    public RegistrationService(
            RegistrationRepository registrationRepository,
            EventRepository eventRepository,
            EventService eventService,
            QaDefectsProperties qaDefects) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.eventService = eventService;
        this.qaDefects = qaDefects;
    }

    @Transactional
    public RegistrationResponse register(Long eventId, Long userId) {
        Event event = eventService.requireEvent(eventId);

        if (registrationRepository.findByUserIdAndEventId(userId, eventId).isPresent()) {
            throw new ConflictException("ALREADY_REGISTERED", "You are already registered for this event.");
        }

        long currentCount = registrationRepository.countByEventId(eventId);
        // Seeded defect #1: when enabled, the comparison is ">" instead of ">=",
        // so exactly one extra member can register past a full event.
        boolean full = qaDefects.isEnabled() ? currentCount > event.getCapacity() : currentCount >= event.getCapacity();
        if (full) {
            throw new ConflictException("EVENT_FULL", "This event has no open seats left.");
        }

        Registration saved = registrationRepository.save(new Registration(userId, eventId));
        return toResponse(saved, event);
    }

    @Transactional
    public void cancel(Long eventId, Long userId) {
        Registration registration = registrationRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("REGISTRATION_NOT_FOUND", "You are not registered for this event."));
        registrationRepository.delete(registration);
    }

    public List<RegistrationResponse> myRegistrations(Long userId) {
        return registrationRepository.findByUserId(userId).stream()
                .map(r -> toResponse(r, eventRepository.findById(r.getEventId()).orElse(null)))
                .toList();
    }

    public boolean isRegistered(Long eventId, Long userId) {
        return registrationRepository.findByUserIdAndEventId(userId, eventId).isPresent();
    }

    private RegistrationResponse toResponse(Registration registration, Event event) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getEventId(),
                event != null ? event.getTitle() : null,
                event != null ? event.getEventDate() : null,
                registration.getCreatedAt());
    }
}
