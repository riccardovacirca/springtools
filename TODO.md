# TODO

- Aggiungere al comando 'cmd git fetch', l'esecuzione del comando
  git config --global --add safe.directory /usr/src/app
  per contrassegnare il repository locale come sicuro

- Reimplementare la classe JSON della libreria util il modo spring idiomatico
  e correggere le eventuali chiamate esistenti:
  package dev.crm.util;
  import com.fasterxml.jackson.databind.ObjectMapper;
  import org.springframework.stereotype.Component;
  import java.nio.charset.StandardCharsets;
  import java.nio.file.Files;
  import java.nio.file.Paths;
  @Component
  public class JSON {
    private final ObjectMapper mapper;
    public JSON(ObjectMapper mapper) {
      this.mapper = mapper;
    }
    public String encode(Object obj) throws Exception {
      return mapper.writeValueAsString(obj);
    }
    public <T> T decode(String json, Class<T> cls) throws Exception {
      return mapper.readValue(json, cls);
    }
    public String load(String filename) throws Exception {
      return new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
    }
    public <T> T load(String filename, Class<T> cls) throws Exception {
      return decode(load(filename), cls);
    }
  }

- Reimplementare la classe db in modo spring idiomatico:
  package dev.crm.util;
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.jdbc.core.JdbcTemplate;
  import org.springframework.jdbc.support.GeneratedKeyHolder;
  import org.springframework.jdbc.support.KeyHolder;
  import org.springframework.transaction.PlatformTransactionManager;
  import org.springframework.transaction.TransactionStatus;
  import org.springframework.transaction.support.DefaultTransactionDefinition;
  import java.sql.PreparedStatement;
  import java.sql.ResultSetMetaData;
  import java.sql.SQLException;
  import java.util.*;
  /**
  * Java database abstraction layer using Spring JdbcTemplate
  * with transaction support.
  */
  public class DB {
    private final JdbcTemplate jdbcTemplate;
    private final PlatformTransactionManager transactionManager;
    private TransactionStatus currentTransaction;
    private Long lastGeneratedKey = null;
    public DB(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
      this.jdbcTemplate = jdbcTemplate;
      this.transactionManager = transactionManager;
    }
    // ========================================
    // TRANSAZIONI
    // ========================================
    public void begin() {
      if (currentTransaction != null) {
        throw new IllegalStateException("Transaction already active");
      }
      currentTransaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
    }
    public void commit() {
      if (currentTransaction == null) {
        throw new IllegalStateException("No active transaction");
      }
      transactionManager.commit(currentTransaction);
      currentTransaction = null;
    }
    public void rollback() {
      if (currentTransaction == null) {
        throw new IllegalStateException("No active transaction");
      }
      transactionManager.rollback(currentTransaction);
      currentTransaction = null;
    }
    // ========================================
    // QUERY DML (INSERT/UPDATE/DELETE)
    // ========================================
    public int query(String sql, Object... params) {
      KeyHolder keyHolder = new GeneratedKeyHolder();
      int rows = jdbcTemplate.update(connection -> {
        PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < params.length; i++) {
          ps.setObject(i + 1, params[i]);
        }
        return ps;
      }, keyHolder);
      lastGeneratedKey = (keyHolder.getKey() != null) ? keyHolder.getKey().longValue() : null;
      return rows;
    }
    // ========================================
    // SELECT
    // ========================================
    public Recordset select(String sql, Object... params) {
      return new Recordset(jdbcTemplate.query(sql, params, (rs, rowNum) -> {
        Record record = new Record();
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        for (int i = 1; i <= colCount; i++) {
          record.put(meta.getColumnName(i), rs.getObject(i));
        }
        return record;
      }));
    }
    // ========================================
    // RESTITUISCE L'ULTIMA CHIAVE GENERATA
    // ========================================
    public Long lastInsertId() {
      return lastGeneratedKey;
    }
    // ========================================
    // CURSOR PER ITERAZIONE MEMORY-EFFICIENT
    // ========================================
    public Cursor cursor(String sql, Object... params) {
      return new Cursor(jdbcTemplate.getDataSource(), sql, params);
    }
    // ========================================
    // OTTIENE LE COLONNE DI UNA TABELLA
    // ========================================
    public Set<String> getTableColumns(String tableName) {
      return new HashSet<>(jdbcTemplate.query(
        "SELECT * FROM " + tableName + " WHERE 1=0",
        rs -> {
          ResultSetMetaData meta = rs.getMetaData();
          Set<String> columns = new HashSet<>();
          for (int i = 1; i <= meta.getColumnCount(); i++) {
            columns.add(meta.getColumnName(i).toLowerCase());
          }
          return columns;
        }
      ));
    }
    // ========================================
    // RECORD / RECORDSET
    // ========================================
    public static class Record extends HashMap<String, Object> { }
    public static class Recordset extends ArrayList<Record> {
      public Recordset(Collection<Record> records) {
        super(records);
      }
    }
    // ========================================
    // CURSOR
    // ========================================
    public static class Cursor implements AutoCloseable {
      private final java.sql.Connection connection;
      private final java.sql.PreparedStatement statement;
      private final java.sql.ResultSet resultSet;
      Cursor(javax.sql.DataSource dataSource, String sql, Object... params) {
        try {
          connection = dataSource.getConnection();
          statement = connection.prepareStatement(sql);
          for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
          }
          resultSet = statement.executeQuery();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
      public boolean next() throws SQLException {
        return resultSet.next();
      }
      public Object get(String column) throws SQLException {
        return resultSet.getObject(column);
      }
      public Record getRow() throws SQLException {
        Record record = new Record();
        ResultSetMetaData meta = resultSet.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
          record.put(meta.getColumnName(i), resultSet.getObject(i));
        }
        return record;
      }
      @Override
      public void close() {
        try {
          if (resultSet != null) resultSet.close();
        } catch (SQLException ignored) {}
        try {
          if (statement != null) statement.close();
        } catch (SQLException ignored) {}
        try {
          if (connection != null) connection.close();
        } catch (SQLException ignored) {}
      }
    }
    // ========================================
    // CONFIGURAZIONE SPRING
    // ========================================
    @Configuration
    public static class Config {
      @Bean
      public DB db(JdbcTemplate jdbcTemplate, PlatformTransactionManager txManager) {
        return new DB(jdbcTemplate, txManager);
      }
    }
  }


- [x] ~~il file tmp/cmd, proveniente da un diverso contesto applicativo, contiene
  delle procedure relative al database come l'accesso alla cli per sqlite3,
  mariadb e postgres, che dovrebbero essere importate nel comando
  /usr/src/app/bin/cmd locale e contetualizzate per questo progetto. Le stesse
  modifiche dovrebbero essere propagate nell'archetipo sovrascrivendo il file
  corrispondente in:
  /usr/src/app/.toolchain/archetype/src/main/resources/archetype-resources/bin/cmd.~~
  **RISOLTO**: Aggiunta funzione db_cli() con comandi `cmd db` e `cmd db -f <file>`
  - SQLite (default), MariaDB e PostgreSQL supportati
  - Propagato nell'archetype

- [x] ~~il comando cmd git branch senza opzioni deve restituire il nome del branch
  attivo corrente.~~
  **RISOLTO**: Modificata git_branch() per restituire il branch corrente se chiamata senza -b

- [x] ~~aggiungere il seguente subcommand a cmd: cmd git fetch.
  Il comando deve recuperare dal file .env il nome del progetto e la
  configurazione git ed eseguire la seguente sequenza:
  GIT_URL="https://$GIT_USER:$GIT_TOKEN@github.com/$GIT_USER/$PROJECT_NAME.git"
  git init
  git remote add origin "$GIT_URL"
  git fetch origin
  git reset --hard origin/main
  git branch -m master main
  git branch -u origin/main
  verificare la presenza di errori nella sequenza~~
  **RISOLTO**: Aggiunta git_fetch() che esegue solo in /usr/src/app e solo in assenza di .git

- [x] ~~il file .toolchain/archetype/src/main/resources/archetype-resources/.gitignore
  della app non viene copiato nella root del progetto durante l'installazione
  sovrascrivendo il file .gitignore del repo di origine.~~
  **RISOLTO**: Modificato install.sh per usare `mv -f` e forzare la sovrascrittura
  dei file nascosti incluso .gitignore (linee 589, 593).

- [x] ~~Modificare i nomi delle migration nella app e nell'archetipo in modo che ogni
  migration sia relativa a un modulo e sia il più possibile indipendente.~~
  **RISOLTO**: Adottato il formato V<timestamp>__module_<nome_modulo>.sql
  - Migration rinominata da V1__init_database.sql a V20260101_120000__module_status.sql
  - Il prefisso "module_" permette di distinguere migration modulari da quelle generiche
  - Il timestamp deve essere scritto manualmente dallo sviluppatore nel formato yyyymmdd_hhmmss
