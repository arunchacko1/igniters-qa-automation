package org.igniters.qa.sut.controller.web;

import jakarta.servlet.http.HttpServletResponse;
import org.igniters.qa.sut.error.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Without this, an unhandled {@link NotFoundException} on a page controller
 * would fall through to Spring Boot's generic Whitelabel error page as a
 * 500 — this turns it into a real 404 (see TC-025).
 */
@ControllerAdvice(basePackages = "org.igniters.qa.sut.controller.web")
public class WebExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFound(HttpServletResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        return "error-404";
    }
}
