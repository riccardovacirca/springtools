package ${package}.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Utilità per gestione configurazione ambiente.
 * <p>
 * Legge configurazione con priorità:
 * 1. System environment variables (produzione Docker -e)
 * 2. JNDI Environment entries (sviluppo context.xml)
 * 3. Default value
 */
public class Env {

  /**
   * Legge variabile ambiente come stringa.
   *
   * @param key Nome variabile
   * @param defaultValue Valore default se non trovata
   * @return Valore variabile o default
   */
  public static String get(String key, String defaultValue) {
    // 1. System environment (produzione)
    String value = System.getenv(key);
    if (value != null && !value.isEmpty()) {
      return value;
    }

    // 2. JNDI Environment (sviluppo)
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      Object obj = envCtx.lookup(key);
      if (obj != null) {
        return obj.toString();
      }
    } catch (NamingException e) {
      // Not found in JNDI
    }

    // 3. Default
    return defaultValue;
  }

  /**
   * Legge variabile ambiente come stringa.
   *
   * @param key Nome variabile
   * @return Valore variabile o null
   */
  public static String get(String key) {
    return get(key, null);
  }

  /**
   * Legge variabile ambiente come intero.
   *
   * @param key Nome variabile
   * @param defaultValue Valore default se non trovata
   * @return Valore variabile o default
   */
  public static Integer getInt(String key, Integer defaultValue) {
    // 1. System environment (produzione)
    String value = System.getenv(key);
    if (value != null && !value.isEmpty()) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }

    // 2. JNDI Environment (sviluppo)
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      Object obj = envCtx.lookup(key);
      if (obj instanceof Integer) {
        return (Integer) obj;
      }
      if (obj != null) {
        try {
          return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
          return defaultValue;
        }
      }
    } catch (NamingException e) {
      // Not found in JNDI
    }

    // 3. Default
    return defaultValue;
  }

  /**
   * Legge variabile ambiente come intero.
   *
   * @param key Nome variabile
   * @return Valore variabile o null
   */
  public static Integer getInt(String key) {
    return getInt(key, null);
  }

  /**
   * Legge variabile ambiente come long.
   *
   * @param key Nome variabile
   * @param defaultValue Valore default se non trovata
   * @return Valore variabile o default
   */
  public static Long getLong(String key, Long defaultValue) {
    // 1. System environment (produzione)
    String value = System.getenv(key);
    if (value != null && !value.isEmpty()) {
      try {
        return Long.parseLong(value);
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }

    // 2. JNDI Environment (sviluppo)
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      Object obj = envCtx.lookup(key);
      if (obj instanceof Long) {
        return (Long) obj;
      }
      if (obj != null) {
        try {
          return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
          return defaultValue;
        }
      }
    } catch (NamingException e) {
      // Not found in JNDI
    }

    // 3. Default
    return defaultValue;
  }

  /**
   * Legge variabile ambiente come long.
   *
   * @param key Nome variabile
   * @return Valore variabile o null
   */
  public static Long getLong(String key) {
    return getLong(key, null);
  }

  /**
   * Legge variabile ambiente come boolean.
   *
   * @param key Nome variabile
   * @param defaultValue Valore default se non trovata
   * @return Valore variabile o default
   */
  public static Boolean getBoolean(String key, Boolean defaultValue) {
    // 1. System environment (produzione)
    String value = System.getenv(key);
    if (value != null && !value.isEmpty()) {
      return Boolean.parseBoolean(value);
    }

    // 2. JNDI Environment (sviluppo)
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      Object obj = envCtx.lookup(key);
      if (obj instanceof Boolean) {
        return (Boolean) obj;
      }
      if (obj != null) {
        return Boolean.parseBoolean(obj.toString());
      }
    } catch (NamingException e) {
      // Not found in JNDI
    }

    // 3. Default
    return defaultValue;
  }

  /**
   * Legge variabile ambiente come boolean.
   *
   * @param key Nome variabile
   * @return Valore variabile o null
   */
  public static Boolean getBoolean(String key) {
    return getBoolean(key, null);
  }

  /**
   * Legge variabile ambiente come double.
   *
   * @param key Nome variabile
   * @param defaultValue Valore default se non trovata
   * @return Valore variabile o default
   */
  public static Double getDouble(String key, Double defaultValue) {
    // 1. System environment (produzione)
    String value = System.getenv(key);
    if (value != null && !value.isEmpty()) {
      try {
        return Double.parseDouble(value);
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }

    // 2. JNDI Environment (sviluppo)
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      Object obj = envCtx.lookup(key);
      if (obj instanceof Double) {
        return (Double) obj;
      }
      if (obj instanceof Float) {
        return ((Float) obj).doubleValue();
      }
      if (obj != null) {
        try {
          return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
          return defaultValue;
        }
      }
    } catch (NamingException e) {
      // Not found in JNDI
    }

    // 3. Default
    return defaultValue;
  }

  /**
   * Legge variabile ambiente come double.
   *
   * @param key Nome variabile
   * @return Valore variabile o null
   */
  public static Double getDouble(String key) {
    return getDouble(key, null);
  }
}
