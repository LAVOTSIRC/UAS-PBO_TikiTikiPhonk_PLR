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
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", "admin@tikitiphkonk.com", passwordEncoder.encode("admin123"));
            admin.getRoles().add("ROLE_ADMIN");
            userRepository.save(admin);
            System.out.println("=== Default user created: admin / admin123 ===");
        }

        if (!userRepository.existsByUsername("user")) {
            User user = new User("user", "user@tikitiphkonk.com", passwordEncoder.encode("user123"));
            userRepository.save(user);
            System.out.println("=== Default user created: user / user123 ===");
        }
    }
}
