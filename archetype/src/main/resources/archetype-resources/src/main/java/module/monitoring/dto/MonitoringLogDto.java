package ${package}.module.monitoring.dto;

import java.time.LocalDateTime;

public class MonitoringLogDto {
    private Long id;
    private String message;
    private LocalDateTime createdAt;

    public MonitoringLogDto() {}

    public MonitoringLogDto(Long id, String message, LocalDateTime createdAt) {
        this.id = id;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
