package com.plr.backend.service;

import com.plr.backend.dto.PomodoroRequest;
import com.plr.backend.dto.PomodoroResponse;

import java.util.List;

public interface IPomodoroService {
    PomodoroResponse logSession(PomodoroRequest request, String username);
    List<PomodoroResponse> getAllSessions(String username);
    List<PomodoroResponse> getSessionsByTask(Long taskId, String username);
    void deleteSession(Long id, String username);
}
