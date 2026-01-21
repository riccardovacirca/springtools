package dev.myapp.module.monitoring.dto;

public class HealthDto {
    private String status;

    public HealthDto() {}
    public HealthDto(String status) { this.status = status; }
    public String getStatus() { return status; }
}
