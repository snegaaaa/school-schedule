package ru.school.schoolschedule.config;

import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import ru.school.schoolschedule.model.TimeSlot;
import ru.school.schoolschedule.repo.LessonRepository;
import ru.school.schoolschedule.repo.TimeSlotRepository;

@Component
public class DataInitializer {

    private static final LocalTime DAY_START = LocalTime.of(8, 30);
    private static final int LESSON_MIN = 45;
    private static final int BREAK_MIN = 10;

    // фиксируем строго 16 слотов
    private static final int MAX_SLOTS = 16;

    private final TimeSlotRepository timeSlotRepo;
    private final LessonRepository lessonRepo;

    public DataInitializer(TimeSlotRepository timeSlotRepo, LessonRepository lessonRepo) {
        this.timeSlotRepo = timeSlotRepo;
        this.lessonRepo = lessonRepo;
    }

    @PostConstruct
    public void init() {
        normalizeTimeSlots();
    }

    @Transactional
    public void normalizeTimeSlots() {

        // 1) гарантируем, что 1..16 существуют и имеют правильное время
        for (int n = 1; n <= MAX_SLOTS; n++) {
            var start = calcStart(n);
            var end = start.plusMinutes(LESSON_MIN);

            var existing = timeSlotRepo.findByNumber(n);
            if (existing.isPresent()) {
                TimeSlot ts = existing.get();
                boolean changed = false;

                if (!ts.getStartTime().equals(start)) {
                    ts.setStartTime(start);
                    changed = true;
                }
                if (!ts.getEndTime().equals(end)) {
                    ts.setEndTime(end);
                    changed = true;
                }
                if (changed) timeSlotRepo.save(ts);
            } else {
                timeSlotRepo.save(new TimeSlot(n, start, end));
            }
        }

        // 2) удаляем лишние слоты (номер <1 или >16), но только если они нигде не используются
        List<TimeSlot> all = timeSlotRepo.findAll();
        for (TimeSlot ts : all) {
            if (ts.getNumber() > MAX_SLOTS || ts.getNumber() < 1) {
                if (!lessonRepo.existsByTimeSlotId(ts.getId())) {
                    timeSlotRepo.delete(ts);
                }
            }
        }
    }

    private LocalTime calcStart(int number) {
        long shift = (long) (number - 1) * (LESSON_MIN + BREAK_MIN);
        return DAY_START.plusMinutes(shift);
    }
}
