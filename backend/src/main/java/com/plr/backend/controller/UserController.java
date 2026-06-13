package com.plr.backend.controller;

import com.plr.backend.dto.StatSummaryResponse;
import com.plr.backend.dto.UserProfileResponse;
import com.plr.backend.model.User;
import com.plr.backend.service.IStatService;
import com.plr.backend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IStatService statService;

    @GetMapping("/profile")
    public UserProfileResponse getProfile(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        UserProfileResponse res = new UserProfileResponse();
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setJoinedDate(user.getCreatedAt());
        return res;
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @RequestBody Map<String, String> body) {
        try {
            String newUsername = body.get("username");
            String newEmail = body.get("email");
            userService.updateProfile(authentication.getName(), newUsername, newEmail);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> body) {
        try {
            String oldPassword = body.get("oldPassword");
            String newPassword = body.get("newPassword");
            userService.changePassword(authentication.getName(), oldPassword, newPassword);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(
            Authentication authentication,
            @RequestBody Map<String, String> body) {
        try {
            String password = body.get("password");
            userService.deleteAccount(authentication.getName(), password);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public StatSummaryResponse getStats(Authentication authentication) {
        return statService.getSummary(authentication.getName());
    }
}
