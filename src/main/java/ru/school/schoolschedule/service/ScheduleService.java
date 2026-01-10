package ru.school.schoolschedule.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.school.schoolschedule.model.Lesson;
import ru.school.schoolschedule.model.Room;
import ru.school.schoolschedule.model.SchoolClass;
import ru.school.schoolschedule.model.Subject;
import ru.school.schoolschedule.model.Teacher;
import ru.school.schoolschedule.model.TimeSlot;
import ru.school.schoolschedule.model.WeekDay;
import ru.school.schoolschedule.repo.LessonRepository;
import ru.school.schoolschedule.repo.RoomRepository;
import ru.school.schoolschedule.repo.SchoolClassRepository;
import ru.school.schoolschedule.repo.SubjectRepository;
import ru.school.schoolschedule.repo.TeacherRepository;
import ru.school.schoolschedule.repo.TimeSlotRepository;

@Service
public class ScheduleService {

    private final SchoolClassRepository classRepo;
    private final LessonRepository lessonRepo;
    private final SubjectRepository subjectRepo;
    private final TeacherRepository teacherRepo;
    private final RoomRepository roomRepo;
    private final TimeSlotRepository timeSlotRepo;

    public ScheduleService(SchoolClassRepository classRepo,
                           LessonRepository lessonRepo,
                           SubjectRepository subjectRepo,
                           TeacherRepository teacherRepo,
                           RoomRepository roomRepo,
                           TimeSlotRepository timeSlotRepo) {
        this.classRepo = classRepo;
        this.lessonRepo = lessonRepo;
        this.subjectRepo = subjectRepo;
        this.teacherRepo = teacherRepo;
        this.roomRepo = roomRepo;
        this.timeSlotRepo = timeSlotRepo;
    }

    // ---------- чтение (read-only) ----------

    @Transactional(readOnly = true)
    public List<SchoolClass> getClasses() {
        return classRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<TimeSlot> getTimeSlots() {
        return timeSlotRepo.findAllByOrderByNumberAsc();
    }

    @Transactional(readOnly = true)
    public List<Subject> getSubjects() {
        return subjectRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Teacher> getTeachers() {
        return teacherRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Room> getRooms() {
        return roomRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Lesson> findLesson(Long id) {
        return lessonRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Lesson> getLessonsForDay(Long classId, WeekDay day) {
        return lessonRepo.findBySchoolClassIdAndWeekDayOrderByTimeSlotNumberAsc(classId, day);
    }

    @Transactional(readOnly = true)
    public Map<WeekDay, List<Lesson>> getWeekByDay(Long classId) {
        List<Lesson> all = lessonRepo.findBySchoolClassIdOrderByWeekDayAscTimeSlotNumberAsc(classId);

        Map<WeekDay, List<Lesson>> byDay = new LinkedHashMap<>();
        for (WeekDay d : WeekDay.values()) byDay.put(d, new ArrayList<>());

        for (Lesson l : all) {
            WeekDay wd = l.getWeekDay();
            if (wd == null) continue; // защита от мусора
            List<Lesson> bucket = byDay.get(wd);
            if (bucket != null) bucket.add(l);
        }

        return byDay;
    }

    // ---------- изменение данных ----------

    /**
     * @return null если успешно, иначе: notFound / conflict
     */
    @Transactional
    public String addLesson(Long classId,
                            WeekDay day,
                            Long timeSlotId,
                            Long subjectId,
                            Long teacherId,
                            Long roomId) {

        var schoolClassOpt = classRepo.findById(classId);
        var timeSlotOpt = timeSlotRepo.findById(timeSlotId);
        var subjectOpt = subjectRepo.findById(subjectId);
        var teacherOpt = teacherRepo.findById(teacherId);
        var roomOpt = roomRepo.findById(roomId);

        if (schoolClassOpt.isEmpty() || timeSlotOpt.isEmpty() || subjectOpt.isEmpty()
                || teacherOpt.isEmpty() || roomOpt.isEmpty()) {
            return "notFound";
        }

        // дружественные проверки конфликтов (класс/учитель/кабинет)
        if (lessonRepo.existsByWeekDayAndTimeSlotIdAndSchoolClassId(day, timeSlotId, classId)) return "conflict";
        if (lessonRepo.existsByWeekDayAndTimeSlotIdAndTeacherId(day, timeSlotId, teacherId)) return "conflict";
        if (lessonRepo.existsByWeekDayAndTimeSlotIdAndRoomId(day, timeSlotId, roomId)) return "conflict";

        Lesson lesson = new Lesson();
        lesson.setSchoolClass(schoolClassOpt.get());
        lesson.setWeekDay(day);
        lesson.setTimeSlot(timeSlotOpt.get());
        lesson.setSubject(subjectOpt.get());
        lesson.setTeacher(teacherOpt.get());
        lesson.setRoom(roomOpt.get());

        try {
            lessonRepo.save(lesson);
        } catch (DataIntegrityViolationException e) {
            // финальная защита — UNIQUE constraint в БД
            return "conflict";
        }

        return null;
    }

    /**
     * @return null если успешно, иначе notFound
     */
    @Transactional
    public String deleteLesson(Long id) {
        try {
            lessonRepo.deleteById(id);
        } catch (EmptyResultDataAccessException ignored) {
            return "notFound";
        }
        return null;
    }

    /**
     * @return null если успешно, иначе: notFound / conflict
     * Проверка "это тот класс/день?" остаётся в контроллере.
     */
    @Transactional
    public String editLesson(Long id,
                             WeekDay day,
                             Long timeSlotId,
                             Long subjectId,
                             Long teacherId,
                             Long roomId) {

        var lessonOpt = lessonRepo.findById(id);
        if (lessonOpt.isEmpty()) return "notFound";

        Lesson lesson = lessonOpt.get();
        Long classId = lesson.getSchoolClass().getId();

        var timeSlotOpt = timeSlotRepo.findById(timeSlotId);
        var subjectOpt = subjectRepo.findById(subjectId);
        var teacherOpt = teacherRepo.findById(teacherId);
        var roomOpt = roomRepo.findById(roomId);

        if (timeSlotOpt.isEmpty() || subjectOpt.isEmpty() || teacherOpt.isEmpty() || roomOpt.isEmpty()) {
            return "notFound";
        }

        if (lessonRepo.existsByWeekDayAndTimeSlotIdAndSchoolClassIdAndIdNot(day, timeSlotId, classId, id)) return "conflict";
        if (lessonRepo.existsByWeekDayAndTimeSlotIdAndTeacherIdAndIdNot(day, timeSlotId, teacherId, id)) return "conflict";
        if (lessonRepo.existsByWeekDayAndTimeSlotIdAndRoomIdAndIdNot(day, timeSlotId, roomId, id)) return "conflict";

        lesson.setTimeSlot(timeSlotOpt.get());
        lesson.setSubject(subjectOpt.get());
        lesson.setTeacher(teacherOpt.get());
        lesson.setRoom(roomOpt.get());

        try {
            lessonRepo.save(lesson);
        } catch (DataIntegrityViolationException e) {
            return "conflict";
        }

        return null;
    }
}
