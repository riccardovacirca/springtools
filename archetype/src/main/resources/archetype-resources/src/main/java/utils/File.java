package ${package}.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Utilità per operazioni su file.
 * <p>
 * Limite dimensione default: 10MB (configurabile).
 */
public class File {

  // Limite default: 10MB
  private static final long DEFAULT_MAX_SIZE = 10 * 1024 * 1024;

  /**
   * Legge contenuto file come stringa (limite 10MB).
   *
   * @param path Percorso file
   * @return Contenuto file come stringa
   * @throws Exception Se file supera limite o errore lettura
   */
  public static String read(String path) throws Exception {
    return read(path, DEFAULT_MAX_SIZE);
  }

  /**
   * Legge contenuto file con limite dimensione personalizzato.
   *
   * @param path Percorso file
   * @param maxSize Dimensione massima in bytes
   * @return Contenuto file come stringa
   * @throws Exception Se file supera limite o errore lettura
   */
  public static String read(String path, long maxSize) throws Exception {
    Path filePath = Paths.get(path);

    if (!Files.exists(filePath)) {
      throw new Exception("File not found: " + path);
    }

    long fileSize = Files.size(filePath);
    if (fileSize > maxSize) {
      throw new Exception(
        "File size exceeds limit: " + fileSize + " bytes (max: " + maxSize + " bytes)"
      );
    }

    try {
      byte[] bytes = Files.readAllBytes(filePath);
      return new String(bytes, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new Exception("Error reading file: " + path, e);
    }
  }

  /**
   * Scrive contenuto in file (sovrascrive se esiste).
   *
   * @param path Percorso file
   * @param content Contenuto da scrivere
   * @throws Exception Se errore scrittura
   */
  public static void write(String path, String content) throws Exception {
    Path filePath = Paths.get(path);

    try {
      Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new Exception("Error writing file: " + path, e);
    }
  }

  /**
   * Appende contenuto a file esistente (crea se non esiste).
   *
   * @param path Percorso file
   * @param content Contenuto da appendere
   * @throws Exception Se errore scrittura
   */
  public static void append(String path, String content) throws Exception {
    Path filePath = Paths.get(path);

    try {
      Files.write(
        filePath,
        content.getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND
      );
    } catch (IOException e) {
      throw new Exception("Error appending to file: " + path, e);
    }
  }

  /**
   * Verifica se file esiste.
   *
   * @param path Percorso file
   * @return true se file esiste
   */
  public static boolean exists(String path) {
    return Files.exists(Paths.get(path));
  }

  /**
   * Restituisce dimensione file in bytes.
   *
   * @param path Percorso file
   * @return Dimensione in bytes
   * @throws Exception Se file non esiste
   */
  public static long size(String path) throws Exception {
    Path filePath = Paths.get(path);

    if (!Files.exists(filePath)) {
      throw new Exception("File not found: " + path);
    }

    try {
      return Files.size(filePath);
    } catch (IOException e) {
      throw new Exception("Error getting file size: " + path, e);
    }
  }

  /**
   * Elimina file.
   *
   * @param path Percorso file
   * @throws Exception Se errore eliminazione
   */
  public static void delete(String path) throws Exception {
    Path filePath = Paths.get(path);

    if (!Files.exists(filePath)) {
      throw new Exception("File not found: " + path);
    }

    try {
      Files.delete(filePath);
    } catch (IOException e) {
      throw new Exception("Error deleting file: " + path, e);
    }
  }

  /**
   * Copia file.
   *
   * @param source Percorso file sorgente
   * @param dest Percorso file destinazione
   * @throws Exception Se errore copia
   */
  public static void copy(String source, String dest) throws Exception {
    Path sourcePath = Paths.get(source);
    Path destPath = Paths.get(dest);

    if (!Files.exists(sourcePath)) {
      throw new Exception("Source file not found: " + source);
    }

    try {
      Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new Exception("Error copying file from " + source + " to " + dest, e);
    }
  }
}
