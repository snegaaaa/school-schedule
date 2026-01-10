package ru.school.schoolschedule.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.school.schoolschedule.model.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
