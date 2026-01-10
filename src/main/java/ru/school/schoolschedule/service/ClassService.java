package ru.school.schoolschedule.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.school.schoolschedule.model.SchoolClass;
import ru.school.schoolschedule.repo.LessonRepository;
import ru.school.schoolschedule.repo.SchoolClassRepository;

@Service
public class ClassService {

    private final SchoolClassRepository repo;
    private final LessonRepository lessonRepo;

    // 1-11 + русская заглавная буква (включая Ё)
    private static final String CLASS_REGEX = "^(?:[1-9]|1[01])[А-ЯЁ]$";

    public record CreateResult(String error, String value) {}

    public ClassService(SchoolClassRepository repo, LessonRepository lessonRepo) {
        this.repo = repo;
        this.lessonRepo = lessonRepo;
    }

    @Transactional(readOnly = true)
    public List<SchoolClass> classes() {
        return repo.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Optional<SchoolClass> get(Long id) {
        return repo.findById(id);
    }

    @Transactional
    public CreateResult create(String name) {
        String normalized = normalizeClass(name);

        if (normalized.isEmpty()) return new CreateResult("empty", null);
        if (!normalized.matches(CLASS_REGEX)) return new CreateResult("format", normalized);
        if (repo.existsByNameIgnoreCase(normalized)) return new CreateResult("duplicate", normalized);

        try {
            repo.save(new SchoolClass(normalized));
            return new CreateResult(null, null);
        } catch (DataIntegrityViolationException e) {
            return new CreateResult("duplicate", normalized);
        }
    }

    @Transactional
    public String edit(Long id, String name) {
        Optional<SchoolClass> clsOpt = repo.findById(id);
        if (clsOpt.isEmpty()) return "notFound";

        String normalized = normalizeClass(name);
        if (normalized.isEmpty()) return "empty";
        if (!normalized.matches(CLASS_REGEX)) return "format";

        Optional<SchoolClass> dup = repo.findByNameIgnoreCase(normalized);
        if (dup.isPresent() && !dup.get().getId().equals(id)) return "duplicate";

        SchoolClass cls = clsOpt.get();
        cls.setName(normalized);

        try {
            repo.save(cls);
            return null;
        } catch (DataIntegrityViolationException e) {
            return "duplicate";
        }
    }

    @Transactional
    public String delete(Long id) {
        if (!repo.existsById(id)) return "notFound";
        if (lessonRepo.existsBySchoolClassId(id)) return "inUse";

        try {
            repo.deleteById(id);
            return null;
        } catch (DataIntegrityViolationException e) {
            return "inUse";
        }
    }

    private String normalizeClass(String name) {
        String raw = name == null ? "" : name.trim();
        if (raw.isEmpty()) return "";
        String noSpaces = raw.replaceAll("\\s+", "");
        return noSpaces.toUpperCase(new Locale("ru", "RU"));
    }
}
