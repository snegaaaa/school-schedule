package ru.school.schoolschedule.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.school.schoolschedule.model.Room;
import ru.school.schoolschedule.model.Subject;
import ru.school.schoolschedule.model.Teacher;
import ru.school.schoolschedule.service.SetupService;

@Controller
@RequestMapping("/setup")
public class SetupController {

    private final SetupService setupService;

    public SetupController(SetupService setupService) {
        this.setupService = setupService;
    }

    @GetMapping
    public String page(@RequestParam(required = false) String error,
                       @RequestParam(required = false) String section,
                       Model model) {

        model.addAttribute("subjects", setupService.subjects());
        model.addAttribute("teachers", setupService.teachers());
        model.addAttribute("rooms", setupService.rooms());
        model.addAttribute("timeSlots", setupService.timeSlots());

        model.addAttribute("error", error);
        model.addAttribute("section", section);
        return "setup";
    }

    // ----------- SUBJECT -----------
    @PostMapping("/subject")
    public String addSubject(@RequestParam String name) {
        String err = setupService.addSubject(name);
        return (err == null) ? "redirect:/setup" : "redirect:/setup?error=" + err + "&section=subject";
    }

    @GetMapping("/subject/edit")
    public String editSubjectForm(@RequestParam Long id,
                                  @RequestParam(required = false) String error,
                                  Model model) {
        Optional<Subject> subjectOpt = setupService.getSubject(id);
        if (subjectOpt.isEmpty()) return "redirect:/setup?error=notFound&section=subject";

        Subject s = subjectOpt.get();

        model.addAttribute("pageTitle", "Редактирование предмета");
        model.addAttribute("pageSubtitle", "Измените название предмета и сохраните.");
        model.addAttribute("fieldLabel", "Название");
        model.addAttribute("fieldName", "name");
        model.addAttribute("fieldValue", s.getName());
        model.addAttribute("placeholder", "Например: Математика");
        model.addAttribute("hint", "Совет: используйте короткие понятные названия (1–40 символов).");

        model.addAttribute("id", s.getId());
        model.addAttribute("postAction", "/setup/subject/edit");
        model.addAttribute("backUrl", "/setup");

        model.addAttribute("error", error);
        return "setup_edit";
    }

    @PostMapping("/subject/edit")
    public String editSubjectSave(@RequestParam Long id, @RequestParam String name) {
        String err = setupService.editSubject(id, name);
        return (err == null) ? "redirect:/setup" : "redirect:/setup/subject/edit?id=" + id + "&error=" + err;
    }

    @PostMapping("/subject/delete")
    public String deleteSubject(@RequestParam Long id) {
        String err = setupService.deleteSubject(id);
        return (err == null) ? "redirect:/setup" : "redirect:/setup?error=" + err + "&section=subject";
    }

    // ----------- TEACHER -----------
    @PostMapping("/teacher")
    public String addTeacher(@RequestParam String fullName) {
        String err = setupService.addTeacher(fullName);
        return (err == null) ? "redirect:/setup" : "redirect:/setup?error=" + err + "&section=teacher";
    }

    @GetMapping("/teacher/edit")
    public String editTeacherForm(@RequestParam Long id,
                                  @RequestParam(required = false) String error,
                                  Model model) {
        Optional<Teacher> teacherOpt = setupService.getTeacher(id);
        if (teacherOpt.isEmpty()) return "redirect:/setup?error=notFound&section=teacher";

        Teacher t = teacherOpt.get();

        model.addAttribute("pageTitle", "Редактирование учителя");
        model.addAttribute("pageSubtitle", "Измените ФИО и сохраните.");
        model.addAttribute("fieldLabel", "ФИО");
        model.addAttribute("fieldName", "fullName");
        model.addAttribute("fieldValue", t.getFullName());
        model.addAttribute("placeholder", "Например: Иванов И.И.");
        model.addAttribute("hint", "Совет: формат вроде «Фамилия И.О.» выглядит аккуратно и единообразно.");

        model.addAttribute("id", t.getId());
        model.addAttribute("postAction", "/setup/teacher/edit");
        model.addAttribute("backUrl", "/setup");

        model.addAttribute("error", error);
        return "setup_edit";
    }

    @PostMapping("/teacher/edit")
    public String editTeacherSave(@RequestParam Long id, @RequestParam String fullName) {
        String err = setupService.editTeacher(id, fullName);
        return (err == null) ? "redirect:/setup" : "redirect:/setup/teacher/edit?id=" + id + "&error=" + err;
    }

    @PostMapping("/teacher/delete")
    public String deleteTeacher(@RequestParam Long id) {
        String err = setupService.deleteTeacher(id);
        return (err == null) ? "redirect:/setup" : "redirect:/setup?error=" + err + "&section=teacher";
    }

    // ----------- ROOM -----------
    @PostMapping("/room")
    public String addRoom(@RequestParam String number) {
        String err = setupService.addRoom(number);
        return (err == null) ? "redirect:/setup" : "redirect:/setup?error=" + err + "&section=room";
    }

    @GetMapping("/room/edit")
    public String editRoomForm(@RequestParam Long id,
                               @RequestParam(required = false) String error,
                               Model model) {
        Optional<Room> roomOpt = setupService.getRoom(id);
        if (roomOpt.isEmpty()) return "redirect:/setup?error=notFound&section=room";

        Room r = roomOpt.get();

        model.addAttribute("pageTitle", "Редактирование кабинета");
        model.addAttribute("pageSubtitle", "Измените номер кабинета и сохраните.");
        model.addAttribute("fieldLabel", "Номер кабинета");
        model.addAttribute("fieldName", "number");
        model.addAttribute("fieldValue", r.getNumber());
        model.addAttribute("placeholder", "Например: 101");
        model.addAttribute("hint", "Совет: используйте цифры (и при необходимости букву: 101А).");

        model.addAttribute("id", r.getId());
        model.addAttribute("postAction", "/setup/room/edit");
        model.addAttribute("backUrl", "/setup");

        model.addAttribute("error", error);
        return "setup_edit";
    }

    @PostMapping("/room/edit")
    public String editRoomSave(@RequestParam Long id, @RequestParam String number) {
        String err = setupService.editRoom(id, number);
        return (err == null) ? "redirect:/setup" : "redirect:/setup/room/edit?id=" + id + "&error=" + err;
    }

    @PostMapping("/room/delete")
    public String deleteRoom(@RequestParam Long id) {
        String err = setupService.deleteRoom(id);
        return (err == null) ? "redirect:/setup" : "redirect:/setup?error=" + err + "&section=room";
    }

    // ----------- TIME SLOTS (auto) -----------
    @PostMapping("/timeslot/add")
    public String addTimeSlotAuto() {
        String err = setupService.addTimeSlotAuto();
        return (err == null) ? "redirect:/setup" : "redirect:/setup?error=" + err + "&section=timeslot";
    }

    @PostMapping("/timeslot/delete")
    public String deleteTimeSlot(@RequestParam Long id) {
        String err = setupService.deleteTimeSlot(id);
        return (err == null) ? "redirect:/setup" : "redirect:/setup?error=" + err + "&section=timeslot";
    }
}
