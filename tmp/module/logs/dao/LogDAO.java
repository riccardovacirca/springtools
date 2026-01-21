package dev.myapp.module.logs.dao;

import dev.hello.utils.DB;
import dev.myapp.module.logs.dto.LogDto;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LogDAO {

    private static final String JNDI_SOURCE = "jdbc/hello";

    public long insert(String message) throws Exception {
        DB db = new DB(JNDI_SOURCE);
        try {
            db.open();
            db.query(
                "INSERT INTO logs (message, created_at) VALUES (?, ?)",
                message,
                DB.toSqlTimestamp(LocalDateTime.now())
            );
            return db.lastInsertId();
        } finally {
            db.close();
        }
    }

    public LogDto findById(long id) throws Exception {
        DB db = new DB(JNDI_SOURCE);
        try {
            db.open();
            DB.Recordset rs = db.select(
                "SELECT id, message, created_at FROM logs WHERE id = ?",
                id
            );

            if (rs.isEmpty()) {
                return null;
            }

            DB.Record r = rs.get(0);
            return map(r);
        } finally {
            db.close();
        }
    }

    public List<LogDto> find(int limit, int offset) throws Exception {
        DB db = new DB(JNDI_SOURCE);
        List<LogDto> list = new ArrayList<>();
        try {
            db.open();
            DB.Recordset rs = db.select(
                "SELECT id, message, created_at " +
                "FROM logs ORDER BY id DESC LIMIT ? OFFSET ?",
                limit, offset
            );

            for (DB.Record r : rs) {
                list.add(map(r));
            }
            return list;
        } finally {
            db.close();
        }
    }

    private LogDto map(DB.Record r) {
        return new LogDto(
            DB.toLong(r.get("id")),
            DB.toString(r.get("message")),
            DB.toLocalDateTime(r.get("created_at"))
        );
    }
}
