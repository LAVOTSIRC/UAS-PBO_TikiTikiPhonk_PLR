package com.plr.backend.service;

import com.plr.backend.dto.RegisterRequest;
import com.plr.backend.model.User;

public interface IUserService {
    User register(RegisterRequest request);
    User findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void updateProfile(String currentUsername, String newUsername, String newEmail);
    void changePassword(String username, String oldPassword, String newPassword);
    void deleteAccount(String username, String password);
}
