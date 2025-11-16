package dev.luisvives.trabajoprogramacionsegundo.usuarios.repository;

import dev.luisvives.trabajoprogramacionsegundo.BaseRepositoryTest;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Tipo;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de integración para UsuariosRepository usando Testcontainers.
 */
class UsuariosRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Usuario usuarioAdmin;
    private Usuario usuarioNormal;
    private Usuario usuarioEliminado;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos
        usuariosRepository.deleteAll();

        // Limpiar la tabla de tipos de usuario
        entityManager.getEntityManager()
                .createNativeQuery("DELETE FROM usuario_tipo")
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        // Crear usuarios de prueba con @CollectionTable configurado
        LocalDateTime ahora = LocalDateTime.now();

        // Usuario Admin con múltiples roles
        usuarioAdmin = Usuario.builder()
                .username("admin")
                .password("$2a$12$hashedPassword123")
                .email("admin@example.com")
                .tipo(new ArrayList<>(List.of(Tipo.ADMIN, Tipo.USUARIO)))
                .isDeleted(false)
                .fechaCreacion(ahora)
                .fechaModificacion(ahora)
                .build();
        usuarioAdmin = usuariosRepository.saveAndFlush(usuarioAdmin);

        // Usuario normal con un solo rol
        usuarioNormal = Usuario.builder()
                .username("usuario1")
                .password("$2a$12$hashedPassword456")
                .email("usuario1@example.com")
                .tipo(new ArrayList<>(List.of(Tipo.USUARIO)))
                .isDeleted(false)
                .fechaCreacion(ahora)
                .fechaModificacion(ahora)
                .build();
        usuarioNormal = usuariosRepository.saveAndFlush(usuarioNormal);

        // Usuario eliminado
        usuarioEliminado = Usuario.builder()
                .username("eliminado")
                .password("$2a$12$hashedPassword789")
                .email("eliminado@example.com")
                .tipo(new ArrayList<>(List.of(Tipo.USUARIO)))
                .isDeleted(true)
                .fechaCreacion(ahora)
                .fechaModificacion(ahora)
                .build();
        usuarioEliminado = usuariosRepository.saveAndFlush(usuarioEliminado);

        entityManager.clear();
    }

    // ==========================================
    // TESTS PARA findByEmail
    // ==========================================

    @Test
    @DisplayName("findByEmail - Encuentra usuario por email")
    void testFindByEmail() {
        // When
        Optional<Usuario> resultado = usuariosRepository.findByEmail("admin@example.com");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo("admin@example.com");
        assertThat(resultado.get().getUsername()).isEqualTo("admin");
        assertThat(resultado.get().getTipo()).containsExactlyInAnyOrder(Tipo.ADMIN, Tipo.USUARIO);
    }

    @Test
    @DisplayName("findByEmail - No encuentra usuario con email inexistente")
    void testFindByEmailNoExiste() {
        // When
        Optional<Usuario> resultado = usuariosRepository.findByEmail("noexiste@example.com");

        // Then
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByEmail - Encuentra usuario eliminado por email")
    void testFindByEmailUsuarioEliminado() {
        // When
        Optional<Usuario> resultado = usuariosRepository.findByEmail("eliminado@example.com");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("findByEmail - Email es case sensitive")
    void testFindByEmailCaseSensitive() {
        // When
        Optional<Usuario> resultado = usuariosRepository.findByEmail("ADMIN@EXAMPLE.COM");

        // Then
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByEmail - Email vacío no encuentra nada")
    void testFindByEmailVacio() {
        // When
        Optional<Usuario> resultado = usuariosRepository.findByEmail("");

        // Then
        assertThat(resultado).isEmpty();
    }

    // ==========================================
    // TESTS PARA findByUsername
    // ==========================================

    @Test
    @DisplayName("findByUsername - Encuentra usuario por username con sus roles")
    void testFindByUsername() {
        // When
        Optional<Usuario> resultado = usuariosRepository.findByUsername("admin");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getUsername()).isEqualTo("admin");
        assertThat(resultado.get().getEmail()).isEqualTo("admin@example.com");
        assertThat(resultado.get().getTipo()).hasSize(2);
        assertThat(resultado.get().getTipo()).containsExactlyInAnyOrder(Tipo.ADMIN, Tipo.USUARIO);
    }

    @Test
    @DisplayName("findByUsername - No encuentra usuario inexistente")
    void testFindByUsernameNoExiste() {
        // When
        Optional<Usuario> resultado = usuariosRepository.findByUsername("noexiste");

        // Then
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByUsername - Encuentra usuario eliminado con sus roles")
    void testFindByUsernameUsuarioEliminado() {
        // When
        Optional<Usuario> resultado = usuariosRepository.findByUsername("eliminado");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getIsDeleted()).isTrue();
        assertThat(resultado.get().getTipo()).contains(Tipo.USUARIO);
    }

    @Test
    @DisplayName("findByUsername - Username es case sensitive")
    void testFindByUsernameCaseSensitive() {
        // When
        Optional<Usuario> resultado = usuariosRepository.findByUsername("ADMIN");

        // Then
        assertThat(resultado).isEmpty();
    }

    // ==========================================
    // TESTS PARA findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase
    // ==========================================

    @Test
    @DisplayName("findByUsernameOrEmail - Encuentra por username exacto con roles")
    void testFindByUsernameOrEmailPorUsername() {
        // When
        Optional<Usuario> resultado = usuariosRepository
                .findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase("admin", "cualquiera");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getUsername()).isEqualTo("admin");
        assertThat(resultado.get().getTipo()).containsExactlyInAnyOrder(Tipo.ADMIN, Tipo.USUARIO);
    }

    @Test
    @DisplayName("findByUsernameOrEmail - Encuentra por username ignorando mayúsculas")
    void testFindByUsernameOrEmailPorUsernameIgnoreCase() {
        // When
        Optional<Usuario> resultado = usuariosRepository
                .findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase("ADMIN", "cualquiera");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("findByUsernameOrEmail - Encuentra por email exacto")
    void testFindByUsernameOrEmailPorEmail() {
        // When
        Optional<Usuario> resultado = usuariosRepository
                .findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase("cualquiera", "admin@example.com");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    @DisplayName("findByUsernameOrEmail - Encuentra por email ignorando mayúsculas")
    void testFindByUsernameOrEmailPorEmailIgnoreCase() {
        // When
        Optional<Usuario> resultado = usuariosRepository
                .findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase("cualquiera", "ADMIN@EXAMPLE.COM");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    @DisplayName("findByUsernameOrEmail - Encuentra con username válido aunque email sea inválido")
    void testFindByUsernameOrEmailUsernamePrioridad() {
        // When
        Optional<Usuario> resultado = usuariosRepository
                .findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase("admin", "noexiste@example.com");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("findByUsernameOrEmail - Encuentra con email válido aunque username sea inválido")
    void testFindByUsernameOrEmailEmailPrioridad() {
        // When
        Optional<Usuario> resultado = usuariosRepository
                .findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase("noexiste", "admin@example.com");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    @DisplayName("findByUsernameOrEmail - No encuentra si ambos son inválidos")
    void testFindByUsernameOrEmailAmbosInvalidos() {
        // When
        Optional<Usuario> resultado = usuariosRepository
                .findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase("noexiste", "noexiste@example.com");

        // Then
        assertThat(resultado).isEmpty();
    }

    // ==========================================
    // TESTS PARA updateIsDeletedToTrueById
    // ==========================================

    @Test
    @DisplayName("updateIsDeletedToTrueById - Marca usuario como eliminado")
    void testUpdateIsDeletedToTrueById() {
        // Given
        Long idUsuario = usuarioNormal.getId();

        // When
        usuariosRepository.updateIsDeletedToTrueById(idUsuario);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Usuario> resultado = usuariosRepository.findById(idUsuario);
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("updateIsDeletedToTrueById - No afecta otros campos ni roles")
    void testUpdateIsDeletedToTrueByIdNoAfectaOtrosCampos() {
        // Given
        Long idUsuario = usuarioNormal.getId();
        String usernameOriginal = usuarioNormal.getUsername();
        String emailOriginal = usuarioNormal.getEmail();

        // When
        usuariosRepository.updateIsDeletedToTrueById(idUsuario);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Usuario> resultado = usuariosRepository.findById(idUsuario);
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getUsername()).isEqualTo(usernameOriginal);
        assertThat(resultado.get().getEmail()).isEqualTo(emailOriginal);
        assertThat(resultado.get().getTipo()).contains(Tipo.USUARIO);
    }

    @Test
    @DisplayName("updateIsDeletedToTrueById - No falla con ID inexistente")
    void testUpdateIsDeletedToTrueByIdNoExiste() {
        // When & Then - No debe lanzar excepción
        usuariosRepository.updateIsDeletedToTrueById(999L);
        entityManager.flush();
    }

    // ==========================================
    // TESTS PARA findAllByIsDeletedFalse
    // ==========================================

    @Test
    @DisplayName("findAllByIsDeletedFalse - Devuelve solo usuarios no eliminados con sus roles")
    void testFindAllByIsDeletedFalse() {
        // When
        List<Usuario> resultado = usuariosRepository.findAllByIsDeletedFalse();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2); // admin y usuario1
        assertThat(resultado)
                .extracting(Usuario::getUsername)
                .containsExactlyInAnyOrder("admin", "usuario1");
        assertThat(resultado)
                .allMatch(u -> !u.getIsDeleted());

        // Verificar que los roles se cargan correctamente
        Usuario admin = resultado.stream()
                .filter(u -> u.getUsername().equals("admin"))
                .findFirst()
                .orElseThrow();
        assertThat(admin.getTipo()).containsExactlyInAnyOrder(Tipo.ADMIN, Tipo.USUARIO);
    }

    @Test
    @DisplayName("findAllByIsDeletedFalse - No incluye usuarios eliminados")
    void testFindAllByIsDeletedFalseNoIncluyeEliminados() {
        // When
        List<Usuario> resultado = usuariosRepository.findAllByIsDeletedFalse();

        // Then
        assertThat(resultado)
                .extracting(Usuario::getUsername)
                .doesNotContain("eliminado");
    }

    @Test
    @DisplayName("findAllByIsDeletedFalse - Después de eliminar usuario no lo devuelve")
    void testFindAllByIsDeletedFalseDespuesDeEliminar() {
        // Given
        usuariosRepository.updateIsDeletedToTrueById(usuarioNormal.getId());
        entityManager.flush();
        entityManager.clear();

        // When
        List<Usuario> resultado = usuariosRepository.findAllByIsDeletedFalse();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("findAllByIsDeletedFalse - Todos eliminados devuelve lista vacía")
    void testFindAllByIsDeletedFalseTodosEliminados() {
        // Given
        usuariosRepository.updateIsDeletedToTrueById(usuarioAdmin.getId());
        usuariosRepository.updateIsDeletedToTrueById(usuarioNormal.getId());
        entityManager.flush();
        entityManager.clear();

        // When
        List<Usuario> resultado = usuariosRepository.findAllByIsDeletedFalse();

        // Then
        assertThat(resultado).isEmpty();
    }

    // ==========================================
    // TESTS PARA findAll(Specification, Pageable)
    // ==========================================

    @Test
    @DisplayName("findAll con Specification - Buscar por username")
    void testFindAllConSpecificationPorUsername() {
        // Given
        Specification<Usuario> spec = (root, query, cb) ->
                cb.like(root.get("username"), "%admin%");
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Usuario> resultado = usuariosRepository.findAll(spec, pageable);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getUsername()).isEqualTo("admin");
        assertThat(resultado.getContent().get(0).getTipo()).hasSize(2);
    }

    @Test
    @DisplayName("findAll con Specification - Filtrar por isDeleted false")
    void testFindAllConSpecificationNoEliminados() {
        // Given
        Specification<Usuario> spec = (root, query, cb) ->
                cb.equal(root.get("isDeleted"), false);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Usuario> resultado = usuariosRepository.findAll(spec, pageable);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getContent())
                .allMatch(u -> !u.getIsDeleted());
    }

    @Test
    @DisplayName("findAll con Specification - Paginación primera página")
    void testFindAllConSpecificationPaginacion() {
        // Given
        Specification<Usuario> spec = (root, query, cb) ->
                cb.equal(root.get("isDeleted"), false);
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<Usuario> resultado = usuariosRepository.findAll(spec, pageable);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getTotalElements()).isEqualTo(2);
        assertThat(resultado.getTotalPages()).isEqualTo(2);
        assertThat(resultado.isFirst()).isTrue();
        assertThat(resultado.hasNext()).isTrue();
    }

    // ==========================================
    // TESTS CRUD Y VALIDACIONES
    // ==========================================

    @Test
    @DisplayName("save - Guarda un nuevo usuario con roles correctamente")
    void testSaveUsuario() {
        // Given
        Usuario nuevoUsuario = Usuario.builder()
                .username("nuevo")
                .password("$2a$12$newPassword")
                .email("nuevo@example.com")
                .tipo(new ArrayList<>(List.of(Tipo.USUARIO)))
                .isDeleted(false)
                .build();

        // When
        Usuario guardado = usuariosRepository.saveAndFlush(nuevoUsuario);
        entityManager.clear();

        Usuario recargado = usuariosRepository.findById(guardado.getId()).orElseThrow();

        // Then
        assertThat(recargado).isNotNull();
        assertThat(recargado.getId()).isNotNull();
        assertThat(recargado.getUsername()).isEqualTo("nuevo");
        assertThat(recargado.getTipo()).contains(Tipo.USUARIO);
        assertThat(recargado.getFechaCreacion()).isNotNull();
    }

    @Test
    @DisplayName("save - No permite username duplicado")
    void testSaveUsernameDuplicado() {
        // Given
        Usuario usuarioDuplicado = Usuario.builder()
                .username("admin") // Ya existe
                .password("$2a$12$password")
                .email("otro@example.com")
                .tipo(new ArrayList<>(List.of(Tipo.USUARIO)))
                .isDeleted(false)
                .build();

        // When & Then
        assertThatThrownBy(() -> {
            usuariosRepository.save(usuarioDuplicado);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("save - No permite email duplicado")
    void testSaveEmailDuplicado() {
        // Given
        Usuario usuarioDuplicado = Usuario.builder()
                .username("otrousuario")
                .password("$2a$12$password")
                .email("admin@example.com") // Ya existe
                .tipo(new ArrayList<>(List.of(Tipo.USUARIO)))
                .isDeleted(false)
                .build();

        // When & Then
        assertThatThrownBy(() -> {
            usuariosRepository.save(usuarioDuplicado);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("UserDetails - getAuthorities devuelve roles correctos con prefijo ROLE_")
    void testGetAuthorities() {
        // When
        Usuario usuario = usuariosRepository.findByUsername("admin").orElseThrow();

        // Then
        assertThat(usuario.getAuthorities())
                .hasSize(2)
                .extracting(auth -> auth.getAuthority())
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USUARIO");
    }

    @Test
    @DisplayName("UserDetails - isEnabled devuelve false para usuario eliminado")
    void testIsEnabledUsuarioEliminado() {
        // When
        Usuario usuario = usuariosRepository.findByUsername("eliminado").orElseThrow();

        // Then
        assertThat(usuario.isEnabled()).isFalse();
        assertThat(usuario.getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("UserDetails - isEnabled devuelve true para usuario activo")
    void testIsEnabledUsuarioActivo() {
        // When
        Usuario usuario = usuariosRepository.findByUsername("admin").orElseThrow();

        // Then
        assertThat(usuario.isEnabled()).isTrue();
        assertThat(usuario.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("Usuario con múltiples roles se persiste correctamente")
    void testUsuarioConMultiplesRoles() {
        // Given
        Usuario usuarioMultiRol = Usuario.builder()
                .username("multirole")
                .password("$2a$12$password")
                .email("multirole@example.com")
                .tipo(new ArrayList<>(List.of(Tipo.ADMIN, Tipo.USUARIO)))
                .isDeleted(false)
                .build();

        // When
        Usuario guardado = usuariosRepository.saveAndFlush(usuarioMultiRol);
        entityManager.clear();
        Usuario recargado = usuariosRepository.findById(guardado.getId()).orElseThrow();

        // Then
        assertThat(recargado.getTipo()).hasSize(2);
        assertThat(recargado.getTipo()).containsExactlyInAnyOrder(Tipo.ADMIN, Tipo.USUARIO);
        assertThat(recargado.getAuthorities()).hasSize(2);
    }
}