package ru.school.schoolschedule.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.school.schoolschedule.model.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByNumberIgnoreCase(String number);
    Optional<Room> findByNumberIgnoreCase(String number);

    // сортировка для UI
    List<Room> findAllByOrderByNumberAsc();
}
