package com.plr.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PlaylistResponse {
    private Long id;
    private String name;
    private String description;
    private int songCount;
    private List<SongResponse> songs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PlaylistResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getSongCount() { return songCount; }
    public void setSongCount(int songCount) { this.songCount = songCount; }
    public List<SongResponse> getSongs() { return songs; }
    public void setSongs(List<SongResponse> songs) { this.songs = songs; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
