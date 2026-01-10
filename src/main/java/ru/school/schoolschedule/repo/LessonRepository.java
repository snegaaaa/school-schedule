package ru.school.schoolschedule.repo;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ru.school.schoolschedule.model.Lesson;
import ru.school.schoolschedule.model.WeekDay;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findBySchoolClassIdAndWeekDayOrderByTimeSlotNumberAsc(Long schoolClassId, WeekDay weekDay);

    List<Lesson> findBySchoolClassIdOrderByWeekDayAscTimeSlotNumberAsc(Long schoolClassId);

    boolean existsBySchoolClassId(Long schoolClassId);

    boolean existsBySubjectId(Long subjectId);
    boolean existsByTeacherId(Long teacherId);
    boolean existsByRoomId(Long roomId);

    boolean existsByTimeSlotId(Long timeSlotId);

    // конфликты времени (учитель/кабинет)
    boolean existsByWeekDayAndTimeSlotIdAndTeacherId(WeekDay weekDay, Long timeSlotId, Long teacherId);
    boolean existsByWeekDayAndTimeSlotIdAndRoomId(WeekDay weekDay, Long timeSlotId, Long roomId);

    boolean existsByWeekDayAndTimeSlotIdAndTeacherIdAndIdNot(WeekDay weekDay, Long timeSlotId, Long teacherId, Long id);
    boolean existsByWeekDayAndTimeSlotIdAndRoomIdAndIdNot(WeekDay weekDay, Long timeSlotId, Long roomId, Long id);

    // конфликты времени (класс)
    boolean existsByWeekDayAndTimeSlotIdAndSchoolClassId(WeekDay weekDay, Long timeSlotId, Long schoolClassId);
    boolean existsByWeekDayAndTimeSlotIdAndSchoolClassIdAndIdNot(WeekDay weekDay, Long timeSlotId, Long schoolClassId, Long id);

    // --- статистика ---
    @Query("select t.fullName, count(l) from Lesson l join l.teacher t group by t.id, t.fullName order by count(l) desc")
    List<Object[]> topTeachers(Pageable pageable);

    @Query("select r.number, count(l) from Lesson l join l.room r group by r.id, r.number order by count(l) desc")
    List<Object[]> topRooms(Pageable pageable);

    @Query("select s.name, count(l) from Lesson l join l.subject s group by s.id, s.name order by count(l) desc")
    List<Object[]> topSubjects(Pageable pageable);

    @Query("select l.weekDay, count(l) from Lesson l group by l.weekDay")
    List<Object[]> countLessonsByWeekDay();
}
