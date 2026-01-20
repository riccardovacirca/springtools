package ${package}.utils;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Utilità per gestire risposte HTTP servlet.
 * <p>
 * Fornisce metodi helper per operazioni comuni sulle HttpServletResponse.
 */
public class HttpResponse {

  /**
   * Imposta un header HTTP nella risposta.
   * <p>
   * Permette di aggiungere o sovrascrivere un header nella response HTTP.
   * Utile per impostare Content-Type, Cache-Control, CORS headers, ecc.
   *
   * @param res HttpServletResponse su cui impostare l'header
   * @param key Nome dell'header da impostare
   * @param value Valore dell'header
   */
  public static void setHeader(HttpServletResponse res, String key, String value) {
    res.setHeader(key, value);
  }

  /**
   * Imposta il Content-Type della risposta HTTP.
   *
   * @param res HttpServletResponse su cui impostare il Content-Type
   * @param contentType Content-Type da impostare (es: "application/json", "text/html")
   */
  public static void setContentType(HttpServletResponse res, String contentType) {
    res.setContentType(contentType);
  }

  /**
   * Imposta lo status code della risposta HTTP.
   *
   * @param res HttpServletResponse su cui impostare lo status
   * @param statusCode Codice HTTP (es: 200, 404, 500)
   */
  public static void setStatus(HttpServletResponse res, int statusCode) {
    res.setStatus(statusCode);
  }

  /**
   * Scrive il corpo della risposta HTTP.
   *
   * @param res HttpServletResponse su cui scrivere
   * @param body Contenuto da scrivere nella response
   * @throws IOException Se si verifica un errore durante la scrittura
   */
  public static void writeBody(HttpServletResponse res, String body) throws IOException {
    res.getWriter().write(body);
  }

  /**
   * Invia una risposta JSON di successo con status code 200.
   * <p>
   * Imposta automaticamente Content-Type a "application/json;charset=UTF-8"
   * e crea un body JSON nel formato standard: {"err": false, "log": null, "out": body}.
   *
   * @param res HttpServletResponse su cui scrivere
   * @param body Dati da inserire nel campo "out" del JSON (può essere null)
   * @throws IOException Se si verifica un errore durante la scrittura
   */
  public static void sendJsonSuccess(HttpServletResponse res, Object body) throws IOException {
    res.setContentType("application/json;charset=UTF-8");
    res.setStatus(200);

    // Costruisce manualmente la stringa JSON
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"err\":false,");
    json.append("\"log\":null,");
    json.append("\"out\":");

    if (body == null) {
      json.append("null");
    } else {
      // Serializza body usando JSON.encode()
      try {
        String encodedBody = JSON.encode(body);
        json.append(encodedBody);
      } catch (Exception e) {
        // Fallback: se body è una stringa, esegue escape manuale
        if (body instanceof String) {
          String escapedBody = ((String) body).replace("\\", "\\\\").replace("\"", "\\\"");
          json.append("\"").append(escapedBody).append("\"");
        } else {
          json.append("null");
        }
      }
    }

    json.append("}");

    res.getWriter().write(json.toString());
  }

  /**
   * Invia una risposta JSON per liste con metadata di paginazione.
   * <p>
   * Formato output:
   * {"err": false, "log": null, "out": {"offset": N, "limit": M, "total": T, "hasNext": bool, "items": [...]}}
   * <p>
   * Paginazione opzionale: offset=0 e limit=0 significa "tutti i record" (no paginazione).
   *
   * @param res HttpServletResponse su cui scrivere
   * @param items Lista di elementi da restituire
   * @param offset Posizione iniziale (0-based)
   * @param limit Numero massimo di elementi (0 = tutti i record)
   * @param total Numero totale di elementi disponibili
   * @throws IOException Se si verifica un errore durante la scrittura
   */
  public static void sendJsonList(HttpServletResponse res, java.util.List<?> items, int offset, int limit, int total) throws IOException {
    res.setContentType("application/json;charset=UTF-8");
    res.setStatus(200);

    // Calcola hasNext
    boolean hasNext = (limit > 0) && ((offset + limit) < total);

    // Costruisce JSON manualmente
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"err\":false,");
    json.append("\"log\":null,");
    json.append("\"out\":{");
    json.append("\"offset\":").append(offset).append(",");
    json.append("\"limit\":").append(limit).append(",");
    json.append("\"total\":").append(total).append(",");
    json.append("\"hasNext\":").append(hasNext).append(",");
    json.append("\"items\":");

    // Serializza items
    if (items == null || items.isEmpty()) {
      json.append("[]");
    } else {
      try {
        String encodedItems = JSON.encode(items);
        json.append(encodedItems);
      } catch (Exception e) {
        json.append("[]");
      }
    }

    json.append("}}");

    res.getWriter().write(json.toString());
  }

  /**
   * Invia una risposta JSON di errore con status code e messaggio.
   * <p>
   * Imposta automaticamente Content-Type a "application/json;charset=UTF-8"
   * e crea un body JSON nel formato standard: {"err": true, "log": message, "out": null}.
   *
   * @param res HttpServletResponse su cui scrivere
   * @param statusCode Codice HTTP di errore (es: 400, 404, 500)
   * @param message Messaggio di log/errore
   * @throws IOException Se si verifica un errore durante la scrittura
   */
  public static void sendJsonError(HttpServletResponse res, int statusCode, String message) throws IOException {
    res.setContentType("application/json;charset=UTF-8");
    res.setStatus(statusCode);

    // Costruisce manualmente la stringa JSON
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"err\":true,");
    json.append("\"log\":");

    if (message == null) {
      json.append("null");
    } else {
      // Escape delle virgolette e backslash nel messaggio
      String escapedLog = message.replace("\\", "\\\\").replace("\"", "\\\"");
      json.append("\"").append(escapedLog).append("\"");
    }

    json.append(",");
    json.append("\"out\":null");
    json.append("}");

    res.getWriter().write(json.toString());
  }

  /**
   * Abilita CORS (Cross-Origin Resource Sharing) sulla risposta HTTP.
   * <p>
   * Imposta gli header necessari per permettere richieste cross-origin:
   * - Access-Control-Allow-Origin: *
   * - Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
   * - Access-Control-Allow-Headers: Content-Type, Authorization
   * - Access-Control-Max-Age: 3600
   *
   * @param res HttpServletResponse su cui impostare gli header CORS
   */
  public static void enableCors(HttpServletResponse res) {
    res.setHeader("Access-Control-Allow-Origin", "*");
    res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    res.setHeader("Access-Control-Max-Age", "3600");
  }

  /**
   * Disabilita CORS (Cross-Origin Resource Sharing) sulla risposta HTTP.
   * <p>
   * Imposta header CORS restrittivi che bloccano richieste cross-origin:
   * - Access-Control-Allow-Origin: null
   *
   * @param res HttpServletResponse su cui impostare gli header CORS restrittivi
   */
  public static void disableCors(HttpServletResponse res) {
    res.setHeader("Access-Control-Allow-Origin", "null");
  }
}
