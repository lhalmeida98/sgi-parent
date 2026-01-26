package ec.sgi.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final String secret;
  private final long expirationSeconds;

  public JwtService(
      @Value("${app.security.jwt.secret:change-me}") String secret,
      @Value("${app.security.jwt.expiration-seconds:86400}") long expirationSeconds
  ) {
    this.secret = secret;
    this.expirationSeconds = expirationSeconds;
  }

  public String generateToken(UsuarioPrincipal principal) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(expirationSeconds);
    return Jwts.builder()
        .subject(principal.getUsername())
        .claim("empresaId", principal.getEmpresaId())
        .claim("rol", principal.getRol())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .compact();
  }

  public Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
