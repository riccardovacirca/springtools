package dev.springtools.util;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utilità per gestire risposte HTTP con builder pattern.
 *
 * <p>Formato standard: {"err": boolean, "log": string|null, "out": object|null}
 *
 * <p>Content-Type default: application/json
 *
 * <p>Esempi:
 *
 * <pre>
 * // Risposta JSON (default)
 * return HttpResponse.create()
 *     .out(data)
 *     .build();
 *
 * // Risposta con errore
 * return HttpResponse.create()
 *     .err(true)
 *     .log("Not found")
 *     .status(HttpStatus.NOT_FOUND)
 *     .build();
 *
 * // Risposta XML (esplicito)
 * return HttpResponse.create()
 *     .out(data)
 *     .contentType("application/xml")
 *     .build();
 * </pre>
 */
public class HttpResponse {
  private boolean err = false;
  private String log = null;
  private Object out = null;
  private HttpStatus status = HttpStatus.OK;
  private String contentType = "application/json";
  private Map<String, String> headers = new HashMap<>();

  private HttpResponse() {}

  /** Crea una nuova istanza di HttpResponse */
  public static HttpResponse create() {
    return new HttpResponse();
  }

  /**
   * Imposta il flag di errore
   *
   * @param value true se errore, false altrimenti
   * @return this per chaining
   */
  public HttpResponse err(boolean value) {
    this.err = value;
    return this;
  }

  /**
   * Imposta il messaggio di log/errore
   *
   * @param message Messaggio (null se nessun messaggio)
   * @return this per chaining
   */
  public HttpResponse log(String message) {
    this.log = message;
    return this;
  }

  /**
   * Imposta i dati in output
   *
   * @param data Dati da restituire (null se nessun dato)
   * @return this per chaining
   */
  public HttpResponse out(Object data) {
    this.out = data;
    return this;
  }

  /**
   * Imposta lo status HTTP
   *
   * @param status HttpStatus (default: OK)
   * @return this per chaining
   */
  public HttpResponse status(HttpStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Imposta il Content-Type della risposta
   *
   * @param type Content-Type (default: application/json)
   * @return this per chaining
   */
  public HttpResponse contentType(String type) {
    this.contentType = type;
    return this;
  }

  /**
   * Aggiunge un header HTTP
   *
   * @param key Nome header
   * @param value Valore header
   * @return this per chaining
   */
  public HttpResponse header(String key, String value) {
    this.headers.put(key, value);
    return this;
  }

  /**
   * Costruisce la ResponseEntity con il body come Map.
   *
   * <p>Spring serializza automaticamente in base al Content-Type impostato (default:
   * application/json).
   *
   * @return ResponseEntity con body {"err": ..., "log": ..., "out": ...}
   */
  public ResponseEntity<Map<String, Object>> build() {
    Map<String, Object> body = new HashMap<>();
    body.put("err", err);
    body.put("log", log);
    body.put("out", out);

    var builder = ResponseEntity.status(status).header("Content-Type", contentType);
    for (var entry : headers.entrySet()) {
      builder = builder.header(entry.getKey(), entry.getValue());
    }
    return builder.body(body);
  }
}
