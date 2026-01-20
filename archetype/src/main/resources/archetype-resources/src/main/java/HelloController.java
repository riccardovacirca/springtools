package ${package};

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        // Legge tutti i messaggi dal database
        List<Map<String, Object>> messages = jdbcTemplate.queryForList(
            "SELECT id, title, content, author, created_at FROM messages ORDER BY id"
        );

        return Map.of(
            "timestamp", java.time.LocalDateTime.now().toString(),
            "database", "SQLite",
            "migration", "Flyway",
            "messages", messages
        );
    }
}
