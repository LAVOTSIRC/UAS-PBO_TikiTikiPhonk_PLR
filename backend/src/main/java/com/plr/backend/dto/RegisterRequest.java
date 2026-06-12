package com.plr.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "Username tidak boleh kosong")
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank(message = "Email tidak boleh kosong")
    @Email
    private String email;

    @NotBlank(message = "Password tidak boleh kosong")
    @Size(min = 6)
    private String password;

    public RegisterRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
