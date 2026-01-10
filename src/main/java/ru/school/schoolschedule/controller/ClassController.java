package ru.school.schoolschedule.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.school.schoolschedule.model.SchoolClass;
import ru.school.schoolschedule.service.ClassService;

@Controller
@RequestMapping("/classes")
public class ClassController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String error,
                       @RequestParam(required = false) String value,
                       Model model) {

        model.addAttribute("classes", classService.classes());
        model.addAttribute("error", error);
        model.addAttribute("value", value);
        return "classes";
    }

    @PostMapping
    public String create(@RequestParam("name") String name) {
        var res = classService.create(name);
        if (res.error() == null) return "redirect:/classes";

        if ("empty".equals(res.error())) {
            return "redirect:/classes?error=empty";
        }

        // format/duplicate
        String v = res.value() == null ? "" : res.value();
        return "redirect:/classes?error=" + res.error() + "&value=" + enc(v);
    }

    @GetMapping("/edit")
    public String editForm(@RequestParam Long id,
                           @RequestParam(required = false) String error,
                           Model model) {

        Optional<SchoolClass> cls = classService.get(id);
        if (cls.isEmpty()) {
            return "redirect:/classes?error=notFound";
        }

        model.addAttribute("schoolClass", cls.get());
        model.addAttribute("error", error);
        return "class_edit";
    }

    @PostMapping("/edit")
    public String editSave(@RequestParam Long id,
                           @RequestParam String name) {

        String err = classService.edit(id, name);
        if (err == null) return "redirect:/classes";
        if ("notFound".equals(err)) return "redirect:/classes?error=notFound";
        return "redirect:/classes/edit?id=" + id + "&error=" + err;
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id) {

        String err = classService.delete(id);
        if (err == null) return "redirect:/classes";
        return "redirect:/classes?error=" + err;
    }

    private String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
