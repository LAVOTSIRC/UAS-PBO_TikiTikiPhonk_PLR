package com.plr.backend.service;

import com.plr.backend.dto.RegisterRequest;
import com.plr.backend.model.User;

public interface IUserService {
    User register(RegisterRequest request);
    User findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
