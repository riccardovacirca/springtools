package dev.springtools.util;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * Utilità per gestione date e timestamp.
 * <p>
 * Fornisce metodi helper per operazioni comuni su date utilizzando Java Time API.
 */
public class DateTime {

  /**
   * Converte una stringa in LocalDate usando formato ISO 8601 (yyyy-MM-dd).
   *
   * @param date Stringa data (es: "2025-11-01")
   * @return LocalDate parsato
   * @throws Exception Se la stringa non è una data valida
   */
  public static LocalDate localDate(String date) throws Exception {
    try {
      return LocalDate.parse(date);
    } catch (DateTimeParseException e) {
      throw new Exception("Invalid date format: " + date, e);
    }
  }

  /**
   * Converte una stringa in LocalTime usando formato ISO 8601 (HH:mm:ss).
   *
   * @param time Stringa tempo (es: "14:30:00")
   * @return LocalTime parsato
   * @throws Exception Se la stringa non è un tempo valido
   */
  public static LocalTime localTime(String time) throws Exception {
    try {
      return LocalTime.parse(time);
    } catch (DateTimeParseException e) {
      throw new Exception("Invalid time format: " + time, e);
    }
  }

  /**
   * Converte una stringa in LocalDateTime usando formato ISO 8601.
   *
   * @param dateTime Stringa data/tempo (es: "2025-11-01T14:30:00")
   * @return LocalDateTime parsato
   * @throws Exception Se la stringa non è un datetime valido
   */
  public static LocalDateTime localDateTime(String dateTime) throws Exception {
    try {
      return LocalDateTime.parse(dateTime);
    } catch (DateTimeParseException e) {
      throw new Exception("Invalid datetime format: " + dateTime, e);
    }
  }

  /**
   * Converte una stringa in LocalDate usando formato personalizzato.
   *
   * @param date Stringa data
   * @param format Pattern formato (es: "dd/MM/yyyy")
   * @return LocalDate parsato
   * @throws Exception Se la stringa non è una data valida
   */
  public static LocalDate localDate(String date, String format) throws Exception {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
      return LocalDate.parse(date, formatter);
    } catch (Exception e) {
      throw new Exception("Invalid date format: " + date, e);
    }
  }

  /**
   * Converte una stringa in LocalTime usando formato personalizzato.
   *
   * @param time Stringa tempo
   * @param format Pattern formato (es: "HH:mm")
   * @return LocalTime parsato
   * @throws Exception Se la stringa non è un tempo valido
   */
  public static LocalTime localTime(String time, String format) throws Exception {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
      return LocalTime.parse(time, formatter);
    } catch (Exception e) {
      throw new Exception("Invalid time format: " + time, e);
    }
  }

  /**
   * Converte una stringa in LocalDateTime usando formato personalizzato.
   *
   * @param dateTime Stringa data/tempo
   * @param format Pattern formato (es: "dd/MM/yyyy HH:mm")
   * @return LocalDateTime parsato
   * @throws Exception Se la stringa non è un datetime valido
   */
  public static LocalDateTime localDateTime(String dateTime, String format) throws Exception {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
      return LocalDateTime.parse(dateTime, formatter);
    } catch (Exception e) {
      throw new Exception("Invalid datetime format: " + dateTime, e);
    }
  }

  /**
   * Formatta LocalDate in stringa ISO 8601 (yyyy-MM-dd).
   *
   * @param date LocalDate da formattare
   * @return Stringa formattata
   */
  public static String formatDate(LocalDate date) {
    return date.toString();
  }

  /**
   * Formatta LocalTime in stringa ISO 8601 (HH:mm:ss).
   *
   * @param time LocalTime da formattare
   * @return Stringa formattata
   */
  public static String formatTime(LocalTime time) {
    return time.toString();
  }

  /**
   * Formatta LocalDateTime in stringa ISO 8601.
   *
   * @param dateTime LocalDateTime da formattare
   * @return Stringa formattata
   */
  public static String formatDateTime(LocalDateTime dateTime) {
    return dateTime.toString();
  }

  /**
   * Formatta LocalDate usando formato personalizzato.
   *
   * @param date LocalDate da formattare
   * @param format Pattern formato (es: "dd/MM/yyyy")
   * @return Stringa formattata
   */
  public static String formatDate(LocalDate date, String format) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
    return date.format(formatter);
  }

  /**
   * Formatta LocalTime usando formato personalizzato.
   *
   * @param time LocalTime da formattare
   * @param format Pattern formato (es: "HH:mm")
   * @return Stringa formattata
   */
  public static String formatTime(LocalTime time, String format) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
    return time.format(formatter);
  }

  /**
   * Formatta LocalDateTime usando formato personalizzato.
   *
   * @param dateTime LocalDateTime da formattare
   * @param format Pattern formato (es: "dd/MM/yyyy HH:mm")
   * @return Stringa formattata
   */
  public static String formatDateTime(LocalDateTime dateTime, String format) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
    return dateTime.format(formatter);
  }

  /**
   * Restituisce la data odierna.
   *
   * @return LocalDate di oggi
   */
  public static LocalDate today() {
    return LocalDate.now();
  }

  /**
   * Restituisce la data di ieri.
   *
   * @return LocalDate di ieri
   */
  public static LocalDate yesterday() {
    return LocalDate.now().minusDays(1);
  }

  /**
   * Restituisce la data di domani.
   *
   * @return LocalDate di domani
   */
  public static LocalDate tomorrow() {
    return LocalDate.now().plusDays(1);
  }

  /**
   * Restituisce data e ora corrente.
   *
   * @return LocalDateTime adesso
   */
  public static LocalDateTime now() {
    return LocalDateTime.now();
  }

  /**
   * Restituisce il primo giorno del mese corrente.
   *
   * @return LocalDate primo giorno del mese
   */
  public static LocalDate startOfMonth() {
    return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
  }

  /**
   * Restituisce l'ultimo giorno del mese corrente.
   *
   * @return LocalDate ultimo giorno del mese
   */
  public static LocalDate endOfMonth() {
    return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
  }

  /**
   * Restituisce il primo giorno dell'anno corrente.
   *
   * @return LocalDate primo giorno dell'anno
   */
  public static LocalDate startOfYear() {
    return LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
  }

  /**
   * Restituisce l'ultimo giorno dell'anno corrente.
   *
   * @return LocalDate ultimo giorno dell'anno
   */
  public static LocalDate endOfYear() {
    return LocalDate.now().with(TemporalAdjusters.lastDayOfYear());
  }

  /**
   * Aggiunge giorni a una data.
   *
   * @param date LocalDate base
   * @param days Numero di giorni da aggiungere (può essere negativo)
   * @return LocalDate risultante
   */
  public static LocalDate addDays(LocalDate date, int days) {
    return date.plusDays(days);
  }

  /**
   * Aggiunge mesi a una data.
   *
   * @param date LocalDate base
   * @param months Numero di mesi da aggiungere (può essere negativo)
   * @return LocalDate risultante
   */
  public static LocalDate addMonths(LocalDate date, int months) {
    return date.plusMonths(months);
  }

  /**
   * Aggiunge anni a una data.
   *
   * @param date LocalDate base
   * @param years Numero di anni da aggiungere (può essere negativo)
   * @return LocalDate risultante
   */
  public static LocalDate addYears(LocalDate date, int years) {
    return date.plusYears(years);
  }

  /**
   * Sottrae giorni da una data.
   *
   * @param date LocalDate base
   * @param days Numero di giorni da sottrarre
   * @return LocalDate risultante
   */
  public static LocalDate subDays(LocalDate date, int days) {
    return date.minusDays(days);
  }

  /**
   * Sottrae mesi da una data.
   *
   * @param date LocalDate base
   * @param months Numero di mesi da sottrarre
   * @return LocalDate risultante
   */
  public static LocalDate subMonths(LocalDate date, int months) {
    return date.minusMonths(months);
  }

  /**
   * Sottrae anni da una data.
   *
   * @param date LocalDate base
   * @param years Numero di anni da sottrarre
   * @return LocalDate risultante
   */
  public static LocalDate subYears(LocalDate date, int years) {
    return date.minusYears(years);
  }

  /**
   * Calcola la differenza in giorni tra due date.
   *
   * @param start Data inizio
   * @param end Data fine
   * @return Numero di giorni di differenza
   */
  public static long daysBetween(LocalDate start, LocalDate end) {
    return ChronoUnit.DAYS.between(start, end);
  }

  /**
   * Calcola la differenza in mesi tra due date.
   *
   * @param start Data inizio
   * @param end Data fine
   * @return Numero di mesi di differenza
   */
  public static long monthsBetween(LocalDate start, LocalDate end) {
    return ChronoUnit.MONTHS.between(start, end);
  }

  /**
   * Calcola la differenza in anni tra due date.
   *
   * @param start Data inizio
   * @param end Data fine
   * @return Numero di anni di differenza
   */
  public static long yearsBetween(LocalDate start, LocalDate end) {
    return ChronoUnit.YEARS.between(start, end);
  }

  /**
   * Calcola la differenza in ore tra due datetime.
   *
   * @param start DateTime inizio
   * @param end DateTime fine
   * @return Numero di ore di differenza
   */
  public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
    return ChronoUnit.HOURS.between(start, end);
  }

  /**
   * Calcola la differenza in minuti tra due datetime.
   *
   * @param start DateTime inizio
   * @param end DateTime fine
   * @return Numero di minuti di differenza
   */
  public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
    return ChronoUnit.MINUTES.between(start, end);
  }

  /**
   * Verifica se date1 è prima di date2.
   *
   * @param date1 Prima data
   * @param date2 Seconda data
   * @return true se date1 è prima di date2
   */
  public static boolean isBefore(LocalDate date1, LocalDate date2) {
    return date1.isBefore(date2);
  }

  /**
   * Verifica se date1 è dopo date2.
   *
   * @param date1 Prima data
   * @param date2 Seconda data
   * @return true se date1 è dopo date2
   */
  public static boolean isAfter(LocalDate date1, LocalDate date2) {
    return date1.isAfter(date2);
  }

  /**
   * Verifica se date1 è uguale a date2.
   *
   * @param date1 Prima data
   * @param date2 Seconda data
   * @return true se date1 è uguale a date2
   */
  public static boolean isEqual(LocalDate date1, LocalDate date2) {
    return date1.isEqual(date2);
  }

  /**
   * Verifica se una data è compresa tra due date (inclusivo).
   *
   * @param date Data da verificare
   * @param start Data inizio range
   * @param end Data fine range
   * @return true se date è tra start e end
   */
  public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
    return (date.isEqual(start) || date.isAfter(start)) &&
           (date.isEqual(end) || date.isBefore(end));
  }

  /**
   * Verifica se una stringa è una data valida in formato ISO 8601.
   *
   * @param date Stringa da validare
   * @return true se la stringa è una data valida
   */
  public static boolean isValid(String date) {
    try {
      LocalDate.parse(date);
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }

  /**
   * Verifica se una stringa è una data valida in formato personalizzato.
   *
   * @param date Stringa da validare
   * @param format Pattern formato
   * @return true se la stringa è una data valida
   */
  public static boolean isValid(String date, String format) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
      LocalDate.parse(date, formatter);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Verifica se una data cade nel weekend (sabato o domenica).
   *
   * @param date Data da verificare
   * @return true se è sabato o domenica
   */
  public static boolean isWeekend(LocalDate date) {
    DayOfWeek day = date.getDayOfWeek();
    return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
  }

  /**
   * Verifica se un anno è bisestile.
   *
   * @param year Anno da verificare
   * @return true se l'anno è bisestile
   */
  public static boolean isLeapYear(int year) {
    return Year.isLeap(year);
  }

  /**
   * Converte LocalDateTime in Unix timestamp (secondi).
   *
   * @param dateTime LocalDateTime da convertire
   * @return Timestamp in secondi
   */
  public static long toTimestamp(LocalDateTime dateTime) {
    return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
  }

  /**
   * Crea LocalDateTime da Unix timestamp (secondi).
   *
   * @param timestamp Unix timestamp in secondi
   * @return LocalDateTime convertito
   */
  public static LocalDateTime fromTimestamp(long timestamp) {
    return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
  }

  /**
   * Converte LocalDateTime in millisecondi epoch.
   *
   * @param dateTime LocalDateTime da convertire
   * @return Millisecondi epoch
   */
  public static long toMillis(LocalDateTime dateTime) {
    return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }

  /**
   * Crea LocalDateTime da millisecondi epoch.
   *
   * @param millis Millisecondi epoch
   * @return LocalDateTime convertito
   */
  public static LocalDateTime fromMillis(long millis) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
  }
}
