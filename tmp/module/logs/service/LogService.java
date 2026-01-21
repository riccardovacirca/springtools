package dev.myapp.module.logs.service;

import dev.myapp.module.logs.dao.LogDao;
import dev.myapp.module.logs.dto.LogDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {

    private final LogDao logDao;

    public LogService(LogDao logDao) {
        this.logDao = logDao;
    }

    public LogDto create(String message) throws Exception {
        long id = logDao.insert(message);
        return logDao.findById(id);
    }

    public LogDto get(long id) throws Exception {
        return logDao.findById(id);
    }

    public List<LogDto> find(int num, int off) throws Exception {
        return logDao.find(num, off);
    }
}
