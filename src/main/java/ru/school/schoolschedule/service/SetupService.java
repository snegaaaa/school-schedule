package ru.school.schoolschedule.service;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.school.schoolschedule.model.Room;
import ru.school.schoolschedule.model.Subject;
import ru.school.schoolschedule.model.Teacher;
import ru.school.schoolschedule.model.TimeSlot;
import ru.school.schoolschedule.repo.LessonRepository;
import ru.school.schoolschedule.repo.RoomRepository;
import ru.school.schoolschedule.repo.SubjectRepository;
import ru.school.schoolschedule.repo.TeacherRepository;
import ru.school.schoolschedule.repo.TimeSlotRepository;

@Service
public class SetupService {

    private final SubjectRepository subjectRepo;
    private final TeacherRepository teacherRepo;
    private final RoomRepository roomRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final LessonRepository lessonRepo;

    private static final String SUBJECT_REGEX = "^[А-ЯЁа-яё0-9 .\\-]{2,50}$";
    private static final String TEACHER_REGEX = "^[А-ЯЁа-яё .\\-]{5,120}$";
    private static final String ROOM_REGEX = "^[0-9]{1,4}[А-ЯЁA-Z]?$";

    public SetupService(SubjectRepository subjectRepo,
                        TeacherRepository teacherRepo,
                        RoomRepository roomRepo,
                        TimeSlotRepository timeSlotRepo,
                        LessonRepository lessonRepo) {
        this.subjectRepo = subjectRepo;
        this.teacherRepo = teacherRepo;
        this.roomRepo = roomRepo;
        this.timeSlotRepo = timeSlotRepo;
        this.lessonRepo = lessonRepo;
    }

    // ---------- PAGE DATA ----------
    @Transactional(readOnly = true)
    public List<Subject> subjects() {
        return subjectRepo.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Teacher> teachers() {
        return teacherRepo.findAllByOrderByFullNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Room> rooms() {
        return roomRepo.findAllByOrderByNumberAsc();
    }

    @Transactional(readOnly = true)
    public List<TimeSlot> timeSlots() {
        // оставляем как было: по реальному времени (надёжно)
        return timeSlotRepo.findAllByOrderByStartTimeAsc();
    }

    // ---------- SUBJECT ----------
    @Transactional
    public String addSubject(String name) {
        String v = normalize(name);
        if (v.isEmpty()) return "empty";
        if (!v.matches(SUBJECT_REGEX)) return "format";
        if (subjectRepo.existsByNameIgnoreCase(v)) return "duplicate";

        try {
            subjectRepo.save(new Subject(v));
            return null;
        } catch (DataIntegrityViolationException e) {
            return "duplicate";
        }
    }

    @Transactional(readOnly = true)
    public Optional<Subject> getSubject(Long id) {
        return subjectRepo.findById(id);
    }

    @Transactional
    public String editSubject(Long id, String name) {
        Optional<Subject> subjectOpt = subjectRepo.findById(id);
        if (subjectOpt.isEmpty()) return "notFound";

        String v = normalize(name);
        if (v.isEmpty()) return "empty";
        if (!v.matches(SUBJECT_REGEX)) return "format";

        var dup = subjectRepo.findByNameIgnoreCase(v);
        if (dup.isPresent() && !dup.get().getId().equals(id)) return "duplicate";

        Subject s = subjectOpt.get();
        s.setName(v);

        try {
            subjectRepo.save(s);
            return null;
        } catch (DataIntegrityViolationException e) {
            return "duplicate";
        }
    }

    @Transactional
    public String deleteSubject(Long id) {
        if (!subjectRepo.existsById(id)) return "notFound";
        if (lessonRepo.existsBySubjectId(id)) return "inUse";

        try {
            subjectRepo.deleteById(id);
            return null;
        } catch (DataIntegrityViolationException e) {
            return "inUse";
        }
    }

    // ---------- TEACHER ----------
    @Transactional
    public String addTeacher(String fullName) {
        String v = normalize(fullName);
        if (v.isEmpty()) return "empty";
        if (!v.matches(TEACHER_REGEX)) return "format";
        if (teacherRepo.existsByFullNameIgnoreCase(v)) return "duplicate";

        try {
            teacherRepo.save(new Teacher(v));
            return null;
        } catch (DataIntegrityViolationException e) {
            return "duplicate";
        }
    }

    @Transactional(readOnly = true)
    public Optional<Teacher> getTeacher(Long id) {
        return teacherRepo.findById(id);
    }

    @Transactional
    public String editTeacher(Long id, String fullName) {
        Optional<Teacher> teacherOpt = teacherRepo.findById(id);
        if (teacherOpt.isEmpty()) return "notFound";

        String v = normalize(fullName);
        if (v.isEmpty()) return "empty";
        if (!v.matches(TEACHER_REGEX)) return "format";

        var dup = teacherRepo.findByFullNameIgnoreCase(v);
        if (dup.isPresent() && !dup.get().getId().equals(id)) return "duplicate";

        Teacher t = teacherOpt.get();
        t.setFullName(v);

        try {
            teacherRepo.save(t);
            return null;
        } catch (DataIntegrityViolationException e) {
            return "duplicate";
        }
    }

    @Transactional
    public String deleteTeacher(Long id) {
        if (!teacherRepo.existsById(id)) return "notFound";
        if (lessonRepo.existsByTeacherId(id)) return "inUse";

        try {
            teacherRepo.deleteById(id);
            return null;
        } catch (DataIntegrityViolationException e) {
            return "inUse";
        }
    }

    // ---------- ROOM ----------
    @Transactional
    public String addRoom(String number) {
        String v = normalize(number);
        if (v.isEmpty()) return "empty";
        if (!v.matches(ROOM_REGEX)) return "format";
        if (roomRepo.existsByNumberIgnoreCase(v)) return "duplicate";

        try {
            roomRepo.save(new Room(v));
            return null;
        } catch (DataIntegrityViolationException e) {
            return "duplicate";
        }
    }

    @Transactional(readOnly = true)
    public Optional<Room> getRoom(Long id) {
        return roomRepo.findById(id);
    }

    @Transactional
    public String editRoom(Long id, String number) {
        Optional<Room> roomOpt = roomRepo.findById(id);
        if (roomOpt.isEmpty()) return "notFound";

        String v = normalize(number);
        if (v.isEmpty()) return "empty";
        if (!v.matches(ROOM_REGEX)) return "format";

        var dup = roomRepo.findByNumberIgnoreCase(v);
        if (dup.isPresent() && !dup.get().getId().equals(id)) return "duplicate";

        Room r = roomOpt.get();
        r.setNumber(v);

        try {
            roomRepo.save(r);
            return null;
        } catch (DataIntegrityViolationException e) {
            return "duplicate";
        }
    }

    @Transactional
    public String deleteRoom(Long id) {
        if (!roomRepo.existsById(id)) return "notFound";
        if (lessonRepo.existsByRoomId(id)) return "inUse";

        try {
            roomRepo.deleteById(id);
            return null;
        } catch (DataIntegrityViolationException e) {
            return "inUse";
        }
    }

    // ---------- TIME SLOTS (disabled) ----------
    @Transactional
    public String addTimeSlotAuto() {
        return "disabled";
    }

    @Transactional
    public String deleteTimeSlot(Long id) {
        return "disabled";
    }

    // ---------- helpers ----------
    private String normalize(String s) {
        return s == null ? "" : s.trim();
    }
}
