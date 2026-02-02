package ${package}.module.status.controller;

import ${package}.module.status.dto.LogRequestDto;
import ${package}.module.status.dto.StatusHealthDto;
import ${package}.module.status.dto.StatusLogDto;
import ${package}.module.status.service.StatusService;
import dev.springtools.util.HttpResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/status")
public class StatusController
{
  private final StatusService service;

  public StatusController(StatusService service)
  {
    this.service = service;
  }

  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> health()
  {
    StatusHealthDto data;
    ResponseEntity<Map<String, Object>> resp;

    data = service.getHealth();
    resp = HttpResponse
        .create()
        .out(data)
        .contentType("application/json")
        .build();

    return resp;
  }

  @PostMapping("/log")
  public ResponseEntity<Map<String, Object>> log(@Valid @RequestBody LogRequestDto request)
      throws Exception
  {
    StatusLogDto log;
    ResponseEntity<Map<String, Object>> resp;
    String message;

    message = request.getMessage();
    log = service.log(message);
    resp = HttpResponse
        .create()
        .out(log)
        .status(HttpStatus.CREATED)
        .contentType("application/json")
        .build();

    return resp;
  }

  @GetMapping("/logs")
  public ResponseEntity<Map<String, Object>> logs(
      @RequestParam(defaultValue = "10") int num,
      @RequestParam(defaultValue = "0") int off)
      throws Exception
  {
    List<StatusLogDto> logs;
    ResponseEntity<Map<String, Object>> resp;

    logs = service.getLogs(num, off);
    resp = HttpResponse
        .create()
        .out(logs)
        .contentType("application/json")
        .build();

    return resp;
  }
}
