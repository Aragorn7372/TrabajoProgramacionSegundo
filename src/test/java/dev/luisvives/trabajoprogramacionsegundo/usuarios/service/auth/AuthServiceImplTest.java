package dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth;

import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth.JwtAuthResponse;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth.SignInRequest;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth.SignUpRequest;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.auth.AuthSingInInvalid;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.auth.UserDiferentPassword;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.auth.UserEmailOrUsernameExists;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Tipo;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Usuario;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.repository.UsuariosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    // Mocks para las dependencias
    @Mock
    private JwtService jwtService;
    @Mock
    private UsuariosRepository usuariosRepository;
    @Mock
    private AuthenticationManager manager;
    @Mock
    private PasswordEncoder encoder;

    // SUT (Subject Under Test)
    // Inyectamos los mocks en el servicio
    @InjectMocks
    private AuthServiceImpl authService;

    // Captor para verificar objetos pasados como argumentos
    @Captor
    private ArgumentCaptor<Usuario> usuarioArgumentCaptor;

    // Objeto de prueba
    private Usuario mockUsuario;

    @BeforeEach
    void setUp() {
        // Inicializamos un usuario mock genérico
        mockUsuario = Usuario.builder()
                .id(1L)
                .username("testuser")
                .email("test@user.com")
                .password("encodedPassword")
                .tipo(List.of(Tipo.USUARIO))
                .build();
    }

    // --- Tests para register() ---

    @Test
    void register_Success() {
        // Arrange
        SignUpRequest request = new SignUpRequest("testuser", "test@user.com", "password123", "password123");

        // Mockeamos el encoder
        when(encoder.encode(request.getPassword())).thenReturn("encodedPassword");

        // Mockeamos el repositorio para que devuelva el usuario guardado
        when(usuariosRepository.save(any(Usuario.class))).thenReturn(mockUsuario);

        // Mockeamos el servicio JWT
        when(jwtService.generateToken(mockUsuario)).thenReturn("test.token");

        // Act
        JwtAuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("test.token", response.getToken());

        // Verificamos que se llamó a save con el usuario correcto
        verify(usuariosRepository).save(usuarioArgumentCaptor.capture());
        Usuario usuarioGuardado = usuarioArgumentCaptor.getValue();

        assertEquals("testuser", usuarioGuardado.getUsername());
        assertEquals("test@user.com", usuarioGuardado.getEmail());
        assertEquals("encodedPassword", usuarioGuardado.getPassword());
        assertTrue(usuarioGuardado.getTipo().contains(Tipo.USUARIO));

        // Verificamos las llamadas
        verify(encoder).encode("password123");
        verify(jwtService).generateToken(mockUsuario);
    }

    @Test
    void register_Failure_PasswordMismatch() {
        // Arrange
        SignUpRequest request = new SignUpRequest("testuser", "test@user.com", "password123", "mismatched_password");

        // Act & Assert
        UserDiferentPassword exception = assertThrows(UserDiferentPassword.class, () -> {
            authService.register(request);
        });

        assertEquals("Eres tan inutil que no pueder poner dos contraseñas iguales", exception.getMessage());

        // Verificamos que no se intentó guardar nada
        verify(usuariosRepository, never()).save(any());
        verify(encoder, never()).encode(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_Failure_UserEmailOrUsernameExists() {
        // Arrange
        SignUpRequest request = new SignUpRequest("testuser", "test@user.com", "password123", "password123");

        // Mockeamos el encoder
        when(encoder.encode(request.getPassword())).thenReturn("encodedPassword");

        // Mockeamos el repositorio para que falle con DataIntegrityViolationException
        when(usuariosRepository.save(any(Usuario.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        UserEmailOrUsernameExists exception = assertThrows(UserEmailOrUsernameExists.class, () -> {
            authService.register(request);
        });

        assertEquals("ya existe un usuario con ese email o username", exception.getMessage());

        // Verificamos que se intentó guardar
        verify(encoder).encode("password123");
        verify(usuariosRepository).save(any(Usuario.class));
        // Verificamos que no se generó token
        verify(jwtService, never()).generateToken(any());
    }

    // --- Tests para login() ---

    @Test
    void login_Success_WithEmail() {
        // Arrange
        String email = "test@user.com";
        String password = "password123";
        SignInRequest request = new SignInRequest(email, password);

        // Mockeamos el repositorio para que encuentre por email
        when(usuariosRepository.findByEmail(email)).thenReturn(Optional.of(mockUsuario));

        // Mockeamos el servicio JWT
        when(jwtService.generateToken(mockUsuario)).thenReturn("email.token");

        // Act
        JwtAuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("email.token", response.getToken());

        // Verificamos que el AuthenticationManager fue llamado
        verify(manager).authenticate(new UsernamePasswordAuthenticationToken(email, password));
        // Verificamos que se buscó por email
        verify(usuariosRepository).findByEmail(email);
        // Verificamos que no se buscó por username
        verify(usuariosRepository, never()).findByUsername(any());
        // Verificamos que se generó el token
        verify(jwtService).generateToken(mockUsuario);
    }

    @Test
    void login_Success_WithUsername() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        SignInRequest request = new SignInRequest(username, password);

        // Mockeamos el repositorio para que encuentre por username
        when(usuariosRepository.findByUsername(username)).thenReturn(Optional.of(mockUsuario));

        // Mockeamos el servicio JWT
        when(jwtService.generateToken(mockUsuario)).thenReturn("username.token");

        // Act
        JwtAuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("username.token", response.getToken());

        // Verificamos que el AuthenticationManager fue llamado
        verify(manager).authenticate(new UsernamePasswordAuthenticationToken(username, password));
        // Verificamos que se buscó por username
        verify(usuariosRepository).findByUsername(username);
        // Verificamos que no se buscó por email
        verify(usuariosRepository, never()).findByEmail(any());
        // Verificamos que se generó el token
        verify(jwtService).generateToken(mockUsuario);
    }

    @Test
    void login_Failure_BadCredentials() {
        // Arrange
        String username = "testuser";
        String badPassword = "wrongpassword";
        SignInRequest request = new SignInRequest(username, badPassword);

        // Mockeamos el AuthenticationManager para que falle
        when(manager.authenticate(any())).thenThrow(BadCredentialsException.class);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(request);
        });

        // Verificamos que no se buscó en el repo ni se generó token
        verify(usuariosRepository, never()).findByUsername(any());
        verify(usuariosRepository, never()).findByEmail(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_Failure_UserNotFoundByUsername() {
        // Arrange
        String username = "nouser";
        String password = "password123";
        SignInRequest request = new SignInRequest(username, password);

        // Mockeamos el repositorio para que no encuentre nada
        when(usuariosRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        AuthSingInInvalid exception = assertThrows(AuthSingInInvalid.class, () -> {
            authService.login(request);
        });

        assertEquals("usuario, email o contraseña incorrecta", exception.getMessage());

        // Verificamos que el manager sí se llamó (en tu lógica, el manager va primero)
        verify(manager).authenticate(new UsernamePasswordAuthenticationToken(username, password));
        // Verificamos la búsqueda
        verify(usuariosRepository).findByUsername(username);
        // Verificamos que no se generó token
        verify(jwtService, never()).generateToken(any());
    }
}