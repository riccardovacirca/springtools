/*
 * tools - Java Tools Library
 * Copyright (C) 2018-2025 Riccardo Vacirca
 * Licensed under Exclusive Free Beta License
 * See LICENSE.md for full terms
 */
package ${package}.utils;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Java database abstraction layer providing simplified database operations
 * with JNDI integration, transaction support, and cursor-based result iteration.
 */
public class DB {

    private String source;
    private DataSource dataSource;
    private Connection connection;
    private long lastGeneratedKey = -1;

    /**
     * Constructor with JNDI resource name
     * @param jndiName JNDI resource name (e.g., "jdbc/MyDB")
     */
    public DB(String jndiName) {
        this.source = jndiName;
        this.dataSource = null;
    }

    /**
     * Constructor with DataSource (recommended for Spring Boot)
     * @param dataSource DataSource to use for connections
     */
    public DB(DataSource dataSource) {
        this.dataSource = dataSource;
        this.source = null;
    }

    /**
     * Opens database connection using JNDI datasource lookup or direct DataSource
     * @throws Exception if connection fails
     */
    public void open() throws Exception {
        if (dataSource != null) {
            // Use direct DataSource (Spring Boot)
            connection = dataSource.getConnection();
        } else if (source != null) {
            // Use JNDI lookup (legacy)
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/" + source);
            connection = ds.getConnection();
            ctx.close();
        } else {
            throw new Exception("No DataSource or JNDI name configured");
        }
    }

    /**
     * Closes database connection
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Silent close
            }
            connection = null;
        }
    }

    /**
     * Checks if database connection is active
     * @return true if connected
     */
    public boolean connected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Starts database transaction
     * @throws Exception if transaction start fails
     */
    public void begin() throws Exception {
        if (connection == null) {
            throw new Exception("Connection not available");
        }
        connection.setAutoCommit(false);
    }

    /**
     * Commits current transaction
     * @throws Exception if commit fails
     */
    public void commit() throws Exception {
        if (connection == null) {
            throw new Exception("Connection not available");
        }
        connection.commit();
        connection.setAutoCommit(true);
    }

    /**
     * Rolls back current transaction
     * @throws Exception if rollback fails
     */
    public void rollback() throws Exception {
        if (connection == null) {
            throw new Exception("Connection not available");
        }
        connection.rollback();
        connection.setAutoCommit(true);
    }

    /**
     * Executes modification query (INSERT, UPDATE, DELETE)
     * @param sql SQL statement with ? placeholders
     * @param params parameters for binding
     * @return number of affected rows
     * @throws Exception if query execution fails
     */
    public int query(String sql, Object... params) throws Exception {
        if (connection == null) {
            throw new Exception("Connection not available");
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            bindParameters(stmt, params);
            int affectedRows = stmt.executeUpdate();

            // Capture generated key if any
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    lastGeneratedKey = generatedKeys.getLong(1);
                } else {
                    lastGeneratedKey = -1;
                }
            }

            return affectedRows;
        }
    }

    /**
     * Executes SELECT query and returns all results
     * @param sql SQL SELECT statement with ? placeholders
     * @param params parameters for binding
     * @return Recordset containing all results
     * @throws Exception if query execution fails
     */
    public Recordset select(String sql, Object... params) throws Exception {
        if (connection == null) {
            throw new Exception("Connection not available");
        }

        Recordset recordset = new Recordset();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            bindParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                while (rs.next()) {
                    Record record = new Record();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = meta.getColumnName(i);
                        Object value = rs.getObject(i);
                        record.put(columnName, value);
                    }
                    recordset.add(record);
                }
            }
        }

        return recordset;
    }

    /**
     * Creates cursor for memory-efficient iteration
     * @param sql SQL SELECT statement with ? placeholders
     * @param params parameters for binding
     * @return Cursor for result set navigation
     * @throws Exception if cursor creation fails
     */
    public Cursor cursor(String sql, Object... params) throws Exception {
        if (connection == null) {
            throw new Exception("Connection not available");
        }

        PreparedStatement stmt = connection.prepareStatement(sql);
        bindParameters(stmt, params);
        ResultSet rs = stmt.executeQuery();

        return new Cursor(rs, stmt);
    }

    /**
     * Returns auto-generated key from last INSERT
     * Uses JDBC getGeneratedKeys() captured during query() execution
     * @return last insert ID, or -1 if no key was generated
     * @throws Exception if operation fails
     */
    public long lastInsertId() throws Exception {
        if (lastGeneratedKey == -1) {
            throw new Exception("No auto-generated key available from last INSERT");
        }
        return lastGeneratedKey;
    }

    /**
     * Binds parameters to prepared statement
     */
    private void bindParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    /**
     * Ottiene i nomi delle colonne di una tabella per validazione filtri dinamici.
     * <p>
     * Utilizzato per validare i filtri dinamici della query string,
     * garantendo che solo colonne esistenti vengano usate nelle clausole WHERE.
     * Questo previene SQL injection e errori di query.
     * <p>
     * Esempio:
     * <pre>
     *   java.util.Set&lt;String&gt; columns = db.getTableColumns("users");
     *   if (columns.contains("email")) {
     *       // OK: la colonna esiste, posso filtrare
     *   }
     * </pre>
     * <p>
     * IMPORTANTE: I nomi delle colonne vengono convertiti in lowercase per
     * facilitare il confronto case-insensitive.
     *
     * @param tableName Nome della tabella di cui ottenere le colonne
     * @return Set contenente i nomi delle colonne (in lowercase)
     * @throws Exception se la connessione non è disponibile o si verifica un errore
     */
    public java.util.Set<String> getTableColumns(String tableName) throws Exception {
        if (connection == null) {
            throw new Exception("Connection not available");
        }

        java.util.Set<String> columns = new java.util.HashSet<>();

        try {
            DatabaseMetaData metaData = connection.getMetaData();

            // Prova prima con il nome originale, poi con uppercase (per DB case-sensitive)
            ResultSet rs = metaData.getColumns(null, null, tableName, null);

            // Se non trova nulla, prova con uppercase (alcuni DB usano table names in uppercase)
            if (!rs.next()) {
                rs.close();
                rs = metaData.getColumns(null, null, tableName.toUpperCase(), null);
            } else {
                // Riposiziona il cursor all'inizio
                rs.close();
                rs = metaData.getColumns(null, null, tableName, null);
            }

            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                if (columnName != null) {
                    columns.add(columnName.toLowerCase());
                }
            }

            rs.close();
        } catch (SQLException e) {
            throw new Exception("Error retrieving table columns for: " + tableName, e);
        }

        return columns;
    }

    // ========================================
    // Type Conversion Helpers (Java 8+ Time API)
    // ========================================

    /**
     * Converts SQL Date to Java LocalDate
     * @param sqlDate SQL Date object from ResultSet
     * @return LocalDate or null if input is null
     */
    public static java.time.LocalDate toLocalDate(Object sqlDate) {
        if (sqlDate == null) {
            return null;
        }
        if (sqlDate instanceof java.sql.Date) {
            return ((java.sql.Date) sqlDate).toLocalDate();
        }
        return null;
    }

    /**
     * Converts SQL Time to Java LocalTime
     * @param sqlTime SQL Time object from ResultSet
     * @return LocalTime or null if input is null
     */
    public static java.time.LocalTime toLocalTime(Object sqlTime) {
        if (sqlTime == null) {
            return null;
        }
        if (sqlTime instanceof java.sql.Time) {
            return ((java.sql.Time) sqlTime).toLocalTime();
        }
        return null;
    }

    /**
     * Converts SQL Timestamp to Java LocalDateTime
     * @param sqlTimestamp SQL Timestamp object from ResultSet
     * @return LocalDateTime or null if input is null
     */
    public static java.time.LocalDateTime toLocalDateTime(Object sqlTimestamp) {
        if (sqlTimestamp == null) {
            return null;
        }
        if (sqlTimestamp instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) sqlTimestamp).toLocalDateTime();
        }
        return null;
    }

    /**
     * Converts Java LocalDate to SQL Date
     * @param localDate Java LocalDate
     * @return SQL Date or null if input is null
     */
    public static java.sql.Date toSqlDate(java.time.LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return java.sql.Date.valueOf(localDate);
    }

    /**
     * Converts Java LocalTime to SQL Time
     * @param localTime Java LocalTime
     * @return SQL Time or null if input is null
     */
    public static java.sql.Time toSqlTime(java.time.LocalTime localTime) {
        if (localTime == null) {
            return null;
        }
        return java.sql.Time.valueOf(localTime);
    }

    /**
     * Converts Java LocalDateTime to SQL Timestamp
     * @param localDateTime Java LocalDateTime
     * @return SQL Timestamp or null if input is null
     */
    public static java.sql.Timestamp toSqlTimestamp(java.time.LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return java.sql.Timestamp.valueOf(localDateTime);
    }

    /**
     * Safely casts Object to Long
     * @param value Object from ResultSet
     * @return Long or null if input is null or not castable
     */
    public static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    /**
     * Safely casts Object to Integer
     * @param value Object from ResultSet
     * @return Integer or null if input is null or not castable
     */
    public static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * Safely casts Object to Boolean
     * @param value Object from ResultSet
     * @return Boolean or null if input is null or not castable
     */
    public static Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }

    /**
     * Safely casts Object to String
     * @param value Object from ResultSet
     * @return String or null if input is null
     */
    public static String toString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * Safely casts Object to BigDecimal
     * @param value Object from ResultSet
     * @return BigDecimal or null if input is null or not castable
     */
    public static java.math.BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.math.BigDecimal) {
            return (java.math.BigDecimal) value;
        }
        return null;
    }

    /**
     * Single database record as key-value map
     */
    public static class Record extends HashMap<String, Object> {
    }

    /**
     * Collection of database records
     */
    public static class Recordset extends ArrayList<Record> {
    }

    /**
     * Memory-efficient iterator for large result sets
     */
    public static class Cursor {
        private ResultSet resultSet;
        private PreparedStatement statement;

        Cursor(ResultSet resultSet, PreparedStatement statement) {
            this.resultSet = resultSet;
            this.statement = statement;
        }

        /**
         * Moves to next row
         * @return true if row available, false if end reached
         * @throws Exception if operation fails
         */
        public boolean next() throws Exception {
            return resultSet.next();
        }

        /**
         * Gets column value from current row
         * @param column column name
         * @return column value
         * @throws Exception if operation fails
         */
        public Object get(String column) throws Exception {
            return resultSet.getObject(column);
        }

        /**
         * Gets entire current row as Record
         * @return Record containing all columns
         * @throws Exception if operation fails
         */
        public Record getRow() throws Exception {
            Record record = new Record();
            ResultSetMetaData meta = resultSet.getMetaData();
            int columnCount = meta.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = meta.getColumnName(i);
                Object value = resultSet.getObject(i);
                record.put(columnName, value);
            }

            return record;
        }

        /**
         * Closes cursor and releases resources
         */
        public void close() {
            if (resultSet != null) {
                try { resultSet.close(); } catch (SQLException e) { }
            }
            if (statement != null) {
                try { statement.close(); } catch (SQLException e) { }
            }
        }
    }
}