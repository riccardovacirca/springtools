# HttpRequest - Guida all'uso

## Panoramica

`HttpRequest` è una classe di utility della libreria `dev.springtools.util` che fornisce metodi helper per semplificare le operazioni comuni sulle richieste HTTP servlet in un contesto Spring.

## Caso d'uso: Endpoint REST con gestione dati della richiesta

### Scenario

Creare un endpoint REST che accetta dati da diverse sorgenti (query string per GET, JSON body per POST) e li elabora in modo uniforme.

### Implementazione

```java
package dev.example.controller;

import dev.springtools.util.HttpRequest;
import dev.springtools.util.HttpResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

  @PostMapping
  public ResponseEntity<Map<String, Object>> createUser(HttpServletRequest req)
      throws Exception {

    // Estrazione automatica dei dati dalla richiesta
    // - GET: query string parameters
    // - POST/PUT: JSON body deserializzato
    Map<String, Object> data = HttpRequest.getRequestData(req);

    // Accesso ai dati tipizzati
    String name = (String) data.get("name");
    Integer age = (Integer) data.get("age");
    Boolean active = (Boolean) data.get("active");

    // Validazione
    if (name == null || name.trim().isEmpty()) {
      ResponseEntity<Map<String, Object>> response =
          HttpResponse.create()
              .err(true)
              .log("Nome utente richiesto")
              .contentType("application/json")
              .build();
      return response;
    }

    // Logica di business
    Map<String, Object> user =
        Map.of(
            "id", 123L,
            "name", name,
            "age", age != null ? age : 0,
            "active", active != null ? active : true);

    // Risposta di successo
    ResponseEntity<Map<String, Object>> response =
        HttpResponse.create().out(user).contentType("application/json").build();
    return response;
  }

  @GetMapping("/search")
  public ResponseEntity<Map<String, Object>> searchUsers(HttpServletRequest req) {

    // Estrazione parametri query string
    Map<String, String> params = HttpRequest.getQueryParams(req);

    String query = params.get("q");
    int page = HttpRequest.getIntParam(req, "page", 1);
    int limit = HttpRequest.getIntParam(req, "limit", 10);

    // Calcolo offset per paginazione
    int offset = (page - 1) * limit;

    // Simulazione ricerca
    Map<String, Object> results =
        Map.of(
            "query", query != null ? query : "",
            "page", page,
            "limit", limit,
            "offset", offset,
            "total", 0,
            "items", java.util.List.of());

    ResponseEntity<Map<String, Object>> response =
        HttpResponse.create().out(results).contentType("application/json").build();
    return response;
  }

  @PostMapping("/upload")
  public ResponseEntity<Map<String, Object>> uploadAvatar(HttpServletRequest req)
      throws Exception {

    // Estrazione file caricati (multipart/form-data)
    long maxSize = 5 * 1024 * 1024; // 5MB
    var files = HttpRequest.getUploadedFiles(req, maxSize);

    // Estrazione campi text dal multipart
    Map<String, String> fields = HttpRequest.getMultipartFields(req);
    String userId = fields.get("userId");

    if (files.isEmpty()) {
      ResponseEntity<Map<String, Object>> response =
          HttpResponse.create()
              .err(true)
              .log("Nessun file caricato")
              .contentType("application/json")
              .build();
      return response;
    }

    var fileInfo = files.get(0);
    Map<String, Object> result =
        Map.of(
            "userId", userId,
            "filename", fileInfo.get("filename"),
            "size", fileInfo.get("size"),
            "path", fileInfo.get("path"));

    ResponseEntity<Map<String, Object>> response =
        HttpResponse.create().out(result).contentType("application/json").build();
    return response;
  }

  @GetMapping("/info")
  public ResponseEntity<Map<String, Object>> getUserInfo(HttpServletRequest req) {

    // Accesso a informazioni della richiesta
    String contentType = HttpRequest.getContentType(req);
    String userAgent = HttpRequest.getHeader(req, "User-Agent");
    Map<String, String> allHeaders = HttpRequest.getHeaders(req);

    Map<String, Object> info =
        Map.of(
            "contentType", contentType != null ? contentType : "N/A",
            "userAgent", userAgent != null ? userAgent : "N/A",
            "headerCount", allHeaders.size());

    ResponseEntity<Map<String, Object>> response =
        HttpResponse.create().out(info).contentType("application/json").build();
    return response;
  }
}
```

## Metodi principali

### Estrazione dati

- **`getRequestData(req)`** - Estrae automaticamente dati da query string (GET) o JSON body (POST/PUT/PATCH/DELETE)
- **`getQueryParams(req)`** - Estrae tutti i parametri dalla query string come Map
- **`getArgs(req)`** - Alias di getQueryParams

### Parsing parametri

- **`getIntParam(req, name, defaultValue)`** - Estrae parametro intero con valore di default
- **`getLongParam(req, name, defaultValue)`** - Estrae parametro long con valore di default

### Upload file

- **`getUploadedFiles(req, maxSize)`** - Gestisce upload multipart, salva in `/workspace/uploads/`
- **`getMultipartFields(req)`** - Estrae campi text da richiesta multipart

### Informazioni richiesta

- **`getBody(req)`** - Legge il corpo della richiesta come stringa
- **`getHeaders(req)`** - Ottiene tutti gli header HTTP
- **`getHeader(req, key)`** - Ottiene un header specifico
- **`getContentType(req)`** - Ottiene il Content-Type della richiesta

## Vantaggi

1. **Semplicità**: API uniforme per accedere ai dati della richiesta
2. **Type-safe**: Metodi helper per parsing sicuro dei tipi
3. **Compatibilità**: Funziona con qualsiasi endpoint Spring
4. **Esplicito**: Rende chiaro da dove provengono i dati (query string vs body)
5. **Gestione errori**: Parsing robusto con valori di default

## Note

- I file uploadati vengono salvati in `/workspace/uploads/`
- Il parsing JSON usa Jackson internamente
- I parametri duplicati nella query string prendono solo il primo valore
- Per file CSV, usare il modulo `excel` della libreria util
