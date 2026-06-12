package com.plr.backend.repository;

import com.plr.backend.model.PomodoroSession;
import com.plr.backend.model.SessionType;
import com.plr.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {
    List<PomodoroSession> findByUser(User user);
    List<PomodoroSession> findByUserAndSessionType(User user, SessionType sessionType);
    Optional<PomodoroSession> findByIdAndUser(Long id, User user);
}
