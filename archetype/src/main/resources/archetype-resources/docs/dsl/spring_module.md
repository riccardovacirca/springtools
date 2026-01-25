MODULE_STYLE layered_spring_architecture

# ARCHITECTURE

PRINCIPLE: Separazione delle responsabilità tra layer con flusso unidirezionale Controller -> Service -> Dao

LAYERS:
  - Levels: controller -> service -> dao -> dto
  - Direction: top-down (unidirezionale)
  - Rationale: Ogni layer ha responsabilità specifiche e comunica solo con il layer adiacente

DATABASE:
  - Type: SQLite3 (default), MariaDB, PostgreSQL
  - Access: JDBC tramite libreria interna dev.crm.util.DB
  - Migration: Flyway
  - Rationale: Accesso diretto JDBC per performance e controllo, no ORM

# CODE FORMATTING

INDENTATION:
  - Size: 2 spaces
  - Never use tabs
  - Rule: Consistent 2-space indentation throughout

BRACES:
  - Class opening brace: Same line as class declaration
  - Method opening brace: Same line as method signature or throws clause
  - Rule: Opening brace always on the same line

METHOD PARAMETERS:
  - First parameter: Same line as method name
  - Other parameters: One per line, aligned in column with first parameter
  - Closing parenthesis: Same line as last parameter
  - Throws clause: Aligned in column with last parameter

EXAMPLES:
  Single parameter:
    public void method(String param) {

  Multiple parameters:
    public void method(String first,
                       String second,
                       int third) {

  With throws:
    public void method(String first,
                       String second)
                       throws Exception {

  With annotations:
    @GetMapping
    public Map<String, Object> findAll(@RequestParam(defaultValue = "50") int limit,
                                       @RequestParam(defaultValue = "0") int offset)
                                       throws Exception {

SPACING:
  - After comma: one space
  - Around operators: one space on each side
  - After keywords: one space (if, for, while, catch)

FUNCTION CALLS:
  Single argument:
    functionName(argument)

  Few arguments:
    functionName(arg1, arg2, arg3)

  Many arguments:
    - Opening parenthesis on same line as function name
    - Arguments on following lines, indented
    - Closing parenthesis aligned with arguments
    - For SQL queries, string concatenation indented further to show structure

  Example:
    db.query(
      "INSERT INTO contatti (" +
        "nome, cognome, ragione_sociale, telefono, email, " +
        "indirizzo, citta, cap, provincia, note, stato, consenso, blacklist, created_at" +
      ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
      dto.nome,
      dto.cognome,
      dto.ragioneSociale,
      dto.telefono,
      dto.email,
      dto.indirizzo,
      dto.citta,
      dto.cap,
      dto.provincia,
      dto.note,
      dto.stato != null ? dto.stato : 1,
      dto.consenso != null ? dto.consenso : false,
      dto.blacklist != null ? dto.blacklist : false,
      DB.toSqlTimestamp(LocalDateTime.now())
    );

# DIRECTORY STRUCTURE

```
src/main/java/dev/crm/module/<module_name>/
├── controller/
│   └── <Entity>Controller.java        # Espone endpoint REST
├── service/
│   └── <Entity>Service.java           # Logica di business
├── dao/
│   └── <Entity>Dao.java               # Accesso dati JDBC
└── dto/
    └── <Entity>Dto.java               # Data Transfer Object

src/main/resources/db/migration/
└── V<YYYYMMDD_HHMMSS>__module_<module_name>.sql
```

# NAMING CONVENTIONS

MODULE:
  - Case: snake_case
  - Examples: contatti, liste, campagne, operatori

PACKAGES:
  - Case: lowercase singular
  - Fixed names: controller, service, dao, dto

CLASSES:
  - Controller: <Entity>Controller (PascalCase)
    Example: ContattoController
  - Service: <Entity>Service (PascalCase)
    Example: ContattoService
  - Dao: <Entity>Dao (PascalCase)
    Example: ContattoDao
  - Dto: <Entity>Dto (PascalCase)
    Example: ContattoDto

DATABASE:
  - Tables: snake_case plural
    Example: contatti
  - Columns: snake_case
    Examples: nome, cognome, ragione_sociale, created_at

# CONTROLLER LAYER

ANNOTATIONS:
  - @RestController
  - @RequestMapping("/api/<module>")

RESPONSIBILITY:
  Allowed:
    - Esporre endpoint REST
    - Validare parametri request
    - Chiamare metodi service
    - Formattare response HTTP
    - Gestire ResponseEntity
    - Mappare exception a status code

  Forbidden:
    - Logica di business
    - Accesso diretto al database
    - Chiamate dirette ai Dao
    - Manipolazione complessa dei dati

DEPENDENCIES:
  - Required: Service (via constructor injection)
  - Forbidden: Dao diretto

STANDARD ENDPOINTS:
  - GET / (list)
    Params: limit, offset
    Response: Map { data, total, limit, offset }

  - GET /{id} (get)
    Response: ResponseEntity<Dto>

  - GET /search (search)
    Params: q (query string), limit
    Response: List<Dto>

  - POST / (create)
    Body: Dto
    Response: Dto

  - PUT /{id} (update)
    Body: Dto
    Response: Dto

  - DELETE /{id} (delete)
    Response: ResponseEntity.noContent()

# SERVICE LAYER

ANNOTATIONS:
  - @Service

RESPONSIBILITY:
  Required:
    - Logica di business
    - Coordinazione tra Dao
    - Validazione business rules
    - Gestione transazioni
    - Orchestrazione operazioni complesse

  Forbidden:
    - Accesso diretto JDBC
    - Costruzione query SQL
    - Gestione connessioni database
    - Logica di presentazione/HTTP

DEPENDENCIES:
  - Required: Dao (via constructor injection)
  - Optional: Altri Service per operazioni complesse

EXCEPTION HANDLING:
  - Pattern: throw Exception con messaggio descrittivo
  - Purpose: Controller gestisce mapping a HTTP status

# DAO LAYER

ANNOTATIONS:
  - @Repository

RESPONSIBILITY:
  Required:
    - Eseguire query SQL
    - Gestire connessioni JDBC tramite DB utility
    - Mappare Record a Dto
    - CRUD operations
    - Query complesse

  Forbidden:
    - Logica di business
    - Validazione business rules
    - Coordinazione tra entità diverse

DEPENDENCIES:
  - Required: javax.sql.DataSource (via constructor injection)
  - Utility: dev.crm.util.DB

PATTERN:
  Connection: DB db = new DB(dataSource)
  Lifecycle: try { db.open(); ... } finally { db.close(); }
  Insert: db.query() + db.lastInsertId()
  Select: db.select() returns Recordset
  Update/Delete: db.query() returns affected rows
  Mapping: private <Entity>Dto mapRecord(Record r)

DB UTILITY METHODS:
  Conversion:
    - DB.toLong(Object)
    - DB.toString(Object)
    - DB.toInteger(Object)
    - DB.toBoolean(Object)
    - DB.toLocalDateTime(Object)
    - DB.toSqlTimestamp(LocalDateTime)

  Query:
    - db.query(sql, params...)
    - db.select(sql, params...)
    - db.lastInsertId()

# DTO LAYER

ANNOTATIONS: None (POJO)

RESPONSIBILITY:
  Required:
    - Rappresentare dati trasferiti tra layer
    - Campi pubblici per semplicità
    - Costruttore vuoto e costruttore con parametri
    - Metodi utility opzionali (es. getDisplayName)

  Forbidden:
    - Logica di business
    - Dipendenze da altri layer
    - Annotazioni JPA/Hibernate
    - Getter/Setter (usare campi pubblici)

FIELD CONVENTIONS:
  - Primary key: Long id
  - Timestamps: LocalDateTime createdAt, updatedAt
  - Visibility: public
  - Naming: camelCase

# MIGRATION RULES

NAMING CONVENTION:
  - Pattern: V<YYYYMMDD_HHMMSS>__module_<module_name>.sql
  - Timestamp format: YYYYMMDD_HHMMSS
  - Separator: double underscore
  - Description: module_<name>
  - Example: V20260101_120002__module_contatti.sql

CONTENT STRUCTURE:
  Order:
    1. CREATE TABLE statements
    2. CREATE INDEX statements
    3. Initial data (optional)

TABLE CONVENTIONS:
  - Primary key: id INTEGER PRIMARY KEY AUTOINCREMENT
  - Timestamps: created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP
  - Required fields: id, created_at
  - Optional fields: updated_at

INDEX CONVENTIONS:
  - Naming: idx_<table>_<column>
  - Common indexes:
    * Foreign keys
    * Search fields
    * Status/state fields
    * Timestamp fields for sorting

# DEPENDENCY INJECTION

PATTERN: Constructor injection

Example:
```java
public ContattoController(ContattoService service) {
    this.service = service;
}
```

RATIONALE: Immutability, testability, esplicita dipendenza

# ERROR HANDLING

DAO LAYER: throw Exception con messaggio tecnico
SERVICE LAYER: throw Exception con messaggio business
CONTROLLER LAYER: catch e mappa a ResponseEntity con status appropriato

# DATA FLOW

REQUEST FLOW:
  HTTP Request -> Controller -> Service -> Dao -> Database

RESPONSE FLOW:
  Database -> Record -> Dto (via mapRecord) -> Service -> Controller -> HTTP Response

PATTERN: Unidirezionale top-down, ogni layer comunica solo con il successivo

# ANTI-PATTERNS

FORBIDDEN:
  ✗ Controller to Dao
    Example: Controller che chiama direttamente Dao
    Reason: Bypassa logica business del Service

  ✗ Business logic in Controller
    Example: Validazioni complesse o manipolazioni dati nel Controller
    Reason: Controller deve solo gestire HTTP, non business logic

  ✗ SQL in Service
    Example: Service che costruisce query SQL
    Reason: SQL compete al Dao

  ✗ Dto with logic
    Example: Dto con metodi che eseguono business logic
    Reason: Dto deve essere un semplice contenitore dati

  ✗ Connection leak
    Example: db.open() senza db.close() in finally
    Reason: Causa esaurimento connessioni

  ✗ Field injection
    Example: @Autowired private Service service;
    Reason: Preferire constructor injection per immutability

  ✗ Generic exceptions
    Example: catch (Exception e) senza re-throw o logging
    Reason: Nasconde errori, rende debugging difficile

  ✗ Hardcoded values
    Example: Valori magici nel codice invece che costanti
    Reason: Difficile manutenzione e testing

# EXAMPLES

## Controller

File: src/main/java/dev/crm/module/contatti/controller/ContattoController.java
```java
@RestController
@RequestMapping("/api/contatti")
public class ContattoController {
  private final ContattoService service;

  public ContattoController(ContattoService service) {
    this.service = service;
  }

  @GetMapping
  public Map<String, Object> findAll(@RequestParam(defaultValue = "50") int limit,
                                     @RequestParam(defaultValue = "0") int offset)
                                     throws Exception {
    Map<String, Object> result = new HashMap<>();
    result.put("data", service.findAll(limit, offset));
    result.put("total", service.count());
    result.put("limit", limit);
    result.put("offset", offset);
    return result;
  }

  @GetMapping("/{id}")
  public ResponseEntity<ContattoDto> findById(@PathVariable Long id)
                                              throws Exception {
    return service.findById(id)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ContattoDto create(@RequestBody ContattoDto dto)
                            throws Exception {
    return service.create(dto);
  }

  @PutMapping("/{id}")
  public ContattoDto update(@PathVariable Long id,
                            @RequestBody ContattoDto dto)
                            throws Exception {
    dto.id = id;
    return service.update(dto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id)
                                     throws Exception {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
```

## Service

File: src/main/java/dev/crm/module/contatti/service/ContattoService.java
```java
@Service
public class ContattoService {
  private final ContattoDao dao;

  public ContattoService(ContattoDao dao) {
    this.dao = dao;
  }

  public ContattoDto create(ContattoDto dto)
                            throws Exception {
    long id = dao.insert(dto);
    return dao.findById(id)
      .orElseThrow(() -> new Exception("Contatto non trovato dopo creazione"));
  }

  public ContattoDto update(ContattoDto dto)
                            throws Exception {
    dao.update(dto);
    return dao.findById(dto.id)
      .orElseThrow(() -> new Exception("Contatto non trovato"));
  }

  public void delete(Long id)
                     throws Exception {
    dao.delete(id);
  }

  public Optional<ContattoDto> findById(Long id)
                                        throws Exception {
    return dao.findById(id);
  }

  public List<ContattoDto> findAll(int limit,
                                   int offset)
                                   throws Exception {
    return dao.findAll(limit, offset);
  }

  public int count()
                   throws Exception {
    return dao.count();
  }
}
```

## Dao

File: src/main/java/dev/crm/module/contatti/dao/ContattoDao.java
```java
@Repository
public class ContattoDao {
  private final DataSource dataSource;

  public ContattoDao(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public long insert(ContattoDto dto)
                     throws Exception {
    DB db = new DB(dataSource);
    try {
      db.open();
      db.query(
        "INSERT INTO contatti (" +
          "nome, cognome, telefono, created_at" +
        ") VALUES (?, ?, ?, ?)",
        dto.nome,
        dto.cognome,
        dto.telefono,
        DB.toSqlTimestamp(LocalDateTime.now())
      );
      return db.lastInsertId();
    } finally {
      db.close();
    }
  }

  public Optional<ContattoDto> findById(Long id)
                                        throws Exception {
    DB db = new DB(dataSource);
    try {
      db.open();
      Recordset rs = db.select(
        "SELECT * FROM contatti WHERE id = ?",
        id
      );
      if (rs.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(mapRecord(rs.get(0)));
    } finally {
      db.close();
    }
  }

  public List<ContattoDto> findAll(int limit,
                                   int offset)
                                   throws Exception {
    DB db = new DB(dataSource);
    List<ContattoDto> result = new ArrayList<>();
    try {
      db.open();
      Recordset rs = db.select(
        "SELECT * FROM contatti ORDER BY id DESC LIMIT ? OFFSET ?",
        limit,
        offset
      );
      for (Record r : rs) {
        result.add(mapRecord(r));
      }
      return result;
    } finally {
      db.close();
    }
  }

  private ContattoDto mapRecord(Record r) {
    return new ContattoDto(
      DB.toLong(r.get("id")),
      DB.toString(r.get("nome")),
      DB.toString(r.get("cognome")),
      DB.toString(r.get("telefono")),
      DB.toLocalDateTime(r.get("created_at")),
      DB.toLocalDateTime(r.get("updated_at"))
    );
  }
}
```

## Dto

File: src/main/java/dev/crm/module/contatti/dto/ContattoDto.java
```java
public class ContattoDto {
  public Long id;
  public String nome;
  public String cognome;
  public String telefono;
  public LocalDateTime createdAt;
  public LocalDateTime updatedAt;

  public ContattoDto() {}

  public ContattoDto(Long id,
                     String nome,
                     String cognome,
                     String telefono,
                     LocalDateTime createdAt,
                     LocalDateTime updatedAt) {
    this.id = id;
    this.nome = nome;
    this.cognome = cognome;
    this.telefono = telefono;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }
}
```

## Migration

File: src/main/resources/db/migration/V20260101_120002__module_contatti.sql
```sql
-- Tabella contatti
CREATE TABLE IF NOT EXISTS contatti (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome VARCHAR(100),
    cognome VARCHAR(100),
    telefono VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Indici
CREATE INDEX IF NOT EXISTS idx_contatti_telefono ON contatti(telefono);
CREATE INDEX IF NOT EXISTS idx_contatti_cognome ON contatti(cognome);
```

# SIMPLE MODULE EXAMPLE

```
src/main/java/dev/crm/module/contatti/
├── controller/
│   └── ContattoController.java
├── service/
│   └── ContattoService.java
├── dao/
│   └── ContattoDao.java
└── dto/
    └── ContattoDto.java

src/main/resources/db/migration/
└── V20260101_120002__module_contatti.sql
```

# COMPLEX MODULE EXAMPLE (Multiple Entities)

```
src/main/java/dev/crm/module/agende/
├── controller/
│   ├── PromemoriaController.java
│   ├── RichiamoController.java
│   └── StoricoRichiamoController.java
├── service/
│   ├── PromemoriaService.java
│   ├── RichiamoService.java
│   └── StoricoRichiamoService.java
├── dao/
│   ├── PromemoriaDao.java
│   ├── RichiamoDao.java
│   └── StoricoRichiamoDao.java
└── dto/
    ├── PromemoriaDto.java
    ├── RichiamoDto.java
    └── StoricoRichiamoDto.java

src/main/resources/db/migration/
└── V20260101_120006__module_agende.sql
```

# DEPENDENCY CHAIN

```
HTTP Request
  └── ContattoController (REST endpoints)
        └── ContattoService (business logic)
              └── ContattoDao (database access)
                    └── DB utility (JDBC wrapper)
                          └── DataSource (connection pool)
                                └── Database (SQLite/MariaDB/PostgreSQL)
```

# MODULE LIFECYCLE

1. **Migration**: Flyway esegue V<timestamp>__module_<name>.sql
2. **Spring Boot**: Carica @Repository, @Service, @RestController
3. **Dependency Injection**: Autowired via constructor injection
4. **Request Handling**:
   - Client -> Controller endpoint
   - Controller -> Service method
   - Service -> Dao query
   - Dao -> DB utility -> Database
   - Response flow inverso

# COMPLETE MODULE CHECKLIST

Before considering a module complete, verify:

□ Controller:
  - @RestController and @RequestMapping present
  - Constructor injection of Service
  - Standard CRUD endpoints implemented
  - Proper ResponseEntity usage

□ Service:
  - @Service annotation present
  - Constructor injection of Dao
  - Business logic separated from data access
  - Proper exception messages

□ Dao:
  - @Repository annotation present
  - Constructor injection of DataSource
  - All queries use try-finally with db.close()
  - Private mapRecord() method present
  - Proper use of DB utility methods

□ Dto:
  - Public fields (no getters/setters)
  - Empty constructor and full constructor
  - Proper field types (Long id, LocalDateTime timestamps)

□ Migration:
  - Correct naming: V<YYYYMMDD_HHMMSS>__module_<name>.sql
  - Primary key with AUTOINCREMENT
  - created_at TIMESTAMP NOT NULL
  - Indexes on foreign keys and search fields
