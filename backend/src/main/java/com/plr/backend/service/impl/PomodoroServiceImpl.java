package com.plr.backend.service.impl;

import com.plr.backend.dto.PomodoroRequest;
import com.plr.backend.dto.PomodoroResponse;
import com.plr.backend.model.PomodoroSession;
import com.plr.backend.model.SessionType;
import com.plr.backend.model.User;
import com.plr.backend.repository.PomodoroSessionRepository;
import com.plr.backend.repository.TaskRepository;
import com.plr.backend.repository.UserRepository;
import com.plr.backend.service.IPomodoroService;
import com.plr.backend.service.score.BreakSessionScore;
import com.plr.backend.service.score.FocusSessionScore;
import com.plr.backend.service.score.LongBreakSessionScore;
import com.plr.backend.service.score.SessionScoreCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PomodoroServiceImpl implements IPomodoroService {

    @Autowired
    private PomodoroSessionRepository pomodoroRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public PomodoroResponse logSession(PomodoroRequest request, String username) {
        User user = findUser(username);
        PomodoroSession session = new PomodoroSession();
        session.setDurationMinutes(request.getDurationMinutes());
        session.setSessionType(request.getSessionType());
        session.setStartTime(request.getStartTime() != null
            ? request.getStartTime()
            : LocalDateTime.now());
        session.setNotes(request.getNotes());
        session.setUser(user);

        if (request.getTaskId() != null) {
            taskRepository.findByIdAndUser(request.getTaskId(), user).ifPresent(task -> {
                task.setTotalSessions(task.getTotalSessions() + 1);
                taskRepository.save(task);
            });
        }

        SessionScoreCalculator calculator = getCalculator(session.getSessionType());
        int points = calculator.calculatePoints(session.getDurationMinutes());
        session.setPoints(points);

        PomodoroSession saved = pomodoroRepository.save(session);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PomodoroResponse> getAllSessions(String username) {
        User user = findUser(username);
        return pomodoroRepository.findByUser(user)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteSession(Long id, String username) {
        User user = findUser(username);
        PomodoroSession session = pomodoroRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new RuntimeException("Sesi Pomodoro tidak ditemukan dengan ID: " + id));
        pomodoroRepository.delete(session);
    }

    private SessionScoreCalculator getCalculator(SessionType type) {
        switch (type) {
            case FOCUS:
                return new FocusSessionScore();
            case LONG_BREAK:
                return new LongBreakSessionScore();
            case SHORT_BREAK:
            default:
                return new BreakSessionScore();
        }
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan: " + username));
    }

    private PomodoroResponse toResponse(PomodoroSession session) {
        PomodoroResponse response = new PomodoroResponse();
        response.setId(session.getId());
        response.setDurationMinutes(session.getDurationMinutes());
        response.setSessionType(session.getSessionType());
        response.setStartTime(session.getStartTime());
        response.setNotes(session.getNotes());
        response.setPoints(session.getPoints());
        response.setCreatedAt(session.getCreatedAt());
        return response;
    }
}
