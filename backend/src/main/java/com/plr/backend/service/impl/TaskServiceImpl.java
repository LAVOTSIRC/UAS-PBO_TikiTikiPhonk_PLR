package com.plr.backend.service.impl;

import com.plr.backend.dto.TaskRequest;
import com.plr.backend.dto.TaskResponse;
import com.plr.backend.model.Task;
import com.plr.backend.model.User;
import com.plr.backend.repository.TaskRepository;
import com.plr.backend.repository.UserRepository;
import com.plr.backend.service.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskServiceImpl implements ITaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public TaskResponse createTask(TaskRequest request, String username) {
        User user = findUser(username);
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setCategory(request.getCategory());
        task.setDueDate(request.getDueDate());
        task.setUser(user);
        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks(String username) {
        User user = findUser(username);
        return taskRepository.findByUser(user)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public TaskResponse updateTask(Long id, TaskRequest request, String username) {
        User user = findUser(username);
        Task task = taskRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new RuntimeException("Tugas tidak ditemukan dengan ID: " + id));
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setCategory(request.getCategory());
        task.setDueDate(request.getDueDate());
        Task updated = taskRepository.save(task);
        return toResponse(updated);
    }

    @Override
    public void deleteTask(Long id, String username) {
        User user = findUser(username);
        Task task = taskRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new RuntimeException("Tugas tidak ditemukan dengan ID: " + id));
        taskRepository.delete(task);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id, String username) {
        User user = findUser(username);
        Task task = taskRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new RuntimeException("Tugas tidak ditemukan dengan ID: " + id));
        return toResponse(task);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan: " + username));
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setCategory(task.getCategory());
        response.setDueDate(task.getDueDate());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
    }
}
