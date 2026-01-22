package ${package}.module.status.controller;

import ${package}.module.status.dto.StatusLogDto;
import ${package}.module.status.dto.StatusHealthDto;
import ${package}.module.status.service.StatusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final StatusService service;

    public StatusController(StatusService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public StatusHealthDto health() {
        return service.getHealth();
    }

    @PostMapping("/log")
    public StatusLogDto log(@RequestBody String message) throws Exception {
        return service.log(message);
    }

    @GetMapping("/logs")
    public List<StatusLogDto> logs(
            @RequestParam(defaultValue = "10") int num,
            @RequestParam(defaultValue = "0") int off
    ) throws Exception {
        return service.getLogs(num, off);
    }
}
