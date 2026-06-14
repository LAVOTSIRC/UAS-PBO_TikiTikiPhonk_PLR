package com.plr.backend.service;

import com.plr.backend.dto.RegisterRequest;
import com.plr.backend.model.User;

public interface IUserService {
    User register(RegisterRequest request);
    User findByUsername(String username);
    User findById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void updateProfile(Long userId, String newUsername, String newEmail);
    void changePassword(Long userId, String oldPassword, String newPassword);
    void deleteAccount(Long userId, String password);
    String saveProfilePicture(Long userId, byte[] imageBytes, String originalFileName);
    String getProfilePicturePath(Long userId);
}
