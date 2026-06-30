package com.example.homeworkapp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarDay {
    private LocalDate date;
    private List<Assignment> assignments;
    private boolean isCurrentMonth;
    private boolean isEmpty;

    public CalendarDay(LocalDate date, boolean isCurrentMonth) {
        this.date = date;
        this.isCurrentMonth = isCurrentMonth;
        this.assignments = new ArrayList<>();
        this.isEmpty = false;
    }

    public static CalendarDay empty() {
        CalendarDay d = new CalendarDay(LocalDate.now(), false);
        d.isEmpty = true;
        return d;
    }

    public boolean isEmpty() { return isEmpty; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<Assignment> getAssignments() { return assignments; }
    public void setAssignments(List<Assignment> assignments) { this.assignments = assignments; }
    public void addAssignment(Assignment assignment) { this.assignments.add(assignment); }

    public boolean isCurrentMonth() { return isCurrentMonth; }
    public void setCurrentMonth(boolean currentMonth) { isCurrentMonth = currentMonth; }

    public int getDay() { return date.getDayOfMonth(); }
    public int getAssignmentCount() { return assignments.size(); }
    public boolean hasAssignments() { return !assignments.isEmpty(); }
}
