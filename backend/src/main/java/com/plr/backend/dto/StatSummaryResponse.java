package com.plr.backend.dto;

import java.util.List;

public class StatSummaryResponse {

    private int totalFocusMinutes;
    private int totalSessions;
    private int completedTasks;
    private int activeTasks;
    private int totalPoints;
    private int currentStreak;
    private List<DaySummary> focusMinutesByDay;

    public StatSummaryResponse() {}

    public int getTotalFocusMinutes() { return totalFocusMinutes; }
    public void setTotalFocusMinutes(int totalFocusMinutes) { this.totalFocusMinutes = totalFocusMinutes; }
    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
    public int getActiveTasks() { return activeTasks; }
    public void setActiveTasks(int activeTasks) { this.activeTasks = activeTasks; }
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public List<DaySummary> getFocusMinutesByDay() { return focusMinutesByDay; }
    public void setFocusMinutesByDay(List<DaySummary> focusMinutesByDay) { this.focusMinutesByDay = focusMinutesByDay; }

    public static class DaySummary {
        private String date;
        private int minutes;

        public DaySummary() {}

        public DaySummary(String date, int minutes) {
            this.date = date;
            this.minutes = minutes;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public int getMinutes() { return minutes; }
        public void setMinutes(int minutes) { this.minutes = minutes; }
    }
}
