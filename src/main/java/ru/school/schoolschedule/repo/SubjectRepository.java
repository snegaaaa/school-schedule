package ru.school.schoolschedule.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.school.schoolschedule.model.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Subject> findByNameIgnoreCase(String name);

    // сортировка для UI
    List<Subject> findAllByOrderByNameAsc();
}
