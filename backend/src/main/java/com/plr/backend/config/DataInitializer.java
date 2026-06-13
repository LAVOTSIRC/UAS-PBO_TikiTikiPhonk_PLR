package com.plr.backend.config;

import com.plr.backend.model.User;
import com.plr.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("user1")) {
            userRepository.save(new User("user1", "user1@tikitikiphonk.com", passwordEncoder.encode("user123")));
            System.out.println("=== Default user created: user1 / user123 ===");
        }

        if (!userRepository.existsByUsername("user2")) {
            userRepository.save(new User("user2", "user2@tikitikiphonk.com", passwordEncoder.encode("user123")));
            System.out.println("=== Default user created: user2 / user123 ===");
        }

        if (!userRepository.existsByUsername("user3")) {
            userRepository.save(new User("user3", "user3@tikitikiphonk.com", passwordEncoder.encode("user123")));
            System.out.println("=== Default user created: user3 / user123 ===");
        }
    }
}
