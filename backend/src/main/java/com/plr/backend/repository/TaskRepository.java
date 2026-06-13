package com.plr.backend.repository;

import com.plr.backend.model.Task;
import com.plr.backend.model.TaskStatus;
import com.plr.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser(User user);
    List<Task> findByUserAndStatus(User user, TaskStatus status);
    Optional<Task> findByIdAndUser(Long id, User user);
    long countByUserAndStatus(User user, TaskStatus status);
}
