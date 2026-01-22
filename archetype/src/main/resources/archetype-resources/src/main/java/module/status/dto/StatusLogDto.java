package ${package}.module.status.dto;

import java.time.LocalDateTime;

public class StatusLogDto {
    private Long id;
    private String message;
    private LocalDateTime createdAt;

    public StatusLogDto() {}

    public StatusLogDto(Long id, String message, LocalDateTime createdAt) {
        this.id = id;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
