package dev.luisvives.trabajoprogramacionsegundo.usuarios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.luisvives.trabajoprogramacionsegundo.common.dto.PageResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.request.PostAndPutPedidoRequestDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.DeletePedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.GenericPedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.mappers.PedidosMapper;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Cliente;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Direccion;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.LineaPedido;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.service.PedidosServiceImpl;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario.*;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.mapper.UsuariosMapper;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Tipo;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Usuario;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth.JwtService;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth.UserServiceImpl;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.usuarios.UsuariosPedidosServiceImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UsuariosRestController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                MongoAutoConfiguration.class,
                MongoDataAutoConfiguration.class,
                MongoRepositoriesAutoConfiguration.class,
                RedisAutoConfiguration.class,
                RedisRepositoriesAutoConfiguration.class
        }
)
class UsuariosRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private UsuariosPedidosServiceImpl usuariosService;

    @MockitoBean
    private PedidosServiceImpl pedidosService;

    @MockitoBean
    private UsuariosMapper mapper;

    @MockitoBean
    private PedidosMapper pedidosMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserServiceImpl userServiceImpl;

    private Usuario testUser;
    private UsuariosAdminResponseDto adminResponseDto;
    private UsuariosResponseDto userResponseDto;
    private UsuariosPutPostDto usuariosPutPostDto;
    private UsuarioPutRequestByUserDto usuarioPutRequestByUserDto;
    private Cliente clienteTest;
    private LineaPedido lineaPedidoTest;
    private ObjectId pedidoId;
    private GenericPedidosResponseDto pedidoResponseDto;

    @BeforeEach
    void setUp() {
        pedidoId = new ObjectId();

        testUser = Usuario.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .email("test@user.com")
                .tipo(List.of(Tipo.USUARIO))
                .isDeleted(false)
                .build();

        adminResponseDto = new UsuariosAdminResponseDto(
                1L,
                "testuser",
                "test@user.com",
                List.of("USUARIO"),
                false,
                List.of()
        );

        userResponseDto = new UsuariosResponseDto(
                "testuser",
                "test@user.com",
                "USUARIO"
        );

        usuariosPutPostDto = new UsuariosPutPostDto(
                "testuser",
                "password123",
                "test@user.com",
                List.of("USUARIO")
        );

        usuarioPutRequestByUserDto = new UsuarioPutRequestByUserDto(
                "newusername",
                "newPassword123",
                "oldPassword",
                "new@email.com"
        );

        Direccion direccion = new Direccion("Calle Falsa", "123", "Springfield", "Provincia", "País", "12345");
        clienteTest = new Cliente("Test Cliente", "cliente@test.com", "600111222", direccion);
        lineaPedidoTest = new LineaPedido(2, 1L, 10.0, 20.0);

        pedidoResponseDto = new GenericPedidosResponseDto(
                pedidoId,
                1L,
                clienteTest,
                List.of(lineaPedidoTest),
                2,
                20.0
        );
    }

    // ====================================================================
    // TESTS PARA ENDPOINTS DE ADMIN (/usuario)
    // ====================================================================

    @Test
    @DisplayName("GET /usuario - Obtener todos los usuarios paginados - OK (Admin)")
    void findAll_ShouldReturnPagedUsers_WhenAdmin() throws Exception {
        var responseList = List.of(userResponseDto);
        var page = new PageImpl<>(responseList);
        var pageDto = new PageResponseDTO<>(
                responseList,
                0,
                20L,
                1,
                20,
                responseList.size(),
                false,
                true,
                false,
                "id",
                "asc"
        );

        when(usuariosService.findAll(any(Optional.class), any(Pageable.class))).thenReturn(page);
        when(mapper.pageToDTO(any(), eq("id"), eq("asc"))).thenReturn(pageDto);

        mockMvc.perform(get("/usuario")
                        .with(user("admin").roles("ADMIN"))
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortBy", "id")
                        .param("order", "asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(20));

        verify(usuariosService).findAll(any(Optional.class), any(Pageable.class));
        verify(mapper).pageToDTO(any(), eq("id"), eq("asc"));
    }



    @Test
    @DisplayName("GET /usuario/{id} - Obtener usuario por ID - OK (Admin)")
    void findById_ShouldReturnUser_WhenAdmin() throws Exception {
        when(usuariosService.findById(1L)).thenReturn(adminResponseDto);

        mockMvc.perform(get("/usuario/{id}", 1L)
                        .with(user("admin").roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@user.com"));

        verify(usuariosService).findById(1L);
    }

    @Test
    @DisplayName("GET /usuario/{id} - Usuario no encontrado - Not Found (404)")
    void findById_WhenNotFound_ShouldReturnNotFound() throws Exception {
        Long idInexistente = 999L;
        when(usuariosService.findById(idInexistente))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        mockMvc.perform(get("/usuario/{id}", idInexistente)
                        .with(user("admin").roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(usuariosService).findById(idInexistente);
    }

    @Test
    @DisplayName("PUT /usuario/{id} - Actualizar usuario - OK (Admin)")
    void updateUser_ShouldUpdateUser_WhenAdmin() throws Exception {
        when(usuariosService.updateAdmin(eq(1L), any(UsuariosPutPostDto.class)))
                .thenReturn(userResponseDto);

        mockMvc.perform(put("/usuario/{id}", 1L)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuariosPutPostDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre").value("testuser"))  // ✅ Cambiado de username a nombre
                .andExpect(jsonPath("$.email").value("test@user.com"))
                .andExpect(jsonPath("$.rol").value("USUARIO"));  // ✅ Cambiado de roles a rol

        verify(usuariosService).updateAdmin(eq(1L), any(UsuariosPutPostDto.class));
    }

    @Test
    @DisplayName("PUT /usuario/{id} - Petición inválida - Bad Request (400)")
    void updateUser_WhenInvalid_ShouldReturnBadRequest() throws Exception {
        UsuariosPutPostDto invalidDto = new UsuariosPutPostDto(
                "", // Username vacío
                "password",
                "test@test.com",
                List.of("USUARIO")
        );

        mockMvc.perform(put("/usuario/{id}", 1L)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(usuariosService, never()).updateAdmin(anyLong(), any());
    }

    @Test
    @DisplayName("DELETE /usuario/{id} - Eliminar usuario - OK (Admin)")
    void deleteUser_ShouldDeleteUser_WhenAdmin() throws Exception {
        UsuariosDeleteResponse deleteResponse = new UsuariosDeleteResponse("Usuario eliminado con éxito",userResponseDto);
        when(usuariosService.delete(1L)).thenReturn(deleteResponse);

        mockMvc.perform(delete("/usuario/{id}", 1L)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuario eliminado con éxito"));

        verify(usuariosService).delete(1L);
    }

    // ====================================================================
    // TESTS PARA ENDPOINTS /me (USUARIO AUTENTICADO)
    // ====================================================================

    @Test
    @DisplayName("GET /usuario/me - Obtener datos del usuario autenticado - OK")
    void me_ShouldReturnAuthenticatedUserData() throws Exception {
        // Configurar el contexto de seguridad con el usuario autenticado
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(usuariosService.findById(1L)).thenReturn(adminResponseDto);

        mockMvc.perform(get("/usuario/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(usuariosService).findById(1L);

        // Limpiar el contexto
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("PUT /usuario/me - Actualizar datos del usuario autenticado - OK")
    void updateMe_ShouldUpdateAuthenticatedUser() throws Exception {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(usuariosService.update(eq(1L), any(UsuarioPutRequestByUserDto.class)))
                .thenReturn(userResponseDto);

        mockMvc.perform(put("/usuario/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioPutRequestByUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@user.com"));

        verify(usuariosService).update(eq(1L), any(UsuarioPutRequestByUserDto.class));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("DELETE /usuario/me - Eliminar usuario autenticado - OK")
    void deleteMe_ShouldDeleteAuthenticatedUser() throws Exception {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UsuariosDeleteResponse deleteResponse = new UsuariosDeleteResponse("Usuario eliminado con éxito", userResponseDto);
        when(usuariosService.delete(1L)).thenReturn(deleteResponse);

        mockMvc.perform(delete("/usuario/me")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuario eliminado con éxito"));

        verify(usuariosService).delete(1L);

        SecurityContextHolder.clearContext();
    }

    // ====================================================================
    // TESTS PARA ENDPOINTS /me/pedidos
    // ====================================================================

    @Test
    @DisplayName("GET /usuario/me/pedidos - Obtener pedidos del usuario autenticado - OK")
    void getPedidosByUsuario_ShouldReturnUserPedidos() throws Exception {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var responseList = List.of(pedidoResponseDto);
        var page = new PageImpl<>(responseList);
        var pageDto = new PageResponseDTO<>(
                responseList,
                0,
                10L,
                1,
                10,
                responseList.size(),
                false,
                true,
                false,
                "id",
                "asc"
        );

        when(pedidosService.findPedidosByUserId(eq(1L), any(PageRequest.class))).thenReturn(page);
        when(pedidosMapper.toPageDto(any(), eq("id"), eq("asc"))).thenReturn(pageDto);

        mockMvc.perform(get("/usuario/me/pedidos")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("order", "asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(10));

        verify(pedidosService).findPedidosByUserId(eq(1L), any(PageRequest.class));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /usuario/me/pedidos/{id} - Obtener pedido por ID - OK")
    void getPedidosById_ShouldReturnPedido_WhenOwner() throws Exception {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(pedidosService.findById(pedidoId)).thenReturn(pedidoResponseDto);

        mockMvc.perform(get("/usuario/me/pedidos/{id}", pedidoId.toHexString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1L));

        verify(pedidosService).findById(pedidoId);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /usuario/me/pedidos/{id} - Forbidden cuando el pedido es de otro usuario")
    void getPedidosById_ShouldReturnForbidden_WhenNotOwner() throws Exception {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        GenericPedidosResponseDto otroPedido = new GenericPedidosResponseDto(
                pedidoId,
                999L, // ID de otro usuario
                clienteTest,
                List.of(lineaPedidoTest),
                2,
                20.0
        );

        when(pedidosService.findById(pedidoId)).thenReturn(otroPedido);

        mockMvc.perform(get("/usuario/me/pedidos/{id}", pedidoId.toHexString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(pedidosService).findById(pedidoId);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /usuario/me/pedidos - Crear pedido - OK")
    void savePedido_ShouldCreatePedido() throws Exception {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        PostAndPutPedidoRequestDto requestDto = new PostAndPutPedidoRequestDto(
                1L,
                clienteTest,
                List.of(lineaPedidoTest)
        );

        when(pedidosService.save(any(PostAndPutPedidoRequestDto.class)))
                .thenReturn(pedidoResponseDto);

        mockMvc.perform(post("/usuario/me/pedidos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1L));

        verify(pedidosService).save(any(PostAndPutPedidoRequestDto.class));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /usuario/me/pedidos - Forbidden cuando el ID de usuario no coincide")
    void savePedido_ShouldReturnForbidden_WhenUserIdMismatch() throws Exception {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        PostAndPutPedidoRequestDto requestDto = new PostAndPutPedidoRequestDto(
                999L, // ID diferente al usuario autenticado
                clienteTest,
                List.of(lineaPedidoTest)
        );

        mockMvc.perform(post("/usuario/me/pedidos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(pedidosService, never()).save(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("PUT /usuario/me/pedidos/{id} - Actualizar pedido - OK")
    void updatePedido_ShouldUpdatePedido() throws Exception {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        PostAndPutPedidoRequestDto requestDto = new PostAndPutPedidoRequestDto(
                1L,
                clienteTest,
                List.of(lineaPedidoTest)
        );

        when(pedidosService.update(eq(pedidoId), any(PostAndPutPedidoRequestDto.class)))
                .thenReturn(pedidoResponseDto);

        mockMvc.perform(put("/usuario/me/pedidos/{id}", pedidoId.toHexString())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1L));

        verify(pedidosService).update(eq(pedidoId), any(PostAndPutPedidoRequestDto.class));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("DELETE /usuario/me/profile/{id} - Eliminar pedido - OK")
    void deleteProfile_ShouldDeletePedido() throws Exception {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        DeletePedidosResponseDto deleteResponse = new DeletePedidosResponseDto(
                pedidoResponseDto,
                "Pedido eliminado con éxito"
        );

        when(pedidosService.delete(pedidoId)).thenReturn(deleteResponse);

        mockMvc.perform(delete("/usuario/me/profile/{id}", pedidoId.toHexString())
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pedido eliminado con éxito"));

        verify(pedidosService).delete(pedidoId);

        SecurityContextHolder.clearContext();
    }
}