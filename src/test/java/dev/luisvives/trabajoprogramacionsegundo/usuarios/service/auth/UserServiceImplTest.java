package dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth;


import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Usuario; // Asumiendo la ruta del modelo
import dev.luisvives.trabajoprogramacionsegundo.usuarios.repository.UsuariosRepository;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests para UserServiceImpl para verificar la carga de usuarios
 * por email o username.
 */
@ExtendWith(MockitoExtension.class) // Habilita la integración de Mockito con JUnit 5
class UserServiceImplTest {

    // 1. El Mock (Dependencia)
    // Creamos un mock del repositorio, ya que no queremos probar la base de datos,
    // solo la lógica de servicio.
    @Mock
    private UsuariosRepository usuariosRepository;

    // 2. El SUT (Subject Under Test)
    // Inyectamos los mocks (UsuariosRepository) en el servicio que vamos a probar.
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void loadUserByUsername_WhenEmailExists_ReturnsUserDetails() {
        // Arrange (Preparar)
        String email = "usuario@test.com";
        // Creamos un usuario mock que implementa UserDetails
        Usuario mockUsuario = mock(Usuario.class);

        // Definimos el comportamiento del mock:
        // "Cuando se llame a findByEmail con este email, devuelve este usuario"
        when(usuariosRepository.findByEmail(email)).thenReturn(Optional.of(mockUsuario));

        // Act (Actuar)
        UserDetails result = userService.loadUserByUsername(email);

        // Assert (Comprobar)
        assertNotNull(result);
        assertEquals(mockUsuario, result, "El UserDetails devuelto debe ser el encontrado por el repositorio");

        // Verificamos que los métodos correctos (y solo esos) fueron llamados
        verify(usuariosRepository).findByEmail(email);
        verify(usuariosRepository, never()).findByUsername(anyString());
    }

    @Test
    void loadUserByUsername_WhenEmailNotFound_ThrowsUsernameNotFoundException() {
        // Arrange
        String email = "noexiste@test.com";

        // "Cuando se llame a findByEmail, devuelve un Optional vacío"
        when(usuariosRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        // Comprobamos que se lanza la excepción correcta
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(email);
        });

        // Comprobamos que el mensaje de la excepción es el esperado
        assertEquals("Email no encontrado: " + email, exception.getMessage());

        // Verificamos las interacciones
        verify(usuariosRepository).findByEmail(email);
        verify(usuariosRepository, never()).findByUsername(anyString());
    }

    @Test
    void loadUserByUsername_WhenUsernameExists_ReturnsUserDetails() {
        // Arrange
        String username = "admin";
        Usuario mockUsuario = mock(Usuario.class);

        // "Cuando se llame a findByUsername, devuelve este usuario"
        // (Nota: no contiene "@", así que entrará en el 'else')
        when(usuariosRepository.findByUsername(username)).thenReturn(Optional.of(mockUsuario));

        // Act
        UserDetails result = userService.loadUserByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(mockUsuario, result, "El UserDetails devuelto debe ser el encontrado por el repositorio");

        // Verificamos que se llamó al método de username y no al de email
        verify(usuariosRepository, never()).findByEmail(anyString());
        verify(usuariosRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_WhenUsernameNotFound_ThrowsUsernameNotFoundException() {
        // Arrange
        String username = "nouser";

        // "Cuando se llame a findByUsername, devuelve vacío"
        when(usuariosRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(username);
        });

        // Assert
        assertEquals("Usuario no encontrado: " + username, exception.getMessage());

        // Verificamos interacciones
        verify(usuariosRepository, never()).findByEmail(anyString());
        verify(usuariosRepository).findByUsername(username);
    }
}