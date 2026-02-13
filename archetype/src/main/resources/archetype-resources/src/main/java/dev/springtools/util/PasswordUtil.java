package dev.springtools.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil
{
  private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  public static String hash(String plainPassword)
  {
    return encoder.encode(plainPassword);
  }

  public static boolean verify(String plainPassword, String passwordHash)
  {
    return encoder.matches(plainPassword, passwordHash);
  }
}
