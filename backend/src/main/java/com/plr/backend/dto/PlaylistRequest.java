package com.plr.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class PlaylistRequest {
    @NotBlank(message = "Nama playlist tidak boleh kosong")
    private String name;

    private String description;

    public PlaylistRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
