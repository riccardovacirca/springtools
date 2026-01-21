package dev.myapp.module.logs.controller;

import dev.myapp.module.logs.dto.LogDto;
import dev.myapp.module.logs.service.LogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/find")
    public List<LogDto> find(
            @RequestParam(defaultValue = "10") int num,
            @RequestParam(defaultValue = "0") int off
    ) throws Exception {
        return logService.find(num, off);
    }

    @PostMapping("/log")
    public LogDto create(@RequestBody String message) throws Exception {
        return logService.create(message);
    }

    @GetMapping("/log/{id}")
    public LogDto get(@PathVariable long id) throws Exception {
        return logService.get(id);
    }
}
