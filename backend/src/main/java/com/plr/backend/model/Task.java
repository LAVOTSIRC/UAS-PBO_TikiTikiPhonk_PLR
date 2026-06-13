package com.plr.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task extends BaseEntity {

    @NotBlank(message = "Judul tugas tidak boleh kosong")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Status tidak boleh null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TaskCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Task() {
        this.status = TaskStatus.TODO;
    }

    public Task(String title, String description, TaskStatus status, TaskCategory category, LocalDateTime dueDate, User user) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.category = category;
        this.dueDate = dueDate;
        this.user = user;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public TaskCategory getCategory() { return category; }
    public void setCategory(TaskCategory category) { this.category = category; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @Override
    public String getEntityDescription() {
        return "Task[id=" + getId() + ", title=" + title + ", status=" + status + ", category=" + category + "]";
    }
}
