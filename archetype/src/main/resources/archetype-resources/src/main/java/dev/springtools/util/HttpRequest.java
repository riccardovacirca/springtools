package dev.springtools.util;

import java.util.*;

/**
 * Utility per gestire richieste HTTP senza dipendere da servlet container.
 *
 * Lavora con dati forniti direttamente da Spring (body, headers, query params).
 */
public class HttpRequest
{

  /**
   * Estrae i parametri numerici con default.
   */
  public static int getIntParam(Map<String, String> queryParams, String name, int defaultValue)
  {
    String value = queryParams.get(name);
    if (value == null || value.trim().isEmpty())
      return defaultValue;
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public static long getLongParam(Map<String, String> queryParams, String name, long defaultValue)
  {
    String value = queryParams.get(name);
    if (value == null || value.trim().isEmpty())
      return defaultValue;
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Estrae i dati da una request.
   *
   * @param body
   *          JSON decodificato come Map<String, Object>
   * @param queryParams
   *          query parameters come Map<String, String>
   * @return mappa combinata dei dati
   */
  public static Map<String, Object> getRequestData(Map<String, Object> body, Map<String, String> queryParams)
  {
    Map<String, Object> data = new HashMap<>();
    if (body != null)
      data.putAll(body);
    if (queryParams != null)
      data.putAll(queryParams);
    return data;
  }

  /**
   * Restituisce tutti i valori testuali dai multipart files caricati.
   * Spring può fornire direttamente i Part o MultipartFile.
   */
  public static Map<String, String> getMultipartFields(Map<String, String> textFields)
  {
    // Qui Spring può passare già i campi text come Map
    return textFields != null ? new HashMap<>(textFields) : new HashMap<>();
  }

  /**
   * Restituisce informazioni sui file caricati (Spring MultipartFile)
   * Thread-safe, il percorso di salvataggio è parametrizzabile
   */
  public static List<Map<String, Object>> getUploadedFiles(List<Map<String, Object>> files, String uploadDir,
      long maxSize)
  {
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map<String, Object> file : files) {
      long size = (long) file.getOrDefault("size", 0L);
      if (size <= maxSize) {
        Map<String, Object> info = new HashMap<>(file);
        // Percorso di salvataggio è parametrizzabile
        String filename = (String) file.get("filename");
        info.put("path", uploadDir + "/" + filename);
        result.add(info);
      }
    }
    return result;
  }

}
