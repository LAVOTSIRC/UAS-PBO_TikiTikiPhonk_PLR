package com.plr.backend.controller;

import com.plr.backend.dto.PomodoroRequest;
import com.plr.backend.dto.PomodoroResponse;
import com.plr.backend.service.IPomodoroService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pomodoro")
@CrossOrigin(origins = "*")
public class PomodoroRestController {

    @Autowired
    private IPomodoroService pomodoroService;

    @GetMapping("/sessions")
    public ResponseEntity<List<PomodoroResponse>> getAllSessions(Authentication authentication) {
        List<PomodoroResponse> sessions = pomodoroService.getAllSessions(authentication.getName());
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/sessions")
    public ResponseEntity<PomodoroResponse> logSession(
            @Valid @RequestBody PomodoroRequest request,
            Authentication authentication) {
        PomodoroResponse session = pomodoroService.logSession(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            pomodoroService.deleteSession(id, authentication.getName());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
