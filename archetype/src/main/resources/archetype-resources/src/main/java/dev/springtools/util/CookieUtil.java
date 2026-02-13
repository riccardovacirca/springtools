package dev.springtools.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

public class CookieUtil
{
  public static void set(HttpServletResponse response, String name, String value, int maxAge)
  {
    Cookie cookie;

    cookie = new Cookie(name, value);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);
    cookie.setHttpOnly(true);
    response.addCookie(cookie);
  }

  public static Optional<String> get(HttpServletRequest request, String name)
  {
    Cookie[] cookies;

    cookies = request.getCookies();
    if (cookies == null) return Optional.empty();

    for (Cookie cookie : cookies) {
      if (cookie.getName().equals(name)) {
        return Optional.of(cookie.getValue());
      }
    }
    return Optional.empty();
  }

  public static void delete(HttpServletResponse response, String name)
  {
    Cookie cookie;

    cookie = new Cookie(name, "");
    cookie.setPath("/");
    cookie.setMaxAge(0);
    cookie.setHttpOnly(true);
    response.addCookie(cookie);
  }
}
