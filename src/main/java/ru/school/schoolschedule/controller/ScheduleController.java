package ru.school.schoolschedule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.school.schoolschedule.model.Lesson;
import ru.school.schoolschedule.model.WeekDay;
import ru.school.schoolschedule.service.ScheduleService;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public String page(@RequestParam(required = false) Long classId,
                       @RequestParam(required = false) WeekDay day,
                       @RequestParam(required = false) String error,
                       Model model) {

        var classes = scheduleService.getClasses();
        var timeSlots = scheduleService.getTimeSlots();

        model.addAttribute("classes", classes);
        model.addAttribute("days", WeekDay.values());
        model.addAttribute("timeSlots", timeSlots);
        model.addAttribute("error", error);

        if (classes.isEmpty()) {
            model.addAttribute("fatal", "noClasses");
            return "schedule";
        }
        if (timeSlots.isEmpty()) {
            model.addAttribute("fatal", "noTimeSlots");
            return "schedule";
        }

        // если передали classId, которого нет — не падаем
        if (classId != null) {
            boolean exists = false;
            for (var c : classes) {
                if (c.getId().equals(classId)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                model.addAttribute("error", "notFound");
                classId = null;
                day = null;
            }
        }

        if (classId != null && day != null) {
            model.addAttribute("selectedClassId", classId);
            model.addAttribute("selectedDay", day);

            model.addAttribute("lessons", scheduleService.getLessonsForDay(classId, day));

            var subjects = scheduleService.getSubjects();
            var teachers = scheduleService.getTeachers();
            var rooms = scheduleService.getRooms();

            model.addAttribute("subjects", subjects);
            model.addAttribute("teachers", teachers);
            model.addAttribute("rooms", rooms);

            if (subjects.isEmpty() || teachers.isEmpty() || rooms.isEmpty()) {
                model.addAttribute("fatal", "needSetup");
            }
        }

        return "schedule";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long classId,
                      @RequestParam WeekDay day,
                      @RequestParam Long timeSlotId,
                      @RequestParam Long subjectId,
                      @RequestParam Long teacherId,
                      @RequestParam Long roomId) {

        String err = scheduleService.addLesson(classId, day, timeSlotId, subjectId, teacherId, roomId);
        if (err != null) {
            return "redirect:/schedule?classId=" + classId + "&day=" + day.name() + "&error=" + err;
        }

        return "redirect:/schedule?classId=" + classId + "&day=" + day.name();
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id,
                         @RequestParam Long classId,
                         @RequestParam WeekDay day) {

        String err = scheduleService.deleteLesson(id);
        if (err != null) {
            return "redirect:/schedule?classId=" + classId + "&day=" + day.name() + "&error=" + err;
        }

        return "redirect:/schedule?classId=" + classId + "&day=" + day.name();
    }

    @GetMapping("/week")
    public String week(@RequestParam(required = false) Long classId,
                       @RequestParam(required = false) String error,
                       Model model) {

        var classes = scheduleService.getClasses();
        model.addAttribute("classes", classes);
        model.addAttribute("days", WeekDay.values());
        model.addAttribute("error", error);

        if (classes.isEmpty()) {
            model.addAttribute("fatal", "noClasses");
            return "schedule_week";
        }

        Long selected = (classId != null) ? classId : classes.get(0).getId();

        boolean exists = false;
        for (var c : classes) {
            if (c.getId().equals(selected)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            selected = classes.get(0).getId();
            model.addAttribute("error", "notFound");
        }

        model.addAttribute("selectedClassId", selected);
        model.addAttribute("byDay", scheduleService.getWeekByDay(selected));
        return "schedule_week";
    }

    @PostMapping("/week/delete")
    public String deleteFromWeek(@RequestParam Long id,
                                 @RequestParam Long classId) {

        String err = scheduleService.deleteLesson(id);
        if (err != null) {
            return "redirect:/schedule/week?classId=" + classId + "&error=" + err;
        }
        return "redirect:/schedule/week?classId=" + classId;
    }

    // -------- РЕДАКТИРОВАНИЕ --------

    @GetMapping("/edit")
    public String editForm(@RequestParam Long id,
                           @RequestParam Long classId,
                           @RequestParam WeekDay day,
                           @RequestParam(required = false) String error,
                           Model model) {

        var lessonOpt = scheduleService.findLesson(id);
        if (lessonOpt.isEmpty()) {
            return "redirect:/schedule?classId=" + classId + "&day=" + day.name() + "&error=notFound";
        }

        Lesson lesson = lessonOpt.get();
        if (!lesson.getSchoolClass().getId().equals(classId) || lesson.getWeekDay() != day) {
            return "redirect:/schedule?classId=" + lesson.getSchoolClass().getId()
                    + "&day=" + lesson.getWeekDay().name() + "&error=forbiddenEdit";
        }

        var subjects = scheduleService.getSubjects();
        var teachers = scheduleService.getTeachers();
        var rooms = scheduleService.getRooms();
        var timeSlots = scheduleService.getTimeSlots();

        if (subjects.isEmpty() || teachers.isEmpty() || rooms.isEmpty() || timeSlots.isEmpty()) {
            return "redirect:/schedule?classId=" + classId + "&day=" + day.name() + "&error=needSetup";
        }

        model.addAttribute("lesson", lesson);
        model.addAttribute("classId", classId);
        model.addAttribute("day", day);
        model.addAttribute("subjects", subjects);
        model.addAttribute("teachers", teachers);
        model.addAttribute("rooms", rooms);
        model.addAttribute("timeSlots", timeSlots);
        model.addAttribute("error", error);

        return "lesson_edit";
    }

    @PostMapping("/edit")
    public String editSave(@RequestParam Long id,
                           @RequestParam Long classId,
                           @RequestParam WeekDay day,
                           @RequestParam Long timeSlotId,
                           @RequestParam Long subjectId,
                           @RequestParam Long teacherId,
                           @RequestParam Long roomId) {

        var lessonOpt = scheduleService.findLesson(id);
        if (lessonOpt.isEmpty()) {
            return "redirect:/schedule?classId=" + classId + "&day=" + day.name() + "&error=notFound";
        }

        Lesson lesson = lessonOpt.get();
        if (!lesson.getSchoolClass().getId().equals(classId) || lesson.getWeekDay() != day) {
            return "redirect:/schedule?classId=" + lesson.getSchoolClass().getId()
                    + "&day=" + lesson.getWeekDay().name() + "&error=forbiddenEdit";
        }

        String err = scheduleService.editLesson(id, day, timeSlotId, subjectId, teacherId, roomId);
        if (err != null) {
            return "redirect:/schedule/edit?id=" + id + "&classId=" + classId + "&day=" + day.name() + "&error=" + err;
        }

        return "redirect:/schedule?classId=" + classId + "&day=" + day.name();
    }
}
