package com.example.homeworkapp;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarHelper {

    /**
     * Generates calendar days for the given month only.
     * Blank cells pad the start so day 1 aligns with its day-of-week (Sunday = column 0).
     * No days from other months are shown.
     */
    public List<CalendarDay> generateCalendarDays(YearMonth yearMonth, List<Assignment> assignments) {
        List<CalendarDay> calendarDays = new ArrayList<>();
        Map<LocalDate, List<Assignment>> assignmentMap = createAssignmentMap(assignments);

        LocalDate firstDay = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        int startOffset = firstDay.getDayOfWeek().getValue() % 7; 
        for (int i = 0; i < startOffset; i++) {
            calendarDays.add(CalendarDay.empty());
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            CalendarDay calendarDay = new CalendarDay(date, true);
            if (assignmentMap.containsKey(date)) {
                calendarDay.setAssignments(assignmentMap.get(date));
            }
            calendarDays.add(calendarDay);
        }

        return calendarDays;
    }

    private Map<LocalDate, List<Assignment>> createAssignmentMap(List<Assignment> assignments) {
        Map<LocalDate, List<Assignment>> map = new HashMap<>();
        for (Assignment a : assignments) {
            LocalDate d = a.getDueDateAsLocalDate();
            if (d != null) {
                map.putIfAbsent(d, new ArrayList<>());
                map.get(d).add(a);
            }
        }
        return map;
    }

    public String getMonthYearString(YearMonth yearMonth) {
        return yearMonth.getMonth().toString() + " " + yearMonth.getYear();
    }
}
