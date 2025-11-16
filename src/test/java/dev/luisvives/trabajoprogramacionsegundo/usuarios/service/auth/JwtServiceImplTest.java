package dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils; // Importante para inyectar valores

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtServiceImplTest {

    @Mock
    private UserDetails userDetails;

    // SUT (Subject Under Test)
    // Usamos @InjectMocks para crear una instancia de JwtServiceImpl
    // (Aunque no hay mocks que inyectar, es buena práctica)
    @InjectMocks
    private JwtServiceImpl jwtService;

    // Valores de prueba para las propiedades @Value
    private final String TEST_KEY = "mi-clave-secreta-de-prueba-muy-larga-y-segura-12345";
    private final Long TEST_EXPIRATION_SECONDS = 3600L; // 1 hora (en segundos)
    private final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        // Inyectamos los valores de configuración en los campos privados del servicio
        ReflectionTestUtils.setField(jwtService, "jwtSigningKey", TEST_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION_SECONDS);

        // Configuramos el mock de UserDetails
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);
    }

    @Test
    void generateToken_ShouldCreateValidTokenWithCorrectSubject() {
        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertNotNull(token);

        // Verificamos el contenido del token usando la misma librería y clave
        Algorithm algorithm = Algorithm.HMAC256(TEST_KEY.getBytes(StandardCharsets.UTF_8));

        // Usamos verify() para asegurar que la firma también es correcta
        DecodedJWT decodedJWT = JWT.require(algorithm)
                .build()
                .verify(token);

        assertEquals(TEST_USERNAME, decodedJWT.getSubject(), "El 'subject' (sub) del token debe ser el username");
        assertEquals("JWT", decodedJWT.getHeaderClaim("typ").asString(), "El header 'typ' debe ser JWT");
        assertTrue(decodedJWT.getExpiresAt().after(new Date()), "La fecha de expiración debe ser en el futuro");
    }

    @Test
    void generateToken_WithExtraClaims_ShouldIncludeThemInToken() {
        // Arrange
        Map<String, Object> extraClaims = Map.of("role", "ADMIN", "userId", 123);

        // Act
        String token = jwtService.generateToken(extraClaims, userDetails);

        // Assert
        Algorithm algorithm = Algorithm.HMAC256(TEST_KEY.getBytes(StandardCharsets.UTF_8));
        DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);

        // Verificamos los claims extra
        Map<String, Object> claimsFromToken = decodedJWT.getClaim("extraClaims").asMap();

        assertEquals(TEST_USERNAME, decodedJWT.getSubject());
        assertEquals("ADMIN", claimsFromToken.get("role"));
        // La librería JWT puede devolver números como Integer o Long, ajustamos el test
        assertEquals(123, ((Number) claimsFromToken.get("userId")).intValue());
    }

    @Test
    void extractUserName_ShouldReturnUsernameFromValidToken() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        String extractedUsername = jwtService.extractUserName(token);

        // Assert
        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    void extractUserName_WhenTokenIsMalformed_ShouldReturnNull() {
        // Arrange
        String malformedToken = "esto.no.es.un.token.jwt";

        // Act
        // El método en el SUT captura la excepción y devuelve null
        String extractedUsername = jwtService.extractUserName(malformedToken);

        // Assert
        assertNull(extractedUsername, "Debe devolver null si el token está malformado");
    }

    @Test
    void isTokenValid_WithValidTokenAndCorrectUser_ShouldReturnTrue() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid, "El token debe ser válido para el usuario que lo generó");
    }

    @Test
    void isTokenValid_WithValidTokenButDifferentUser_ShouldReturnFalse() {
        // Arrange
        String token = jwtService.generateToken(userDetails); // Token generado para "testuser"

        // Creamos un mock para un usuario diferente
        UserDetails otherUser = mock(UserDetails.class);
        when(otherUser.getUsername()).thenReturn("otherUser");

        // Act
        boolean isValid = jwtService.isTokenValid(token, otherUser);

        // Assert
        assertFalse(isValid, "El token no debe ser válido para un usuario diferente");
    }

    @Test
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() throws InterruptedException {
        // Arrange
        // Inyectamos una expiración muy corta (1 milisegundo) solo para este test
        // Nota: jwtExpiration está en segundos, así que 1ms es 0.001s
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L / 1000L); // 0.001 segundos

        String token = jwtService.generateToken(userDetails);

        // Esperamos un momento para asegurar que el token expire
        Thread.sleep(50); // 50 milisegundos

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertFalse(isValid, "El token debe ser inválido porque ha expirado");
    }

    @Test
    void isTokenValid_WithMalformedToken_ShouldReturnFalse() {
        // Arrange
        String malformedToken = "esto.no.es.un.token.jwt";

        // Act
        // El método SUT debe capturar la excepción y devolver false
        boolean isValid = jwtService.isTokenValid(malformedToken, userDetails);

        // Assert
        assertFalse(isValid, "Un token malformado debe resultar en una validación fallida (false)");
    }

    @Test
    void isTokenValid_WhenExtractUserNameReturnsNull_ShouldReturnFalse() {
        // Arrange
        String malformedToken = "token.que.falla.extraccion";
        // Esto hará que extractUserName devuelva null

        // Act
        boolean isValid = jwtService.isTokenValid(malformedToken, userDetails);

        // Assert
        // Esto prueba específicamente la comprobación 'if (username == null)'
        assertFalse(isValid, "Debe devolver false si el username extraído es null");
    }
}