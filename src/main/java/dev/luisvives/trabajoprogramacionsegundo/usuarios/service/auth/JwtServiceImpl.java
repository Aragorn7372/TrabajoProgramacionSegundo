package dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth;

import com.auth0.jwt.JWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

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
        log.debug("extractUserName from token={}", token);
        return extractClaim(token, DecodedJWT::getSubject);
    }

    private <T> T extractClaim(String token, Function<DecodedJWT, T> claimExtractor) {
        log.debug("extractClaim from token={}", token);
        final DecodedJWT decodedJWT = JWT.decode(token);
        return claimExtractor.apply(decodedJWT);
    }


    public String generateToken(Map<String, Object> extra, UserDetails userDetails ) {

        Algorithm algorithm = Algorithm.HMAC256(getSigningKey());
        Date now = new Date();
        Date expiration = new Date(now.getTime() + (1000*jwtExpiration));
        return JWT.create()
                .withHeader(createHeader())
                .withSubject(userDetails.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(expiration)
                .withClaim("extraClaims",extra)
                .sign(algorithm);

    }
    @Override
    public String generateToken(UserDetails userDetails) {
        log.info("Generating token for user " + userDetails.getUsername());
        return generateToken(new HashMap<>(), userDetails);
    }

    private Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        return header;
    }

    private byte[] getSigningKey() {
        return Base64.getEncoder().encode(jwtSigningKey.getBytes());
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return false;
    }
}
