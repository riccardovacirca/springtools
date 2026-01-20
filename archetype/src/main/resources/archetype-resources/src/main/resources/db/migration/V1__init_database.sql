-- Tabella messaggi di esempio
CREATE TABLE messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    author TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Dati di esempio
INSERT INTO messages (title, content, author) VALUES
    ('Benvenuto', 'Benvenuto nella tua applicazione Spring Boot!', 'System'),
    ('Hello World', 'Questo è un messaggio di esempio dal database SQLite', 'Admin'),
    ('Flyway', 'Le migration del database sono gestite da Flyway', 'Developer');
