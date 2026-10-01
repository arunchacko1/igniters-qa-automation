package org.igniters.qa.sut.controller.web;

import java.time.LocalDate;
import org.igniters.qa.sut.dto.EventResponse;
import org.igniters.qa.sut.error.ApiException;
import org.igniters.qa.sut.security.AppUserPrincipal;
import org.igniters.qa.sut.service.EventService;
import org.igniters.qa.sut.service.RegistrationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EventPageController {

    private final EventService eventService;
    private final RegistrationService registrationService;

    public EventPageController(EventService eventService, RegistrationService registrationService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/events";
    }

    @GetMapping("/events")
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        model.addAttribute("events", eventService.listEvents(q, date));
        model.addAttribute("q", q);
        model.addAttribute("date", date);
        return "events";
    }

    @GetMapping("/events/{id}")
    public String detail(
            @PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        EventResponse event = eventService.getEvent(id);
        model.addAttribute("event", event);
        model.addAttribute("isRegistered", registrationService.isRegistered(id, principal.getUser().getId()));
        return "event-detail";
    }

    @PostMapping("/events/{id}/register")
    public String register(
            @PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal, RedirectAttributes redirect) {
        try {
            registrationService.register(id, principal.getUser().getId());
        } catch (ApiException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/events/" + id;
    }

    @PostMapping("/events/{id}/cancel")
    public String cancel(
            @PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal, RedirectAttributes redirect) {
        try {
            registrationService.cancel(id, principal.getUser().getId());
        } catch (ApiException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/events/" + id;
    }
}
