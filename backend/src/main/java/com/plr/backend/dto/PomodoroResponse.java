package com.plr.backend.dto;

import com.plr.backend.model.SessionType;
import java.time.LocalDateTime;

public class PomodoroResponse {
    private Long id;
    private int durationMinutes;
    private SessionType sessionType;
    private LocalDateTime startTime;
    private String notes;
    private LocalDateTime createdAt;

    public PomodoroResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public SessionType getSessionType() { return sessionType; }
    public void setSessionType(SessionType sessionType) { this.sessionType = sessionType; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
