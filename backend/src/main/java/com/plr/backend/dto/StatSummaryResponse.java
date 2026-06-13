package com.plr.backend.dto;

import java.util.Map;

public class StatSummaryResponse {

    private int totalFocusMinutes;
    private int totalSessions;
    private int completedTasks;
    private int activeTasks;
    private int totalPoints;
    private int currentStreak;
    private Map<String, Long> focusMinutesByDay;

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
    public Map<String, Long> getFocusMinutesByDay() { return focusMinutesByDay; }
    public void setFocusMinutesByDay(Map<String, Long> focusMinutesByDay) { this.focusMinutesByDay = focusMinutesByDay; }
}
