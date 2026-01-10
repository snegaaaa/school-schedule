package ru.school.schoolschedule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Backward-compatibility route: some templates used to link to /users.
 * Real admin users page is /admin/users.
 */
@Controller
public class UsersRedirectController {

    @GetMapping({"/users", "/users/"})
    public String users() {
        return "redirect:/admin/users";
    }
}
