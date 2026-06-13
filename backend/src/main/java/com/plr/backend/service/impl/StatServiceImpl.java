package com.plr.backend.service.impl;

import com.plr.backend.dto.StatSummaryResponse;
import com.plr.backend.model.PomodoroSession;
import com.plr.backend.model.TaskStatus;
import com.plr.backend.model.User;
import com.plr.backend.repository.PomodoroSessionRepository;
import com.plr.backend.repository.TaskRepository;
import com.plr.backend.repository.UserRepository;
import com.plr.backend.service.IStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StatServiceImpl implements IStatService {

    @Autowired
    private PomodoroSessionRepository pomodoroRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public StatSummaryResponse getSummary(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan: " + username));

        List<PomodoroSession> allSessions = pomodoroRepository.findByUser(user);

        int totalFocusMinutes = allSessions.stream()
            .filter(s -> s.getSessionType().name().equals("FOCUS"))
            .mapToInt(PomodoroSession::getDurationMinutes)
            .sum();

        int totalSessions = allSessions.size();
        int totalPoints = allSessions.stream().mapToInt(PomodoroSession::getPoints).sum();

        long completedTasks = taskRepository.countByUserAndStatus(user, TaskStatus.DONE);

        long activeTasks = taskRepository.countByUserAndStatus(user, TaskStatus.TODO)
                         + taskRepository.countByUserAndStatus(user, TaskStatus.IN_PROGRESS);

        Map<String, Long> focusMinutesByDay = getDailyFocusMinutes(user);

        int currentStreak = computeStreak(focusMinutesByDay);

        StatSummaryResponse response = new StatSummaryResponse();
        response.setTotalFocusMinutes(totalFocusMinutes);
        response.setTotalSessions(totalSessions);
        response.setCompletedTasks((int) completedTasks);
        response.setActiveTasks((int) activeTasks);
        response.setTotalPoints(totalPoints);
        response.setCurrentStreak(currentStreak);
        response.setFocusMinutesByDay(focusMinutesByDay);
        return response;
    }

    private int computeStreak(Map<String, Long> focusMinutesByDay) {
        List<String> sortedDates = new ArrayList<>(focusMinutesByDay.keySet());
        Collections.sort(sortedDates);
        int streak = 0;
        for (int i = sortedDates.size() - 1; i >= 0; i--) {
            if (focusMinutesByDay.get(sortedDates.get(i)) > 0) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    private Map<String, Long> getDailyFocusMinutes(User user) {
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
        List<Object[]> raw = pomodoroRepository.findDailyFocusMinutesSince(user.getId(), sevenDaysAgo);

        Map<String, Long> dayMap = raw.stream().collect(Collectors.toMap(
            row -> ((java.sql.Date) row[0]).toLocalDate().toString(),
            row -> {
                Number val = (Number) row[1];
                return val != null ? val.longValue() : 0L;
            }
        ));

        Map<String, Long> result = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().minusDays(6 - i);
            String key = date.toString(); // ISO "YYYY-MM-DD"
            result.put(key, dayMap.getOrDefault(key, 0L));
        }
        return result;
    }
}
