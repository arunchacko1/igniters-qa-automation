package org.igniters.qa.sut.controller.web;

import jakarta.validation.Valid;
import org.igniters.qa.sut.config.QaDefectsProperties;
import org.igniters.qa.sut.dto.EventFormData;
import org.igniters.qa.sut.dto.EventResponse;
import org.igniters.qa.sut.error.ApiException;
import org.igniters.qa.sut.service.EventService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/** Reachable only by ROLE_ADMIN — enforced in SecurityConfig's /admin/** matcher, not here. */
@Controller
@RequestMapping("/admin/events")
public class AdminEventPageController {

    private final EventService eventService;
    private final QaDefectsProperties qaDefects;

    public AdminEventPageController(EventService eventService, QaDefectsProperties qaDefects) {
        this.eventService = eventService;
        this.qaDefects = qaDefects;
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("eventForm", new EventFormData());
        model.addAttribute("formAction", "/admin/events");
        return "event-form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("eventForm") EventFormData form, BindingResult bindingResult, Model model) {
        model.addAttribute("formAction", "/admin/events");
        if (bindingResult.hasErrors()) {
            return "event-form";
        }
        try {
            // Seeded defect #5: the web form skips the future-date rule when
            // defects are enabled; the API (EventsApiController) never does.
            EventResponse created = eventService.createEvent(form.toRequest(), !qaDefects.isEnabled());
            return "redirect:/events/" + created.id();
        } catch (ApiException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "event-form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("eventForm", EventFormData.from(eventService.getEvent(id)));
        model.addAttribute("formAction", "/admin/events/" + id);
        return "event-form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("eventForm") EventFormData form,
            BindingResult bindingResult,
            Model model) {
        model.addAttribute("formAction", "/admin/events/" + id);
        if (bindingResult.hasErrors()) {
            return "event-form";
        }
        try {
            eventService.updateEvent(id, form.toRequest());
            return "redirect:/events/" + id;
        } catch (ApiException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "event-form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return "redirect:/events";
    }
}
