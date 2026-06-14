package com.plr.backend.controller;

import com.plr.backend.dto.TaskRequest;
import com.plr.backend.dto.TaskResponse;
import com.plr.backend.model.User;
import com.plr.backend.service.ITaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskRestController {

    @Autowired
    private ITaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(Authentication authentication) {
        List<TaskResponse> tasks = taskService.getAllTasks(getUserId(authentication));
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request,
            Authentication authentication) {
        TaskResponse task = taskService.createTask(request, getUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            Authentication authentication) {
        try {
            TaskResponse updated = taskService.updateTask(id, request, getUserId(authentication));
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            // BUG-05 FIX: Bedakan 404 (task not found) dari 500 (error lain)
            String msg = e.getMessage();
            if (msg != null && msg.contains("Tugas tidak ditemukan")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().body(msg);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            taskService.deleteTask(id, getUserId(authentication));
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            // BUG-05 FIX: Bedakan 404 (task not found) dari 500 (error lain)
            String msg = e.getMessage();
            if (msg != null && msg.contains("Tugas tidak ditemukan")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            TaskResponse task = taskService.getTaskById(id, getUserId(authentication));
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            // BUG-05 FIX: Bedakan 404 (task not found) dari 500 (error lain)
            String msg = e.getMessage();
            if (msg != null && msg.contains("Tugas tidak ditemukan")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().body(msg);
        }
    }

    private Long getUserId(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }
}
