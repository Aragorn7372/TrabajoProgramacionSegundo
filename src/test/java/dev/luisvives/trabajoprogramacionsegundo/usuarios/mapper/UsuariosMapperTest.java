package dev.luisvives.trabajoprogramacionsegundo.usuarios.mapper;

import dev.luisvives.trabajoprogramacionsegundo.common.dto.PageResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario.UsuarioPutRequestByUserDto;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario.UsuariosAdminResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario.UsuariosPutPostDto;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario.UsuariosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Tipo;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Necesario para mockear 'Page'
class UsuariosMapperTest {

    private UsuariosMapper usuariosMapper;

    // Mock para el método pageToDTO
    @Mock
    private Page<UsuariosResponseDto> mockPage;

    // Datos de prueba
    private Usuario testUsuario;
    private List<String> testPedidos;

    @BeforeEach
    void setUp() {
        // Instanciamos el mapper (no necesita @InjectMocks porque no tiene dependencias)
        usuariosMapper = new UsuariosMapper();

        // Creamos un usuario de prueba
        testUsuario = Usuario.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .tipo(List.of(Tipo.USUARIO, Tipo.ADMIN))
                .isDeleted(false)
                .fechaCreacion(LocalDateTime.now())
                .fechaModificacion(LocalDateTime.now())
                .build();

        testPedidos = List.of("pedido-123", "pedido-456");
    }

    @Test
    void usuariosResponseDtoToUsuariosDto() {
        // Act
        UsuariosResponseDto responseDto = usuariosMapper.usuariosResponseDtoToUsuariosDto(testUsuario);

        // Assert
        assertNotNull(responseDto);
        assertEquals("testuser", responseDto.getNombre());
        assertEquals("test@example.com", responseDto.getEmail());
        // El mapper usa .toString() sobre la lista
        assertEquals("[USUARIO, ADMIN]", responseDto.getRol());
    }

    @Test
    void postPutDtoToUsuario() {
        // Arrange
        UsuariosPutPostDto dto = UsuariosPutPostDto.builder()
                .username("newuser")
                .email("new@example.com")
                .password("newpass")
                .roles(List.of("USUARIO", "ADMIN")) // Probamos con mayúsculas
                .build();

        // Act
        Usuario usuario = usuariosMapper.postPutDtoToUsuario(dto);

        // Assert
        assertNotNull(usuario);
        assertEquals("newuser", usuario.getUsername());
        assertEquals("new@example.com", usuario.getEmail());
        assertEquals("newpass", usuario.getPassword());
        assertEquals(2, usuario.getTipo().size());
        assertTrue(usuario.getTipo().contains(Tipo.USUARIO));
        assertTrue(usuario.getTipo().contains(Tipo.ADMIN));
    }

    @Test
    void postPutDtoToUsuario_HandlesMixedCaseRoles() {
        // Arrange
        // Probamos que el .toUpperCase() funcione
        UsuariosPutPostDto dto = UsuariosPutPostDto.builder()
                .username("user2")
                .email("user2@example.com")
                .password("pass123")
                .roles(List.of("usuario", "admin")) // Probamos con minúsculas
                .build();

        // Act
        Usuario usuario = usuariosMapper.postPutDtoToUsuario(dto);

        // Assert
        assertNotNull(usuario);
        assertEquals(2, usuario.getTipo().size());
        assertTrue(usuario.getTipo().contains(Tipo.USUARIO));
        assertTrue(usuario.getTipo().contains(Tipo.ADMIN));
    }

    @Test
    void postPutDtoByUserDtoToUsuario() {
        // Arrange
        UsuarioPutRequestByUserDto dto = UsuarioPutRequestByUserDto.builder()
                .username("byuser")
                .email("byuser@example.com")
                .password("userpass")
                .oldPassword("oldpass") // Este campo es ignorado por el mapper
                .build();

        // Act
        Usuario usuario = usuariosMapper.postPutDtoByUserDtoToUsuario(dto);

        // Assert
        assertNotNull(usuario);
        assertEquals("byuser", usuario.getUsername());
        assertEquals("byuser@example.com", usuario.getEmail());
        assertEquals("userpass", usuario.getPassword());
        assertNull(usuario.getTipo()); // Este mapper no asigna roles
    }

    @Test
    void usuariosAdminResponseDto() {
        // Act
        UsuariosAdminResponseDto adminResponse = usuariosMapper.usuariosAdminResponseDto(testUsuario, testPedidos);

        // Assert
        assertNotNull(adminResponse);
        assertEquals(1L, adminResponse.getId());
        assertEquals("testuser", adminResponse.getUsername());
        assertEquals("test@example.com", adminResponse.getEmail());
        assertFalse(adminResponse.getIsDeleted());
        // El mapper aplica toUpperCase()
        assertEquals(List.of("USUARIO", "ADMIN"), adminResponse.getTipo());
        assertEquals(List.of("pedido-123", "pedido-456"), adminResponse.getPedidos());
    }

    @Test
    void pageToDTO() {
        // Arrange
        // 1. Creamos el contenido de la página
        UsuariosResponseDto dto1 = new UsuariosResponseDto("user1", "user1@mail.com", "USUARIO");
        List<UsuariosResponseDto> content = List.of(dto1);

        // 2. Definimos el comportamiento del mock
        when(mockPage.getContent()).thenReturn(content);
        when(mockPage.getTotalPages()).thenReturn(5);
        when(mockPage.getTotalElements()).thenReturn(10L);
        when(mockPage.getSize()).thenReturn(2);
        when(mockPage.getNumber()).thenReturn(1);
        when(mockPage.getNumberOfElements()).thenReturn(1);
        when(mockPage.isEmpty()).thenReturn(false);
        when(mockPage.isFirst()).thenReturn(false);
        when(mockPage.isLast()).thenReturn(false);

        String sortBy = "nombre";
        String direction = "asc";

        // Act
        // Asumimos que tienes una clase PageResponseDTO que acepta estos argumentos
        // (Tuve que inferir la clase PageResponseDTO por el constructor)
        PageResponseDTO<UsuariosResponseDto> pageResponse = usuariosMapper.pageToDTO(mockPage, sortBy, direction);

        // Assert
        assertNotNull(pageResponse);
        assertEquals(content, pageResponse.getContent());
        assertEquals(5, pageResponse.getTotalPages());
        assertEquals(10L, pageResponse.getTotalElements());
        assertEquals(2, pageResponse.getPageSize());
        assertEquals(1, pageResponse.getPageNumber());
        assertEquals(1, pageResponse.getTotalPageElements());
        assertFalse(pageResponse.isEmpty());
        assertFalse(pageResponse.isFirst());
        assertFalse(pageResponse.isLast());
        assertEquals("nombre", pageResponse.getSortBy());
        assertEquals("asc", pageResponse.getDirection());
    }
}