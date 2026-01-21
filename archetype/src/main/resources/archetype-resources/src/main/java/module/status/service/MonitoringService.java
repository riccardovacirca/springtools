package ${package}.module.status.service;

import ${package}.module.status.dao.MonitoringDao;
import ${package}.module.status.dto.MonitoringLogDto;
import ${package}.module.status.dto.HealthDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitoringService {

    private final MonitoringDao dao;

    public MonitoringService(MonitoringDao dao) {
        this.dao = dao;
    }

    public HealthDto getHealth() {
        return new HealthDto("UP");
    }

    public MonitoringLogDto log(String message) throws Exception {
        long id = dao.insertLog(message);
        return new MonitoringLogDto(id, message, java.time.LocalDateTime.now());
    }

    public List<MonitoringLogDto> getLogs(int limit, int offset) throws Exception {
        return dao.findLogs(limit, offset);
    }
}
