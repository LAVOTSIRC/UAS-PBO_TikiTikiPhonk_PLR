package com.plr.backend.dto;

import com.plr.backend.model.SessionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class PomodoroRequest {
    @Min(value = 0, message = "Durasi minimal 0 menit")
    private int durationMinutes;

    @NotNull(message = "Tipe sesi tidak boleh null")
    private SessionType sessionType;

    private LocalDateTime startTime;
    private Long taskId;

    public PomodoroRequest() {}

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public SessionType getSessionType() { return sessionType; }
    public void setSessionType(SessionType sessionType) { this.sessionType = sessionType; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
}
