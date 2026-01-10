package ru.school.schoolschedule.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.school.schoolschedule.model.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    boolean existsByFullNameIgnoreCase(String fullName);
    Optional<Teacher> findByFullNameIgnoreCase(String fullName);

    // сортировка для UI
    List<Teacher> findAllByOrderByFullNameAsc();
}
