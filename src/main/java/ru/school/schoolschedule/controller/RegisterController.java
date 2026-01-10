package ru.school.schoolschedule.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.school.schoolschedule.model.AppUser;
import ru.school.schoolschedule.repo.AppUserRepository;

@Controller
public class RegisterController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterController(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("password2") String password2,
            Model model
    ) {
        String u = username == null ? "" : username.trim();

        // чтобы при ошибке логин не пропадал
        model.addAttribute("username", u);

        // валидация
        if (u.isEmpty() || password == null || password.isEmpty() || password2 == null || password2.isEmpty()) {
            model.addAttribute("error", "Заполните все поля");
            return "register";
        }
        if (u.length() < 3 || u.length() > 64) {
            model.addAttribute("error", "Логин должен быть от 3 до 64 символов");
            return "register";
        }
        if (!u.matches("[A-Za-z0-9._-]+")) {
            model.addAttribute("error", "Логин может содержать только латиницу, цифры и символы . _ -");
            return "register";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "Пароль должен быть минимум 6 символов");
            return "register";
        }
        if (!password.equals(password2)) {
            model.addAttribute("error", "Пароли не совпадают");
            return "register";
        }
        if (appUserRepository.existsByUsername(u)) {
            model.addAttribute("error", "Такой логин уже занят");
            return "register";
        }

        // создание пользователя
        AppUser user = new AppUser();
        user.setUsername(u);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("VIEWER");
        user.setEnabled(true);

        appUserRepository.save(user);

        return "redirect:/login?registered";
    }
}
