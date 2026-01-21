package ${package}.module.status.controller;

import ${package}.module.status.dto.MonitoringLogDto;
import ${package}.module.status.dto.HealthDto;
import ${package}.module.status.service.MonitoringService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MonitoringService service;

    public MonitoringController(MonitoringService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public HealthDto health() {
        return service.getHealth();
    }

    @PostMapping("/log")
    public MonitoringLogDto log(@RequestBody String message) throws Exception {
        return service.log(message);
    }

    @GetMapping("/logs")
    public List<MonitoringLogDto> logs(
            @RequestParam(defaultValue = "10") int num,
            @RequestParam(defaultValue = "0") int off
    ) throws Exception {
        return service.getLogs(num, off);
    }
}
