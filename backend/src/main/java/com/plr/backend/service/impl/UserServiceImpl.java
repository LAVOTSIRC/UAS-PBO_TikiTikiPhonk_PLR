package com.plr.backend.service.impl;

import com.plr.backend.dto.RegisterRequest;
import com.plr.backend.model.User;
import com.plr.backend.repository.UserRepository;
import com.plr.backend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        // BUG-13 FIX: Tangkap race condition jika 2 request register bersamaan
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
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void updateProfile(String currentUsername, String newUsername, String newEmail) {
        User user = findByUsername(currentUsername);
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
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = findByUsername(username);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Password lama tidak cocok");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password baru minimal 6 karakter");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void deleteAccount(String username, String password) {
        User user = findByUsername(username);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Password tidak cocok");
        }
        userRepository.delete(user);
    }
}
