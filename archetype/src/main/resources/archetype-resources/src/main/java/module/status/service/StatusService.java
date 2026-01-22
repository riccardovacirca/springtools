package ${package}.module.status.service;

import ${package}.module.status.dao.StatusDao;
import ${package}.module.status.dto.StatusLogDto;
import ${package}.module.status.dto.StatusHealthDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatusService {

    private final StatusDao dao;

    public StatusService(StatusDao dao) {
        this.dao = dao;
    }

    public StatusHealthDto getHealth() {
        return new StatusHealthDto("UP");
    }

    public StatusLogDto log(String message) throws Exception {
        long id = dao.insertLog(message);
        return new StatusLogDto(id, message, java.time.LocalDateTime.now());
    }

    public List<StatusLogDto> getLogs(int limit, int offset) throws Exception {
        return dao.findLogs(limit, offset);
    }
}
