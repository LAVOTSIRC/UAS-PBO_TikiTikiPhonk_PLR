package com.plr.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class SongRequest {
    @NotBlank(message = "Judul lagu tidak boleh kosong")
    private String title;

    @NotBlank(message = "Path file tidak boleh kosong")
    private String filePath;

    private Integer durationSeconds;
    private Long fileSize;

    public SongRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
}
