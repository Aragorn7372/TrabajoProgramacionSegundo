package dev.luisvives.trabajoprogramacionsegundo.usuarios.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.function.Function;

@Service
@Slf4j
public class JwtServiceImpl implements  JwtService {
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
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return "";
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return false;
    }
}
