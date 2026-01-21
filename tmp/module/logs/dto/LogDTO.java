package dev.myapp.module.logs.dto;

import java.time.LocalDateTime;

public class LogDTO {

    private Long id;
    private String message;
    private LocalDateTime createdAt;

    public LogDTO() {}

    public LogDTO(Long id, String message, LocalDateTime createdAt) {
        this.id = id;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
