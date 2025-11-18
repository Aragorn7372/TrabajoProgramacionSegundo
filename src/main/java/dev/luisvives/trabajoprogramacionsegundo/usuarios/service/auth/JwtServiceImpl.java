package dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String jwtSigningKey;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Override
    public String extractUserName(String token) {
        log.info(" Extrayendo username del token");


        try {

            String username = extractClaim(token, DecodedJWT::getSubject);
            log.info(" Username extraído: {}", username);
            return username;
        } catch (Exception e) {
            log.error("Error extrayendo username: {}", e.getMessage());
            return null;
        }
    }

    private <T> T extractClaim(String token, Function<DecodedJWT, T> claimExtractor) {
        log.debug("extractClaim from token");
        final DecodedJWT decodedJWT = JWT.decode(token);
        log.debug("Token decodificado - Subject: {}", decodedJWT.getSubject());
        return claimExtractor.apply(decodedJWT);
    }

    public String generateToken(Map<String, Object> extra, UserDetails userDetails) {
        Algorithm algorithm = Algorithm.HMAC256(getSigningKey());
        Date now = new Date();
        Date expiration = new Date(now.getTime() + (1000 * jwtExpiration));

        log.info(" Generando token para usuario: {}", userDetails.getUsername());

        String token = JWT.create()
                .withHeader(createHeader())
                .withSubject(userDetails.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(expiration)
                .withClaim("extraClaims", extra)
                .sign(algorithm);

        log.info(" Token generado con subject: {}", userDetails.getUsername());
        return token;
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        log.info("Generating token for user {}", userDetails.getUsername());
        return generateToken(new HashMap<>(), userDetails);
    }

    private Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        return header;
    }


    private byte[] getSigningKey() {
        return jwtSigningKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            log.info(" Validando token para usuario: {}", userDetails.getUsername());

            final String username = extractUserName(token);

            if (username == null) {
                log.warn(" Username es null");
                return false;
            }

            log.info(" Comparando - Token: '{}' vs Esperado: '{}'", username, userDetails.getUsername());

            boolean usernameMatches = username.equals(userDetails.getUsername());
            boolean tokenNotExpired = !isTokenExpired(token);

            log.info(" Username coincide: {}", usernameMatches);
            log.info(" Token no expirado: {}", tokenNotExpired);

            boolean isValid = usernameMatches && tokenNotExpired;

            if (isValid) {
                log.info("Token válido");
            } else {
                log.warn(" Token inválido");
            }

            return isValid;

        } catch (Exception e) {
            log.error(" Error validando token: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expirationDate = extractExpiration(token);
        boolean expired = expirationDate.before(new Date());
        log.debug("Token expiration: {}, is expired: {}", expirationDate, expired);
        return expired;
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, DecodedJWT::getExpiresAt);
    }
}