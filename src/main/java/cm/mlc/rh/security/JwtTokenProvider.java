package cm.mlc.rh.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/** Émission et validation des jetons JWT pour l'API /api/v1. */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMin;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.expiration-min}") long expirationMin) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        // HMAC-SHA256 exige une clé d'au moins 256 bits : on échoue au démarrage
        // plutôt que d'émettre des jetons signés par une clé trop faible.
        if (bytes.length < 32) {
            throw new IllegalStateException(
                "app.jwt.secret doit faire au moins 32 octets (256 bits) ; longueur actuelle : " + bytes.length);
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMin = expirationMin;
    }

    public String generer(String login, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(login)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMin * 60)))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parser(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }
}
