package ru.school.schoolschedule.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.school.schoolschedule.model.AppUser;
import ru.school.schoolschedule.repo.AppUserRepository;

@Controller
@RequestMapping("/admin/users")
public class AdminUsersController {

    private final AppUserRepository repo;

    public AdminUsersController(AppUserRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String page(
            Model model,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "ok", required = false) String ok
    ) {
        List<AppUser> users = repo.findAll(Sort.by(Sort.Direction.ASC, "username"));
        model.addAttribute("users", users);
        model.addAttribute("error", error);
        model.addAttribute("ok", ok);
        return "admin_users";
    }

    @PostMapping("/update")
    public String update(
            @RequestParam("id") Long id,
            @RequestParam("role") String role,
            @RequestParam(value = "enabled", required = false) String enabled,
            Principal principal
    ) {

        String newRole = role == null ? "" : role.trim().toUpperCase();
        boolean newEnabled = enabled != null; // checkbox

        if (!("ADMIN".equals(newRole) || "VIEWER".equals(newRole))) {
            return "redirect:/admin/users?error=role";
        }

        AppUser u = repo.findById(id).orElse(null);
        if (u == null) {
            return "redirect:/admin/users?error=notFound";
        }

        // Защита от самоблокировки
        String current = principal == null ? null : principal.getName();
        if (current != null && current.equals(u.getUsername())) {
            if (!newEnabled) return "redirect:/admin/users?error=selfDisable";
            if (!"ADMIN".equals(newRole)) return "redirect:/admin/users?error=selfDemote";
        }

        u.setRole(newRole);
        u.setEnabled(newEnabled);
        repo.save(u);

        return "redirect:/admin/users?ok=1";
    }
}