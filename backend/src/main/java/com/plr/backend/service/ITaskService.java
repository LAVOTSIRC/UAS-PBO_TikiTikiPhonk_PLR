package com.plr.backend.service;

import com.plr.backend.dto.TaskRequest;
import com.plr.backend.dto.TaskResponse;

import java.util.List;

public interface ITaskService {
    TaskResponse createTask(TaskRequest request, String username);
    List<TaskResponse> getAllTasks(String username);
    TaskResponse updateTask(Long id, TaskRequest request, String username);
    void deleteTask(Long id, String username);
    TaskResponse getTaskById(Long id, String username);
}
