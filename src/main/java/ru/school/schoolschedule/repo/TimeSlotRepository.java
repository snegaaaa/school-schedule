package ru.school.schoolschedule.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.school.schoolschedule.model.TimeSlot;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    boolean existsByNumber(int number);

    Optional<TimeSlot> findByNumber(int number);

    List<TimeSlot> findAllByOrderByNumberAsc();

    // Надёжная сортировка по реальному времени
    List<TimeSlot> findAllByOrderByStartTimeAsc();
}
