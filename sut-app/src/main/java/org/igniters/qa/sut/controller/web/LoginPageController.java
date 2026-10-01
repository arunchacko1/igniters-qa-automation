package org.igniters.qa.sut.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginPageController {

    // Spring Security's formLogin posts straight to /login itself — this
    // controller only needs to render the page, not handle the submit.
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
