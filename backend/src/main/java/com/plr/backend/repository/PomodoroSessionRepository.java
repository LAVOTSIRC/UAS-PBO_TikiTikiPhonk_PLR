package com.plr.backend.repository;

import com.plr.backend.model.PomodoroSession;
import com.plr.backend.model.SessionType;
import com.plr.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {
    List<PomodoroSession> findByUser(User user);
    List<PomodoroSession> findByUserOrderByStartTimeAsc(User user);
    List<PomodoroSession> findByUserAndSessionType(User user, SessionType sessionType);
    Optional<PomodoroSession> findByIdAndUser(Long id, User user);
    List<PomodoroSession> findByTaskIdAndUser(Long taskId, User user);
    List<PomodoroSession> findByTaskIdAndUserOrderByStartTimeAsc(Long taskId, User user);
    List<PomodoroSession> findByTaskId(Long taskId);

    @Query(value = "SELECT CAST(p.start_time AS date) AS sessionDate, COALESCE(SUM(p.duration_minutes), 0) " +
           "FROM pomodoro_sessions p " +
           "WHERE p.user_id = :userId AND p.session_type = 'FOCUS' AND p.start_time >= :since " +
           "GROUP BY CAST(p.start_time AS date) " +
           "ORDER BY CAST(p.start_time AS date) ASC", nativeQuery = true)
    List<Object[]> findDailyFocusMinutesSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
