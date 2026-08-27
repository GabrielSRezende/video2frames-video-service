package br.com.video2frames.video2frames_video_service.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtValidatorTest {

    private static final String SECRET = "mDMUd7hxqi87Jab3PEoexGDbKUqx548RyntWmtrZJqJ";

    private JwtValidator validator;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        validator = new JwtValidator(new JwtProperties(SECRET));
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void extractSubject_comTokenValido_retornaOSubject() {
        String token = Jwts.builder()
                .subject("gabriel@video2frames.com")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(signingKey)
                .compact();

        Optional<String> subject = validator.extractSubject(token);

        assertThat(subject).contains("gabriel@video2frames.com");
    }

    @Test
    void extractSubject_quandoTokenCompletamenteInvalido_retornaEmpty() {
        assertThat(validator.extractSubject("token-completamente-invalido")).isEmpty();
    }

    @Test
    void extractSubject_quandoAssinadoComOutraChave_retornaEmpty() {
        SecretKey outraChave = Keys.hmacShaKeyFor(
                "outra-chave-completamente-diferente-desta-aqui".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("gabriel@video2frames.com")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(outraChave)
                .compact();

        assertThat(validator.extractSubject(token)).isEmpty();
    }

    @Test
    void extractSubject_quandoTokenExpirado_retornaEmpty() {
        String tokenExpirado = Jwts.builder()
                .subject("gabriel@video2frames.com")
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(signingKey)
                .compact();

        assertThat(validator.extractSubject(tokenExpirado)).isEmpty();
    }
}
