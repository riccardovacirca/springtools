package ${package}.module.auth.service;

import ${package}.module.auth.dao.SessionDao;
import ${package}.module.auth.dto.SessionDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SessionService {
  private final SessionDao dao;

  public SessionService(SessionDao dao) {
    this.dao = dao;
  }

  public Optional<SessionDto> getSession(String token) {
    return dao.get(token);
  }

  public boolean isValid(String token) {
    return dao.get(token).map(s -> s.attiva).orElse(false);
  }

  public List<SessionDto> getActiveSessions() {
    return dao.findAllActive();
  }

  public void invalidateUser(Long userId) {
    dao.invalidaByUser(userId);
  }
}
