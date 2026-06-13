package com.plr.backend.dto;

import com.plr.backend.model.TaskCategory;
import com.plr.backend.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class TaskRequest {
    @NotBlank(message = "Judul tidak boleh kosong")
    private String title;

    private String description;

    @NotNull(message = "Status tidak boleh null")
    private TaskStatus status;

    private TaskCategory category;

    private LocalDateTime dueDate;

    public TaskRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public TaskCategory getCategory() { return category; }
    public void setCategory(TaskCategory category) { this.category = category; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
}
