package ${package}.module.status.dto;

public class HealthDto {
    private String status;

    public HealthDto() {}
    public HealthDto(String status) { this.status = status; }
    public String getStatus() { return status; }
}
