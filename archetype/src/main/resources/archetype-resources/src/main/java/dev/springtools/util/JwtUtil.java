package dev.springtools.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.Date;

public class JwtUtil
{
  private final Algorithm algorithm;
  private final long expirySeconds;

  public JwtUtil(String secret, long expirySeconds)
  {
    this.algorithm = Algorithm.HMAC256(secret);
    this.expirySeconds = expirySeconds;
  }

  public String generate(Long userId, String username, String ruolo)
  {
    Instant now;
    Instant expiry;

    now = Instant.now();
    expiry = now.plusSeconds(expirySeconds);

    return JWT.create()
        .withSubject(userId.toString())
        .withClaim("username", username)
        .withClaim("ruolo", ruolo)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(expiry))
        .sign(algorithm);
  }

  public DecodedJWT validate(String token)
  {
    return JWT.require(algorithm).build().verify(token);
  }

  public Long getUserId(String token)
  {
    return Long.parseLong(validate(token).getSubject());
  }

  public String getRuolo(String token)
  {
    return validate(token).getClaim("ruolo").asString();
  }
}
