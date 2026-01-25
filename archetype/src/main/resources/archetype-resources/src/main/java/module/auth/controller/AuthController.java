package ${package}.module.auth.controller;

import ${package}.module.auth.dto.LoginRequestDto;
import ${package}.module.auth.dto.LoginResponseDto;
import ${package}.module.auth.dto.SessionDto;
import ${package}.module.auth.service.AuthService;
import ${package}.module.auth.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  private final SessionService sessionService;

  public AuthController(AuthService authService,
                        SessionService sessionService) {
    this.authService = authService;
    this.sessionService = sessionService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto dto) {
    LoginResponseDto resp = authService.login(dto);
    if (resp == null) {
      return ResponseEntity.status(401).build();
    }
    return ResponseEntity.ok(resp);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
    if (token != null) {
      authService.logout(token.replace("Bearer ", ""));
    }
    return ResponseEntity.ok().build();
  }

  @GetMapping("/session")
  public ResponseEntity<SessionDto> getSession(@RequestHeader(value = "Authorization", required = false) String token) {
    if (token == null) {
      return ResponseEntity.status(401).build();
    }
    return sessionService.getSession(token.replace("Bearer ", ""))
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.status(401).build());
  }

  @GetMapping("/sessions")
  public List<SessionDto> getActiveSessions() {
    return sessionService.getActiveSessions();
  }

  @PostMapping("/invalidate/{userId}")
  public ResponseEntity<Void> invalidateUser(@PathVariable Long userId) {
    sessionService.invalidateUser(userId);
    return ResponseEntity.ok().build();
  }
}
