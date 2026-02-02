package ${package}.module.status.service;

import ${package}.module.status.dao.StatusDao;
import ${package}.module.status.dto.StatusHealthDto;
import ${package}.module.status.dto.StatusLogDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StatusService
{

  private final StatusDao dao;

  public StatusService(StatusDao dao)
  {
    this.dao = dao;
  }

  public StatusHealthDto getHealth()
  {
    StatusHealthDto result;

    result = new StatusHealthDto("UP", 0, 0, 0, 0, 0, 0, 0);

    return result;
  }

  public StatusLogDto log(String message) throws Exception
  {
    long id;
    StatusLogDto result;

    id = dao.insertLog(message);
    result = new StatusLogDto(id, message, java.time.LocalDateTime.now());

    return result;
  }

  public List<StatusLogDto> getLogs(int limit, int offset) throws Exception
  {
    return dao.findLogs(limit, offset);
  }
}
