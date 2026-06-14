package com.plr.backend.controller;

import com.plr.backend.dto.StatSummaryResponse;
import com.plr.backend.dto.UserProfileResponse;
import com.plr.backend.model.User;
import com.plr.backend.service.IStatService;
import com.plr.backend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IStatService statService;

    @Value("${app.profile-pictures.dir:./data/profile-pictures/}")
    private String profilePicturesDir;

    @GetMapping("/profile")
    public UserProfileResponse getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        UserProfileResponse res = new UserProfileResponse();
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setJoinedDate(user.getCreatedAt());
        res.setProfilePicturePath(user.getProfilePicturePath());
        return res;
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @RequestBody Map<String, String> body) {
        try {
            String newUsername = body.get("username");
            String newEmail = body.get("email");
            userService.updateProfile(getUserId(authentication), newUsername, newEmail);
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
            userService.changePassword(getUserId(authentication), oldPassword, newPassword);
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
            userService.deleteAccount(getUserId(authentication), password);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public StatSummaryResponse getStats(Authentication authentication) {
        return statService.getSummary(getUserId(authentication));
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File tidak boleh kosong"));
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "File harus berupa gambar"));
            }
            String savedPath = userService.saveProfilePicture(
                getUserId(authentication), file.getBytes(), file.getOriginalFilename());
            return ResponseEntity.ok(Map.of("profilePicturePath", savedPath));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile-picture/file/{userId}")
    public ResponseEntity<Resource> getProfilePictureFile(@PathVariable Long userId) {
        try {
            String path = userService.getProfilePicturePath(userId);
            if (path == null || path.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Path filePath = Paths.get(path);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "image/png";
            }
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName() + "\"")
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Long getUserId(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }
}
