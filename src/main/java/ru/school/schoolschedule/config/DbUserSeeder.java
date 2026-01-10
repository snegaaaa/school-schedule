package ru.school.schoolschedule.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ru.school.schoolschedule.model.AppUser;
import ru.school.schoolschedule.repo.AppUserRepository;

@Component
public class DbUserSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DbUserSeeder(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedIfMissing("admin", "admin123", "ADMIN");
        seedIfMissing("viewer", "viewer123", "VIEWER");
    }

    private void seedIfMissing(String username, String rawPassword, String role) {
        if (appUserRepository.existsByUsername(username)) {
            return;
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        // Кодируем через общий PasswordEncoder (поддерживает {bcrypt} и совместим с legacy-форматом)
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);      // "ADMIN" / "VIEWER"
        user.setEnabled(true);

        appUserRepository.save(user);
    }
}
