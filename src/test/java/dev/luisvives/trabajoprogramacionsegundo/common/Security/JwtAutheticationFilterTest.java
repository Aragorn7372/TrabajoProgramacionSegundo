package dev.luisvives.trabajoprogramacionsegundo.common.Security;

import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth.JwtService;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth.UserServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAutheticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAutheticationFilter jwtAutheticationFilter; // Tu clase

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Limpiamos el contexto de seguridad ANTES de cada test
        // Esto es crucial para los tests de autenticación
        SecurityContextHolder.clearContext();

        // Creamos un UserDetails genérico para usar en los tests
        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void when_NoAuthorizationHeader_then_ChainContinues() throws ServletException, IOException {
        // RAMA 1: !StringUtils.hasText(authHeader)
        // Simulamos que getHeader("Authorization") devuelve null
        when(request.getHeader("Authorization")).thenReturn(null);

        // Ejecutamos el filtro
        jwtAutheticationFilter.doFilterInternal(request, response, filterChain);

        // Verificamos que se llama al siguiente filtro
        verify(filterChain, times(1)).doFilter(request, response);
        // Verificamos que NO se interactuó con los servicios
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userService);
        // Verificamos que el contexto sigue vacío
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void when_HeaderDoesNotStartWithBearer_then_ChainContinues() throws ServletException, IOException {
        // RAMA 1: !StringUtils.startsWithIgnoreCase(authHeader, "Bearer ")
        when(request.getHeader("Authorization")).thenReturn("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");

        jwtAutheticationFilter.doFilterInternal(request, response, filterChain);

        // Verificamos lo mismo que el test anterior
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void when_TokenExtractionFails_then_SendErrorAndReturn() throws ServletException, IOException {
        // RAMA 3: catch (Exception e) al extraer username
        when(request.getHeader("Authorization")).thenReturn("Bearer malformed-token");
        // Simulamos que el token está malformado o expirado
        when(jwtService.extractUserName("malformed-token")).thenThrow(new RuntimeException("Token no válido"));

        jwtAutheticationFilter.doFilterInternal(request, response, filterChain);

        // Verificamos que se envía error 401
        verify(response, times(1)).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token no autorizado o no válido");
        // Verificamos que la cadena de filtros NO continuó
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void when_UsernameInTokenIsNull_then_ChainContinues() throws ServletException, IOException {
        // RAMA 5: !StringUtils.hasText(userName)
        when(request.getHeader("Authorization")).thenReturn("Bearer token-with-no-user");
        // Simulamos un token válido que devuelve un username null
        when(jwtService.extractUserName("token-with-no-user")).thenReturn(null);

        jwtAutheticationFilter.doFilterInternal(request, response, filterChain);

        // La cadena debe continuar, pero sin autenticar
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(userService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void when_UserIsAlreadyAuthenticated_then_ChainContinues() throws ServletException, IOException {
        // RAMA 5: SecurityContextHolder.getContext().getAuthentication() != null

        // 1. Creamos una autenticación Falsa y la metemos en el contexto
        Authentication existingAuth = new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(existingAuth);
        SecurityContextHolder.setContext(context);

        // 2. Simulamos una cabecera válida (que sería ignorada)
        when(request.getHeader("Authorization")).thenReturn("Bearer some-token");
        when(jwtService.extractUserName("some-token")).thenReturn("testuser");

        jwtAutheticationFilter.doFilterInternal(request, response, filterChain);

        // Verificamos que la cadena continúa
        verify(filterChain, times(1)).doFilter(request, response);
        // VerificRequestquamos que NO se intentó cargar el usuario,
        // porque ya había una autenticación
        verify(userService, never()).loadUserByUsername(anyString());
        // Verificamos que la autenticación original sigue ahí
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);
    }

    @Test
    void when_UserNotFoundInDB_then_SendErrorAndReturn() throws ServletException, IOException {
        // RAMA 7: catch (Exception e) al cargar usuario
        when(request.getHeader("Authorization")).thenReturn("Bearer token-for-unknown-user");
        when(jwtService.extractUserName("token-for-unknown-user")).thenReturn("unknown_user");

        // Simulamos que el servicio lanza UsernameNotFoundException
        when(userService.loadUserByUsername("unknown_user")).thenThrow(new UsernameNotFoundException("Usuario no existe"));

        jwtAutheticationFilter.doFilterInternal(request, response, filterChain);

        // Verificamos que se envía error 401
        verify(response, times(1)).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuario no autorizado");
        // Verificamos que la cadena de filtros NO continuó
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void when_TokenIsInvalidForUser_then_ChainContinues() throws ServletException, IOException {
        // RAMA 9: !jwtService.isTokenValid(jwt, userDetails)
        when(request.getHeader("Authorization")).thenReturn("Bearer token-for-testuser");
        when(jwtService.extractUserName("token-for-testuser")).thenReturn("testuser");

        // Devolvemos el usuario
        when(userService.loadUserByUsername("testuser")).thenReturn(userDetails);

        // Simulamos que el token NO es válido para ESE usuario (p.ej. firma incorrecta)
        when(jwtService.isTokenValid("token-for-testuser", userDetails)).thenReturn(false);

        jwtAutheticationFilter.doFilterInternal(request, response, filterChain);

        // La cadena debe continuar, pero sin autenticar
        verify(filterChain, times(1)).doFilter(request, response);
        // Verificamos que el contexto sigue vacío
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void when_TokenIsValidAndUserExists_then_AuthenticateAndChainContinues() throws ServletException, IOException {
        // RAMA 10: ÉXITO
        String token = "valid-token-for-testuser";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUserName(token)).thenReturn(username);

        // Devolvemos el usuario
        when(userService.loadUserByUsername(username)).thenReturn(userDetails);

        // Simulamos que el token ES válido
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        // (Simulamos la request para el buildDetails)
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // Ejecutamos el filtro
        jwtAutheticationFilter.doFilterInternal(request, response, filterChain);

        // Verificamos que se cargó al usuario 2 VECES
        verify(userService, times(2)).loadUserByUsername(username);

        // Verificamos que la cadena continuó
        verify(filterChain, times(1)).doFilter(request, response);

        // Verificación MÁS IMPORTANTE:
        // Comprobamos que el usuario se ha autenticado en el contexto
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(userDetails);
        assertThat(auth.isAuthenticated()).isTrue(); // El token es válido
        assertThat(auth.getCredentials()).isNull(); // No guardamos password/token aquí
    }
}