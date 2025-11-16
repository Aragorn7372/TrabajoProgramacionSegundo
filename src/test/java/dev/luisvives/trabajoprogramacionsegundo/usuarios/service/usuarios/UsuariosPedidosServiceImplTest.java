package dev.luisvives.trabajoprogramacionsegundo.usuarios.service.usuarios;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.repository.PedidosRepository;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario.*;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.auth.UserEmailOrUsernameExists;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.usuarios.IncorrectOldPassword;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.usuarios.UserNotFound;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.mapper.UsuariosMapper;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Tipo;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Usuario;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.repository.UsuariosRepository;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.usuarios.UsuariosPedidosServiceImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.val;


@ExtendWith(MockitoExtension.class)
class UsuariosPedidosServiceImplTest {

    @Mock
    private UsuariosRepository usuariosRepository;
    @Mock
    private PedidosRepository pedidosRepository;
    @Mock
    private UsuariosMapper usuariosMapper;
    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UsuariosPedidosServiceImpl service;

    // Reusable fixtures (real instances, NOT mocked)
    private Usuario usuario;
    private UsuariosResponseDto usuariosResponseDto;
    private UsuarioPutRequestByUserDto usuarioPutRequestByUserDto;
    private ObjectId pedidoObjectId;
    private Pedido pedido;
    private UsuariosAdminResponseDto usuariosAdminResponseDto;

    @BeforeEach
    void setUp() {
        val usuarioVal = Usuario.builder()
                .id(1L)
                .username("usuario_test")
                .password("password123")
                .email("usuario.test@example.com")
                .tipo(List.of(Tipo.USUARIO))
                .isDeleted(false)
                .fechaCreacion(LocalDateTime.now())
                .fechaModificacion(LocalDateTime.now())
                .build();
        this.usuario = usuarioVal;

        this.usuariosResponseDto = UsuariosResponseDto.builder()
                .nombre(usuario.getUsername())
                .email(usuario.getEmail())
                .rol(usuario.getTipo().toString())
                .build();

        this.usuarioPutRequestByUserDto = UsuarioPutRequestByUserDto.builder()
                .username(usuario.getUsername())
                .oldPassword(usuario.getPassword())
                .email(usuario.getEmail())
                .password("newpassword123")
                .build();

        this.pedidoObjectId = ObjectId.get();
        this.pedido = Pedido.builder()
                .idUsuario(usuario.getId())
                .id(pedidoObjectId)
                .build();

        this.usuariosAdminResponseDto = UsuariosAdminResponseDto.builder()
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .isDeleted(usuario.getIsDeleted())
                .id(usuario.getId())
                .tipo(usuario.getTipo().stream().map(Tipo::toString).collect(Collectors.toList()))
                .pedidos(List.of(pedido.getId().toHexString()))
                .build();
    }

    @SuppressWarnings("unchecked")
    @Test
    void findAll_returnsMappedPage() {
        Page<Usuario> page = new PageImpl<>(List.of(usuario));
        Pageable pageable = PageRequest.of(0, 10);

        when(usuariosRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(usuariosMapper.usuariosResponseDtoToUsuariosDto(usuario)).thenReturn(usuariosResponseDto);

        Page<UsuariosResponseDto> result = service.findAll(Optional.empty(), pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertSame(usuariosResponseDto, result.getContent().get(0));

        verify(usuariosRepository).findAll(any(Specification.class), eq(pageable));
        verify(usuariosMapper).usuariosResponseDtoToUsuariosDto(usuario);
    }

    @Test
    void findById_successful() {
        Long userId = usuario.getId();
        when(usuariosRepository.findById(userId)).thenReturn(Optional.of(usuario));
        when(pedidosRepository.findPedidosByIdsByIdUsuario(userId)).thenReturn(List.of(pedido));
        when(usuariosMapper.usuariosAdminResponseDto(usuario, List.of(pedido.getId().toHexString())))
                .thenReturn(usuariosAdminResponseDto);

        UsuariosAdminResponseDto result = service.findById(userId);

        assertSame(usuariosAdminResponseDto, result);
        verify(usuariosRepository).findById(userId);
        verify(pedidosRepository).findPedidosByIdsByIdUsuario(userId);
        verify(usuariosMapper).usuariosAdminResponseDto(usuario, List.of(pedido.getId().toHexString()));
    }

    @Test
    void findById_notFound_throws() {
        Long idNotFound = 42L;
        when(usuariosRepository.findById(idNotFound)).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> service.findById(idNotFound));
        verify(usuariosRepository).findById(idNotFound);
        verifyNoInteractions(pedidosRepository, usuariosMapper);
    }

    @Test
    void update_throwsIncorrectOldPassword_whenEncoderMatches() {
        Long id = usuario.getId();

        when(usuariosRepository.findById(id)).thenReturn(Optional.of(usuario));
        // the encoder is mocked: when it matches (true) the service throws IncorrectOldPassword
        when(encoder.matches(usuarioPutRequestByUserDto.getOldPassword(), usuario.getPassword())).thenReturn(true);

        assertThrows(IncorrectOldPassword.class, () -> service.update(id, usuarioPutRequestByUserDto));

        verify(usuariosRepository).findById(id);
        verify(encoder).matches(usuarioPutRequestByUserDto.getOldPassword(), usuario.getPassword());
    }

    @Test
    void update_throwsUserEmailOrUsernameExists_whenRepositoryFindsExistingUserWithSameId() {
        Long id = usuario.getId();

        when(usuariosRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(encoder.matches(usuarioPutRequestByUserDto.getOldPassword(), usuario.getPassword())).thenReturn(false);

        // create a real Usuario that the repository returns as "found" (not a mock)
        Usuario foundUsuario = Usuario.builder()
                .id(id) // same id to trigger the service's "equals(id)" check and throw
                .username(usuarioPutRequestByUserDto.getUsername())
                .email(usuarioPutRequestByUserDto.getEmail())
                .build();

        when(usuariosRepository.findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase(
                usuarioPutRequestByUserDto.getUsername(),
                usuarioPutRequestByUserDto.getEmail()))
                .thenReturn(Optional.of(foundUsuario));

        assertThrows(UserEmailOrUsernameExists.class, () -> service.update(id, usuarioPutRequestByUserDto));

        verify(usuariosRepository).findById(id);
        verify(usuariosRepository).findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase(
                usuarioPutRequestByUserDto.getUsername(),
                usuarioPutRequestByUserDto.getEmail());
    }

    @Test
    void update_successful() {
        Long id = usuario.getId();

        when(usuariosRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(encoder.matches(usuarioPutRequestByUserDto.getOldPassword(), usuario.getPassword())).thenReturn(false);

        when(usuariosRepository.findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase(
                usuarioPutRequestByUserDto.getUsername(),
                usuarioPutRequestByUserDto.getEmail()))
                .thenReturn(Optional.empty());

        // NO hacer when(...) sobre usuarioPutRequestByUserDto.getPassword() porque es una instancia real.
        // En su lugar stubear la llamada al encoder (que SÍ es mock)
        when(encoder.encode(usuarioPutRequestByUserDto.getPassword())).thenReturn("encodedNew");

        // crear la entidad Usuario que el mapper devolverá para guardar
        Usuario usuarioToSave = Usuario.builder()
                .username(usuarioPutRequestByUserDto.getUsername())
                .email(usuarioPutRequestByUserDto.getEmail())
                .password("encodedNew") // la contraseña ya codificada
                .build();

        Usuario savedUsuario = Usuario.builder()
                .id(id)
                .username(usuarioToSave.getUsername())
                .email(usuarioToSave.getEmail())
                .password(usuarioToSave.getPassword())
                .build();

        when(usuariosMapper.postPutDtoByUserDtoToUsuario(any(UsuarioPutRequestByUserDto.class)))
                .thenReturn(usuarioToSave);
        when(usuariosRepository.save(usuarioToSave)).thenReturn(savedUsuario);
        when(usuariosMapper.usuariosResponseDtoToUsuariosDto(savedUsuario)).thenReturn(usuariosResponseDto);

        UsuariosResponseDto result = service.update(id, usuarioPutRequestByUserDto);

        assertSame(usuariosResponseDto, result);

        verify(usuariosRepository).findById(id);
        verify(encoder).matches(usuarioPutRequestByUserDto.getOldPassword(), usuario.getPassword());
        verify(usuariosRepository).findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase(
                usuarioPutRequestByUserDto.getUsername(),
                usuarioPutRequestByUserDto.getEmail());
        verify(usuariosMapper).postPutDtoByUserDtoToUsuario(any(UsuarioPutRequestByUserDto.class));
        verify(usuariosRepository).save(usuarioToSave);
        verify(usuariosMapper).usuariosResponseDtoToUsuariosDto(savedUsuario);
    }

    @Test
    void delete_whenHasPedidos_marksIsDeletedAndReturnsLogicalDeletionResponse() {
        Long userId = usuario.getId();
        when(usuariosRepository.findById(userId)).thenReturn(Optional.of(usuario));
        when(pedidosRepository.findPedidosByIdsByIdUsuario(userId)).thenReturn(List.of(pedido));
        when(usuariosMapper.usuariosResponseDtoToUsuariosDto(usuario)).thenReturn(usuariosResponseDto);

        UsuariosDeleteResponse response = service.delete(userId);

        assertNotNull(response);
        assertEquals("usuario eliminado con borrado logico exitoso", response.getMessage());
        assertSame(usuariosResponseDto, response.getUsuario());

        verify(usuariosRepository).updateIsDeletedToTrueById(userId);
        verify(usuariosMapper).usuariosResponseDtoToUsuariosDto(usuario);
    }

    @Test
    void delete_whenNoPedidos_deletesPhysicallyAndReturnsDeletionResponse() {
        Long otherId = 2L;
        Usuario otherUser = Usuario.builder()
                .id(otherId)
                .username("other")
                .email("other@example.com")
                .password("pw")
                .build();

        when(usuariosRepository.findById(otherId)).thenReturn(Optional.of(otherUser));
        when(pedidosRepository.findPedidosByIdsByIdUsuario(otherId)).thenReturn(List.of());
        UsuariosResponseDto dto = UsuariosResponseDto.builder().build();
        when(usuariosMapper.usuariosResponseDtoToUsuariosDto(otherUser)).thenReturn(dto);

        UsuariosDeleteResponse response = service.delete(otherId);

        assertNotNull(response);
        assertEquals("usuario eliminado", response.getMessage());
        assertSame(dto, response.getUsuario());

        verify(usuariosRepository).delete(otherUser);
        verify(usuariosMapper).usuariosResponseDtoToUsuariosDto(otherUser);
    }

    @Test
    void updateAdmin_throwsUserEmailOrUsernameExists_whenRepositoryFindsExistingUserWithSameId() {
        Long userId = usuario.getId();

        // real request object is mocked here only to control getters (it's a DTO-like object);
        // the user's instruction was to avoid mocking data/domain classes — DTOs/entities below are real.
        UsuariosPutPostDto request = mock(UsuariosPutPostDto.class);
        when(request.getUsername()).thenReturn("name");
        when(request.getEmail()).thenReturn("email@example.com");

        when(usuariosRepository.findById(userId)).thenReturn(Optional.of(usuario));

        // create a real Usuario returned by repo that has same id to trigger the service's check
        Usuario found = Usuario.builder()
                .id(userId) // same id -> service current logic will throw
                .username(request.getUsername())
                .email(request.getEmail())
                .build();

        when(usuariosRepository.findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase("name", "email@example.com"))
                .thenReturn(Optional.of(found));

        assertThrows(UserEmailOrUsernameExists.class, () -> service.updateAdmin(userId, request));

        verify(usuariosRepository).findById(userId);
        verify(usuariosRepository).findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase("name", "email@example.com");
    }

    @Test
    void updateAdmin_successful() {
        Long id = usuario.getId();

        UsuariosPutPostDto request = mock(UsuariosPutPostDto.class);
        when(request.getUsername()).thenReturn("admin");
        when(request.getEmail()).thenReturn("admin@example.com");
        when(request.getPassword()).thenReturn("p");

        when(usuariosRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(usuariosRepository.findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase("admin", "admin@example.com"))
                .thenReturn(Optional.empty());
        when(encoder.encode("p")).thenReturn("encodedP");

        Usuario usuarioToSave = Usuario.builder()
                .username("admin")
                .email("admin@example.com")
                .password("encodedP")
                .build();
        Usuario savedUsuario = Usuario.builder()
                .id(id)
                .username("admin")
                .email("admin@example.com")
                .password("encodedP")
                .build();

        when(usuariosMapper.postPutDtoToUsuario(request)).thenReturn(usuarioToSave);
        when(usuariosRepository.save(usuarioToSave)).thenReturn(savedUsuario);
        when(usuariosMapper.usuariosResponseDtoToUsuariosDto(savedUsuario)).thenReturn(usuariosResponseDto);

        var result = service.updateAdmin(id, request);

        assertSame(usuariosResponseDto, result);

        verify(usuariosRepository).findById(id);
        verify(usuariosRepository).findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase("admin", "admin@example.com");
        verify(encoder).encode("p");
        verify(usuariosMapper).postPutDtoToUsuario(request);
        verify(usuariosRepository).save(usuarioToSave);
        verify(usuariosMapper).usuariosResponseDtoToUsuariosDto(savedUsuario);
    }
}