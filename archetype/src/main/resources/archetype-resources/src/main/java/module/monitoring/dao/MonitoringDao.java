package ${package}.module.monitoring.dao;

import ${package}.utils.DB;
import ${package}.utils.DB.Record;
import ${package}.utils.DB.Recordset;
import ${package}.module.monitoring.dto.MonitoringLogDto;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MonitoringDao {

    private final DataSource dataSource;

    public MonitoringDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long insertLog(String message) throws Exception {
        DB db = new DB(dataSource);
        try {
            db.open();
            db.query("INSERT INTO monitoring_logs (message, created_at) VALUES (?, ?)",
                    message, DB.toSqlTimestamp(LocalDateTime.now()));
            return db.lastInsertId();
        } finally {
            db.close();
        }
    }

    public List<MonitoringLogDto> findLogs(int limit, int offset) throws Exception {
        DB db = new DB(dataSource);
        List<MonitoringLogDto> logs = new ArrayList<>();
        try {
            db.open();
            Recordset rs = db.select("SELECT id, message, created_at FROM monitoring_logs ORDER BY id DESC LIMIT ? OFFSET ?",
                    limit, offset);
            for (Record r : rs) {
                logs.add(new MonitoringLogDto(
                        DB.toLong(r.get("id")),
                        DB.toString(r.get("message")),
                        DB.toLocalDateTime(r.get("created_at"))
                ));
            }
            return logs;
        } finally {
            db.close();
        }
    }
}
