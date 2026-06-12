package com.plr.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "pomodoro_sessions")
public class PomodoroSession extends BaseEntity {

    @Min(value = 1, message = "Durasi minimal 1 menit")
    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @NotNull(message = "Tipe sesi tidak boleh null")
    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false, length = 20)
    private SessionType sessionType;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public PomodoroSession() {}

    public PomodoroSession(int durationMinutes, SessionType sessionType,
                           LocalDateTime startTime, String notes, User user) {
        this.durationMinutes = durationMinutes;
        this.sessionType = sessionType;
        this.startTime = startTime;
        this.notes = notes;
        this.user = user;
    }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public SessionType getSessionType() { return sessionType; }
    public void setSessionType(SessionType sessionType) { this.sessionType = sessionType; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @Override
    public String getEntityDescription() {
        return "PomodoroSession[id=" + getId() + ", type=" + sessionType + ", duration=" + durationMinutes + "min]";
    }
}
