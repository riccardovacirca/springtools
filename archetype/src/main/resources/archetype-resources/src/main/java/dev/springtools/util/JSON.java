package dev.springtools.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JSON
{
  private static final ObjectMapper mapper = new ObjectMapper();

  public static String encode(Object obj) throws Exception
  {
    return mapper.writeValueAsString(obj);
  }

  public static <T> T decode(String json, Class<T> cls) throws Exception
  {
    return mapper.readValue(json, cls);
  }

  public static String load(String filename) throws Exception
  {
    return new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
  }

  public static <T> T load(String filename, Class<T> cls) throws Exception
  {
    String json = load(filename);
    return decode(json, cls);
  }
}
