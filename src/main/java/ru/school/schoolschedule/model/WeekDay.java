package ru.school.schoolschedule.model;

public enum WeekDay {
    MONDAY("Понедельник"),
    TUESDAY("Вторник"),
    WEDNESDAY("Среда"),
    THURSDAY("Четверг"),
    FRIDAY("Пятница"),
    SATURDAY("Суббота");

    private final String title;

    WeekDay(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
