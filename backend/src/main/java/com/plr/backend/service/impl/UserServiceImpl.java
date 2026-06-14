package com.plr.backend.service.impl;

import com.plr.backend.dto.RegisterRequest;
import com.plr.backend.model.Playlist;
import com.plr.backend.model.PomodoroSession;
import com.plr.backend.model.Task;
import com.plr.backend.model.User;
import com.plr.backend.repository.PlaylistRepository;
import com.plr.backend.repository.PomodoroSessionRepository;
import com.plr.backend.repository.TaskRepository;
import com.plr.backend.repository.UserRepository;
import com.plr.backend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PomodoroSessionRepository pomodoroSessionRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Value("${app.profile-pictures.dir:./data/profile-pictures/}")
    private String profilePicturesDir;

    @Override
    public User register(RegisterRequest request) {
        if (existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username sudah digunakan: " + request.getUsername());
        }
        if (existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email sudah digunakan: " + request.getEmail());
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username atau email sudah digunakan oleh akun lain");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan dengan ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void updateProfile(Long userId, String newUsername, String newEmail) {
        User user = findById(userId);
        if (!user.getUsername().equals(newUsername) && existsByUsername(newUsername)) {
            throw new IllegalArgumentException("Username sudah digunakan");
        }
        if (!user.getEmail().equals(newEmail) && existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email sudah digunakan");
        }
        user.setUsername(newUsername);
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = findById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Password lama tidak cocok");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password baru minimal 6 karakter");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("Password baru tidak boleh sama dengan password lama");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void deleteAccount(Long userId, String password) {
        User user = findById(userId);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Password tidak cocok");
        }

        // Hapus semua child entities terlebih dahulu untuk menghindari FK constraint
        List<Task> tasks = taskRepository.findByUser(user);
        taskRepository.deleteAll(tasks);

        List<PomodoroSession> sessions = pomodoroSessionRepository.findByUser(user);
        pomodoroSessionRepository.deleteAll(sessions);

        List<Playlist> playlists = playlistRepository.findByUser(user);
        playlistRepository.deleteAll(playlists);

        // Hapus profile picture jika ada
        if (user.getProfilePicturePath() != null) {
            try {
                Path picPath = Paths.get(user.getProfilePicturePath());
                Files.deleteIfExists(picPath);
            } catch (Exception ignored) {}
        }

        userRepository.delete(user);
    }

    @Override
    public String saveProfilePicture(Long userId, byte[] imageBytes, String originalFileName) {
        User user = findById(userId);
        try {
            Path uploadDir = Paths.get(profilePicturesDir);
            Files.createDirectories(uploadDir);

            String extension = "png";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
            }
            String fileName = user.getId() + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;
            Path targetPath = uploadDir.resolve(fileName);
            Files.write(targetPath, imageBytes);

            // Hapus file lama jika ada
            if (user.getProfilePicturePath() != null) {
                try {
                    Path oldPath = Paths.get(user.getProfilePicturePath());
                    Files.deleteIfExists(oldPath);
                } catch (Exception ignored) {}
            }

            user.setProfilePicturePath(targetPath.toAbsolutePath().toString());
            userRepository.save(user);
            return targetPath.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException("Gagal menyimpan foto profil: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getProfilePicturePath(Long userId) {
        User user = findById(userId);
        return user.getProfilePicturePath();
    }
}
