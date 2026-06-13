package com.plr.backend.service.impl;

import com.plr.backend.dto.StatSummaryResponse;
import com.plr.backend.dto.StatSummaryResponse.DaySummary;
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
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

        List<DaySummary> focusMinutesByDay = getDailyFocusMinutes(user);

        int currentStreak = computeStreak(focusMinutesByDay);
        System.out.println("[StatServiceImpl] currentStreak computed: " + currentStreak);

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

    private int computeStreak(List<DaySummary> focusMinutesByDay) {
        int streak = 0;
        for (int i = focusMinutesByDay.size() - 1; i >= 0; i--) {
            if (focusMinutesByDay.get(i).getMinutes() > 0) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    private List<DaySummary> getDailyFocusMinutes(User user) {
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
        List<Object[]> raw = pomodoroRepository.findDailyFocusMinutesSince(user.getId(), sevenDaysAgo);

        Map<LocalDate, Integer> dayMap = raw.stream()
            .collect(Collectors.toMap(
                row -> ((java.sql.Date) row[0]).toLocalDate(),
                row -> {
                    Number val = (Number) row[1];
                    return val != null ? val.intValue() : 0;
                }
            ));

        List<DaySummary> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().minusDays(6 - i);
            int minutes = dayMap.getOrDefault(date, 0);
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("id", "ID"));
            result.add(new DaySummary(dayName, minutes));
        }
        return result;
    }
}
