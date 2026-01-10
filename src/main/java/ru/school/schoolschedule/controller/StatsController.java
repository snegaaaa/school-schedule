package ru.school.schoolschedule.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ru.school.schoolschedule.model.WeekDay;
import ru.school.schoolschedule.repo.AppUserRepository;
import ru.school.schoolschedule.repo.LessonRepository;
import ru.school.schoolschedule.repo.RoomRepository;
import ru.school.schoolschedule.repo.SchoolClassRepository;
import ru.school.schoolschedule.repo.SubjectRepository;
import ru.school.schoolschedule.repo.TeacherRepository;
import ru.school.schoolschedule.repo.TimeSlotRepository;

@Controller
public class StatsController {

    private final SchoolClassRepository classRepo;
    private final SubjectRepository subjectRepo;
    private final TeacherRepository teacherRepo;
    private final RoomRepository roomRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final LessonRepository lessonRepo;
    private final AppUserRepository userRepo;

    public StatsController(
            SchoolClassRepository classRepo,
            SubjectRepository subjectRepo,
            TeacherRepository teacherRepo,
            RoomRepository roomRepo,
            TimeSlotRepository timeSlotRepo,
            LessonRepository lessonRepo,
            AppUserRepository userRepo
    ) {
        this.classRepo = classRepo;
        this.subjectRepo = subjectRepo;
        this.teacherRepo = teacherRepo;
        this.roomRepo = roomRepo;
        this.timeSlotRepo = timeSlotRepo;
        this.lessonRepo = lessonRepo;
        this.userRepo = userRepo;
    }

    public record DayBar(String title, long count, int percent) {}
    public record TopItem(String name, long count) {}

    @GetMapping("/stats")
    public String stats(Model model) {

        long usersCount = userRepo.count();
        long classesCount = classRepo.count();
        long subjectsCount = subjectRepo.count();
        long teachersCount = teacherRepo.count();
        long roomsCount = roomRepo.count();
        long timeSlotsCount = timeSlotRepo.count();
        long lessonsCount = lessonRepo.count();

        TopItem topTeacher = topFrom(lessonRepo.topTeachers(PageRequest.of(0, 1)));
        TopItem topRoom = topFrom(lessonRepo.topRooms(PageRequest.of(0, 1)));
        TopItem topSubject = topFrom(lessonRepo.topSubjects(PageRequest.of(0, 1)));

        Map<WeekDay, Long> dayCounts = new EnumMap<>(WeekDay.class);
        for (WeekDay d : WeekDay.values()) {
            dayCounts.put(d, 0L);
        }

        for (Object[] row : lessonRepo.countLessonsByWeekDay()) {
            if (row == null || row.length < 2) continue;

            WeekDay day = (WeekDay) row[0];
            long cnt = toLongSafe(row[1]);

            if (day != null) {
                dayCounts.put(day, cnt);
            }
        }

        long max = dayCounts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        List<DayBar> bars = new ArrayList<>();
        for (WeekDay d : WeekDay.values()) {
            long c = dayCounts.getOrDefault(d, 0L);
            int percent = max == 0 ? 0 : (int) Math.round((c * 100.0) / max);
            bars.add(new DayBar(d.getTitle(), c, percent));
        }

        model.addAttribute("usersCount", usersCount);
        model.addAttribute("classesCount", classesCount);
        model.addAttribute("subjectsCount", subjectsCount);
        model.addAttribute("teachersCount", teachersCount);
        model.addAttribute("roomsCount", roomsCount);
        model.addAttribute("timeSlotsCount", timeSlotsCount);
        model.addAttribute("lessonsCount", lessonsCount);

        model.addAttribute("topTeacher", topTeacher);
        model.addAttribute("topRoom", topRoom);
        model.addAttribute("topSubject", topSubject);

        model.addAttribute("bars", bars);
        model.addAttribute("updatedAt", LocalDateTime.now());

        return "stats";
    }

    private TopItem topFrom(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) return new TopItem("—", 0);

        Object[] r = rows.get(0);
        if (r == null || r.length < 2) return new TopItem("—", 0);

        String name = (r[0] == null) ? "—" : String.valueOf(r[0]);
        long count = toLongSafe(r[1]);

        return new TopItem(name, count);
    }

    private long toLongSafe(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
