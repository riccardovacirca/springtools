package dev.springtools.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilità per gestire richieste HTTP servlet.
 * <p>
 * Fornisce metodi helper per operazioni comuni sulle HttpServletRequest.
 */
public class HttpRequest {

  /**
   * Legge il corpo della richiesta HTTP come stringa.
   * <p>
   * Utilizzato tipicamente per estrarre JSON body da richieste POST/PUT.
   *
   * @param req HttpServletRequest da cui leggere il body
   * @return Corpo della richiesta come stringa
   * @throws IOException Se si verifica un errore durante la lettura
   */
  public static String getBody(HttpServletRequest req) throws IOException {
    StringBuilder jsonBuilder = new StringBuilder();
    BufferedReader reader = req.getReader();
    String line;
    while ((line = reader.readLine()) != null) {
      jsonBuilder.append(line);
    }
    return jsonBuilder.toString();
  }

  /**
   * Restituisce tutti gli header HTTP della richiesta.
   *
   * @param req HttpServletRequest da cui estrarre gli header
   * @return Map con nome header e valore
   */
  public static Map<String, String> getHeaders(HttpServletRequest req) {
    Map<String, String> headers = new HashMap<>();
    Enumeration<String> headerNames = req.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String name = headerNames.nextElement();
      headers.put(name, req.getHeader(name));
    }
    return headers;
  }

  /**
   * Restituisce il valore di un header HTTP specifico.
   *
   * @param req HttpServletRequest da cui estrarre l'header
   * @param key Nome dell'header
   * @return Valore dell'header o null se non esiste
   */
  public static String getHeader(HttpServletRequest req, String key) {
    return req.getHeader(key);
  }

  /**
   * Restituisce il Content-Type della richiesta HTTP.
   *
   * @param req HttpServletRequest da cui estrarre il Content-Type
   * @return Content-Type della richiesta o null se non presente
   */
  public static String getContentType(HttpServletRequest req) {
    return req.getContentType();
  }

  /**
   * Restituisce la dimensione del body della richiesta in bytes.
   *
   * @param req HttpServletRequest
   * @return Dimensione in bytes o -1 se non disponibile
   */
  public static int getSize(HttpServletRequest req) {
    return req.getContentLength();
  }

  /**
   * Restituisce la query string della richiesta.
   *
   * @param req HttpServletRequest
   * @return Query string o null se non presente
   */
  public static String getQueryString(HttpServletRequest req) {
    return req.getQueryString();
  }

  /**
   * Restituisce i parametri della query string come mappa.
   *
   * @param req HttpServletRequest
   * @return Map con parametri query string
   */
  public static Map<String, String> getArgs(HttpServletRequest req) {
    Map<String, String> args = new HashMap<>();
    Map<String, String[]> parameterMap = req.getParameterMap();
    for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
      if (entry.getValue() != null && entry.getValue().length > 0) {
        args.put(entry.getKey(), entry.getValue()[0]);
      }
    }
    return args;
  }

  /**
   * Restituisce il protocollo HTTP della richiesta.
   *
   * @param req HttpServletRequest
   * @return Protocollo (es: "HTTP/1.1")
   */
  public static String getProtocol(HttpServletRequest req) {
    return req.getProtocol();
  }

  /**
   * Estrae tutti i query parameters dalla request per il supporto di filtri dinamici.
   * <p>
   * Utilizzato per il supporto automatico di filtri dinamici negli endpoint collection.
   * Automaticamente estrae tutti i parametri ?key=value dalla URL query string.
   * <p>
   * Esempio:
   * <pre>
   *   GET /api/users?name=John&status=active
   *   Map&lt;String, String&gt; params = HttpRequest.getQueryParams(req);
   *   → { "name": "John", "status": "active" }
   * </pre>
   * <p>
   * Se ci sono parametri duplicati (es: ?tag=java&tag=web), viene preso solo
   * il primo valore. Parametri vuoti vengono ignorati.
   *
   * @param req HttpServletRequest da cui estrarre i query parameters
   * @return Map con tutti i parametri della query string (chiave=valore)
   *
   * @see #getArgs(HttpServletRequest)
   */
  public static Map<String, String> getQueryParams(HttpServletRequest req) {
    Map<String, String> params = new HashMap<>();
    Map<String, String[]> parameterMap = req.getParameterMap();

    for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
      String key = entry.getKey();
      String[] values = entry.getValue();

      // Prendi solo il primo valore se ci sono duplicati
      if (values != null && values.length > 0 && values[0] != null && !values[0].isEmpty()) {
        params.put(key, values[0]);
      }
    }

    return params;
  }

  /**
   * Gestisce l'upload di file da richieste multipart.
   * Salva i file in /workspace/uploads/ se non superano il limite di dimensione.
   *
   * @param req HttpServletRequest
   * @param maxSize Dimensione massima in bytes per file
   * @return Lista di Map con attributi dei file caricati
   * @throws Exception Se si verifica un errore durante l'upload
   */
  public static List<Map<String, Object>> getUploadedFiles(HttpServletRequest req, long maxSize) throws Exception {
    List<Map<String, Object>> files = new ArrayList<>();
    String uploadDir = "/workspace/uploads/";

    // Crea directory se non esiste
    Files.createDirectories(Paths.get(uploadDir));

    Collection<Part> parts = req.getParts();
    for (Part part : parts) {
      String filename = part.getSubmittedFileName();

      // Solo file (non campi text)
      if (filename != null && !filename.isEmpty()) {
        long size = part.getSize();

        // Ignora file che superano il limite
        if (size <= maxSize) {
          Map<String, Object> fileInfo = new HashMap<>();
          fileInfo.put("fieldName", part.getName());
          fileInfo.put("filename", filename);
          fileInfo.put("contentType", part.getContentType());
          fileInfo.put("size", size);

          String filePath = uploadDir + filename;
          part.write(filePath);
          fileInfo.put("path", filePath);

          files.add(fileInfo);
        }
      }
    }

    return files;
  }

  /**
   * Estrae i campi text da una richiesta multipart.
   *
   * @param req HttpServletRequest
   * @return Map con nome campo e valore
   * @throws Exception Se si verifica un errore durante la lettura
   */
  public static Map<String, String> getMultipartFields(HttpServletRequest req) throws Exception {
    Map<String, String> fields = new HashMap<>();

    Collection<Part> parts = req.getParts();
    for (Part part : parts) {
      String filename = part.getSubmittedFileName();

      // Solo campi text (non file)
      if (filename == null || filename.isEmpty()) {
        String fieldName = part.getName();
        String value = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        fields.put(fieldName, value);
      }
    }

    return fields;
  }

  /**
   * Estrae i dati dalla richiesta HTTP e li restituisce come Map.
   * <p>
   * Gestisce automaticamente tutti i metodi HTTP (GET, POST, PUT, PATCH, DELETE)
   * estraendo i dati dalla sorgente appropriata:
   * <ul>
   *   <li>GET: query string parameters</li>
   *   <li>POST/PUT/PATCH/DELETE: JSON body deserializzato</li>
   * </ul>
   * <p>
   * Mappatura tipi JSON su Object:
   * <ul>
   *   <li>JSON string → String</li>
   *   <li>JSON number (int) → Integer</li>
   *   <li>JSON number (float) → Double</li>
   *   <li>JSON boolean → Boolean</li>
   *   <li>JSON array → ArrayList</li>
   *   <li>JSON object → LinkedHashMap</li>
   *   <li>JSON null → null</li>
   * </ul>
   * <p>
   * Esempio JSON:
   * <pre>
   * {
   *   "name": "Mario",
   *   "age": 30,
   *   "active": true,
   *   "tags": ["dev", "java"],
   *   "address": {"city": "Roma"}
   * }
   * </pre>
   * <p>
   * Estrazione valori:
   * <pre>
   * Map&lt;String, Object&gt; data = HttpRequest.getRequestData(req);
   * String name = (String) data.get("name");
   * Integer age = (Integer) data.get("age");
   * Boolean active = (Boolean) data.get("active");
   * ArrayList tags = (ArrayList) data.get("tags");
   * LinkedHashMap address = (LinkedHashMap) data.get("address");
   * </pre>
   *
   * @param req HttpServletRequest da cui estrarre i dati
   * @return Map con i dati della richiesta
   * @throws Exception Se si verifica un errore durante l'estrazione o la deserializzazione
   *
   * @see #getBody(HttpServletRequest)
   * @see #getArgs(HttpServletRequest)
   */
  public static Map<String, Object> getRequestData(HttpServletRequest req) throws Exception {
    String method = req.getMethod().toUpperCase();

    switch (method) {
      case "GET":
        // GET: converte parametri query string in Map<String, Object>
        Map<String, String> params = getArgs(req);
        Map<String, Object> result = new HashMap<>();
        result.putAll(params);
        return result;

      case "POST":
      case "PUT":
      case "PATCH":
      case "DELETE":
        // POST/PUT/PATCH/DELETE: legge JSON dal body
        String jsonData = getBody(req);

        // Se il body è vuoto, usa i parametri (fallback per form-urlencoded)
        if (jsonData == null || jsonData.trim().isEmpty()) {
          Map<String, String> formParams = getArgs(req);
          Map<String, Object> formResult = new HashMap<>();
          formResult.putAll(formParams);
          return formResult;
        }

        // Deserializza JSON in Map<String, Object>
        return JSON.decode(jsonData, Map.class);

      default:
        throw new IllegalArgumentException("Metodo HTTP non supportato: " + method);
    }
  }

  /**
   * Estrae un parametro intero dalla query string o dai parametri della richiesta.
   * <p>
   * Utilizzato per paginazione e parametri numerici. Restituisce un valore di default
   * se il parametro non è presente o non è un numero valido.
   *
   * @param req HttpServletRequest da cui estrarre il parametro
   * @param name Nome del parametro
   * @param defaultValue Valore di default se il parametro non esiste o è invalido
   * @return Valore intero del parametro o defaultValue
   */
  public static int getIntParam(HttpServletRequest req, String name, int defaultValue) {
    String value = req.getParameter(name);
    if (value == null || value.trim().isEmpty()) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Estrae un parametro long dalla query string o dai parametri della richiesta.
   * <p>
   * Utilizzato per ID e parametri numerici lunghi. Restituisce un valore di default
   * se il parametro non è presente o non è un numero valido.
   *
   * @param req HttpServletRequest da cui estrarre il parametro
   * @param name Nome del parametro
   * @param defaultValue Valore di default se il parametro non esiste o è invalido
   * @return Valore long del parametro o defaultValue
   */
  public static long getLongParam(HttpServletRequest req, String name, long defaultValue) {
    String value = req.getParameter(name);
    if (value == null || value.trim().isEmpty()) {
      return defaultValue;
    }
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
