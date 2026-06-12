package com.plr.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Mematikan CSRF agar bisa mengirim data (POST/PUT) dari JavaFX atau Postman nantinya
                .csrf(csrf -> csrf.disable())

                // 2. Mengizinkan H2 Console ditampilkan
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // 3. Mengatur rute URL mana saja yang boleh diakses
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll() // Buka akses penuh tanpa login ke H2 Console
                        .anyRequest().permitAll() // SEMENTARA: Buka semua jalur API agar gampang testing
                );

        return http.build();
    }
}