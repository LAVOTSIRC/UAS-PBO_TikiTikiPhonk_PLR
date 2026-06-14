package com.plr.backend.service;

import com.plr.backend.dto.TaskRequest;
import com.plr.backend.dto.TaskResponse;

import java.util.List;

public interface ITaskService {
    TaskResponse createTask(TaskRequest request, Long userId);
    List<TaskResponse> getAllTasks(Long userId);
    TaskResponse updateTask(Long id, TaskRequest request, Long userId);
    void deleteTask(Long id, Long userId);
    TaskResponse getTaskById(Long id, Long userId);
}
