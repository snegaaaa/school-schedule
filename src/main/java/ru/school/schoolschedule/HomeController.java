package ru.school.schoolschedule;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ru.school.schoolschedule.repo.LessonRepository;
import ru.school.schoolschedule.repo.RoomRepository;
import ru.school.schoolschedule.repo.SchoolClassRepository;
import ru.school.schoolschedule.repo.SubjectRepository;
import ru.school.schoolschedule.repo.TeacherRepository;
import ru.school.schoolschedule.repo.TimeSlotRepository;

@Controller
public class HomeController {

    private final SchoolClassRepository classRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final SubjectRepository subjectRepo;
    private final TeacherRepository teacherRepo;
    private final RoomRepository roomRepo;
    private final LessonRepository lessonRepo;

    public HomeController(SchoolClassRepository classRepo,
                          TimeSlotRepository timeSlotRepo,
                          SubjectRepository subjectRepo,
                          TeacherRepository teacherRepo,
                          RoomRepository roomRepo,
                          LessonRepository lessonRepo) {
        this.classRepo = classRepo;
        this.timeSlotRepo = timeSlotRepo;
        this.subjectRepo = subjectRepo;
        this.teacherRepo = teacherRepo;
        this.roomRepo = roomRepo;
        this.lessonRepo = lessonRepo;
    }

    public record HealthItem(
            String status,     // ok / warn / bad
            String title,
            String value,
            String message,
            String actionUrl,  // может быть null
            String actionText  // может быть null
    ) {}

    @GetMapping("/")
    public String index(Model model) {

        long classes = classRepo.count();
        long timeSlots = timeSlotRepo.count();
        long subjects = subjectRepo.count();
        long teachers = teacherRepo.count();
        long rooms = roomRepo.count();
        long lessons = lessonRepo.count();

        boolean readyCore = classes > 0 && timeSlots > 0;
        boolean readyRefs = subjects > 0 && teachers > 0 && rooms > 0;
        boolean readyAll = readyCore && readyRefs;

        List<HealthItem> health = new ArrayList<>();

        // Классы
        health.add(new HealthItem(
                classes > 0 ? "ok" : "bad",
                "Классы",
                String.valueOf(classes),
                classes > 0 ? "Ок: классы добавлены." : "Нужно добавить хотя бы один класс.",
                classes > 0 ? null : "/classes",
                classes > 0 ? null : "Перейти"
        ));

        // Слоты времени
        health.add(new HealthItem(
                timeSlots > 0 ? "ok" : "bad",
                "Слоты времени",
                String.valueOf(timeSlots),
                timeSlots > 0 ? "Ок: слоты времени настроены." : "Нужно добавить слоты времени (№, начало, конец).",
                timeSlots > 0 ? null : "/setup",
                timeSlots > 0 ? null : "Перейти"
        ));

        // Справочники
        if (subjects == 0 || teachers == 0 || rooms == 0) {
            health.add(new HealthItem(
                    "warn",
                    "Справочники",
                    (subjects + teachers + rooms) + " (предм./учит./каб.)",
                    "Для добавления уроков нужны предметы, учителя и кабинеты.",
                    "/setup",
                    "Перейти"
            ));
        } else {
            health.add(new HealthItem(
                    "ok",
                    "Справочники",
                    (subjects + teachers + rooms) + " (предм./учит./каб.)",
                    "Ок: предметы, учителя и кабинеты заполнены.",
                    null,
                    null
            ));
        }

        // Уроки (не критично, но показатель готовности)
        health.add(new HealthItem(
                lessons > 0 ? "ok" : (readyAll ? "warn" : "warn"),
                "Уроки в расписании",
                String.valueOf(lessons),
                lessons > 0 ? "Ок: расписание уже заполнено." : (readyAll ? "Можно начинать заполнять расписание." : "Сначала заполните базовые данные выше."),
                (lessons > 0 || !readyAll) ? null : "/schedule",
                (lessons > 0 || !readyAll) ? null : "Перейти"
        ));

        model.addAttribute("health", health);
        model.addAttribute("readyAll", readyAll);
        model.addAttribute("readyCore", readyCore);

        return "index";
    }
}
