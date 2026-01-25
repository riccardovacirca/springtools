package ${package}.module.auth.service;

import ${package}.module.auth.dao.UserDao;
import ${package}.module.auth.dao.SessionDao;
import ${package}.module.auth.dto.LoginRequestDto;
import ${package}.module.auth.dto.LoginResponseDto;
import ${package}.module.auth.dto.SessionDto;
import ${package}.module.auth.dto.UserDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class AuthService {
  private final UserDao userDao;
  private final SessionDao sessionDao;

  public AuthService(UserDao userDao,
                     SessionDao sessionDao) {
    this.userDao = userDao;
    this.sessionDao = sessionDao;
  }

  public LoginResponseDto login(LoginRequestDto dto) {
    if (!userDao.verificaCredenziali(dto.username, dto.password)) {
      return null;
    }

    UserDto user = userDao.findByUsername(dto.username).orElse(null);
    if (user == null) {
      return null;
    }

    // Crea sessione
    SessionDto session = new SessionDto();
    session.userId = user.id;
    session.username = user.username;
    session.ruolo = user.ruolo;
    session.attiva = true;
    session.token = UUID.randomUUID().toString();
    session.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    sessionDao.salva(session);

    // Response
    LoginResponseDto resp = new LoginResponseDto();
    resp.token = session.token;
    resp.userId = user.id;
    resp.username = user.username;
    resp.ruolo = user.ruolo;
    return resp;
  }

  public void logout(String token) {
    sessionDao.rimuovi(token);
  }
}
