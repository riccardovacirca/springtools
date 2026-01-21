package dev.myapp.module.monitoring.service;

import dev.myapp.module.monitoring.dao.MonitoringDao;
import dev.myapp.module.monitoring.dto.MonitoringLogDto;
import dev.myapp.module.monitoring.dto.HealthDto;
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
