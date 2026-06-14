package com.plr.backend.service;

import com.plr.backend.dto.PomodoroRequest;
import com.plr.backend.dto.PomodoroResponse;

import java.util.List;

public interface IPomodoroService {
    PomodoroResponse logSession(PomodoroRequest request, Long userId);
    List<PomodoroResponse> getAllSessions(Long userId);
    List<PomodoroResponse> getSessionsByTask(Long taskId, Long userId);
    void deleteSession(Long id, Long userId);
}
