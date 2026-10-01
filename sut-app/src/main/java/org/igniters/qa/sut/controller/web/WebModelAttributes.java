package org.igniters.qa.sut.controller.web;

import org.igniters.qa.sut.domain.Role;
import org.igniters.qa.sut.security.AppUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Makes "is the current user an admin" available to every Thymeleaf page without repeating it per controller. */
@ControllerAdvice(basePackages = "org.igniters.qa.sut.controller.web")
public class WebModelAttributes {

    @ModelAttribute
    public void addCommonAttributes(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        if (principal != null) {
            model.addAttribute("isAdmin", principal.getUser().getRole() == Role.ADMIN);
            model.addAttribute("currentUserName", principal.getUser().getName());
        }
    }
}
