package dev.luisvives.trabajoprogramacionsegundo.pedidos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.luisvives.trabajoprogramacionsegundo.common.dto.PageResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.request.PostAndPutPedidoRequestDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.DeletePedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.GenericPedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.mappers.PedidosMapper;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Cliente;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Direccion;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.LineaPedido;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.service.PedidosService;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth.JwtService;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth.UserServiceImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PedidosRestController.class,
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
class PedidosRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PedidosService pedidosService;

    @MockitoBean
    private PedidosMapper pedidosMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserServiceImpl userServiceImpl;

    private Cliente clienteTest;
    private LineaPedido lineaPedidoTest;
    private PostAndPutPedidoRequestDto pedidoRequestDto;
    private GenericPedidosResponseDto pedidoResponseDto;
    private ObjectId testId;

    @BeforeEach
    void setUp() {
        testId = new ObjectId();

        Direccion direccion = new Direccion("Calle Falsa", "123", "Springfield", "Provincia", "País", "12345");
        clienteTest = new Cliente("Homer Simpson", "homer@simpson.com", "600111222", direccion);
        lineaPedidoTest = new LineaPedido(2, 1L, 10.0, 20.0);

        pedidoRequestDto = new PostAndPutPedidoRequestDto(1L, clienteTest, List.of(lineaPedidoTest));

        pedidoResponseDto = new GenericPedidosResponseDto(
                testId,
                1L,
                clienteTest,
                List.of(lineaPedidoTest),
                2,
                20.0
        );
    }

    @Test
    @DisplayName("GET /pedidos - Obtener todos los pedidos paginados - OK")
    void findAll_ShouldReturnPagedPedidos() throws Exception {
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

        when(pedidosService.findAll(any(Pageable.class))).thenReturn(page);
        when(pedidosMapper.toPageDto(any(), eq("id"), eq("asc"))).thenReturn(pageDto);

        mockMvc.perform(get("/pedidos")
                        .with(user("testuser").roles("ADMIN","USUARIO"))
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("direction", "asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(10));

        verify(pedidosService).findAll(any(Pageable.class));
        verify(pedidosMapper).toPageDto(any(), eq("id"), eq("asc"));
    }

    @Test
    @DisplayName("GET /pedidos/{id} - Obtener pedido por ID - OK")
    void findById_ShouldReturnPedido() throws Exception {
        when(pedidosService.findById(testId)).thenReturn(pedidoResponseDto);

        mockMvc.perform(get("/pedidos/{id}", testId.toHexString())
                        .with(user("testuser").roles("ADMIN","USUARIO"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testId.toHexString()))
                .andExpect(jsonPath("$.cliente.nombreCompleto").value(clienteTest.nombreCompleto()));

        verify(pedidosService).findById(testId);
    }

    @Test
    @DisplayName("GET /pedidos/{id} - Pedido no encontrado - Not Found (404)")
    void findById_WhenNotFound_ShouldReturnNotFound() throws Exception {
        var idInexistente = new ObjectId();
        when(pedidosService.findById(idInexistente))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        mockMvc.perform(get("/pedidos/{id}", idInexistente.toHexString())
                        .with(user("testuser").roles("ADMIN","USUARIO"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(pedidosService).findById(idInexistente);
    }

    @Test
    @DisplayName("POST /pedidos - Crear nuevo pedido - Created (201)")
    void save_ShouldCreatePedido() throws Exception {
        when(pedidosService.save(any(PostAndPutPedidoRequestDto.class))).thenReturn(pedidoResponseDto);

        mockMvc.perform(post("/pedidos")
                        .with(user("testuser").roles("ADMIN","USUARIO"))
                        .with(csrf()) // ✅ Token CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testId.toHexString()))
                .andExpect(jsonPath("$.cliente.email").value(clienteTest.email()));

        verify(pedidosService).save(any(PostAndPutPedidoRequestDto.class));
    }

    @Test
    @DisplayName("POST /pedidos - Petición inválida (Cliente Nulo) - Bad Request (400)")
    void save_WhenInvalid_ShouldReturnBadRequest() throws Exception {
        var requestInvalido = new PostAndPutPedidoRequestDto(1L, null, List.of());

        mockMvc.perform(post("/pedidos")
                        .with(user("testuser").roles("ADMIN","USUARIO"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());

        verify(pedidosService, never()).save(any());
    }

    @Test
    @DisplayName("PUT /pedidos/{id} - Actualizar pedido - OK")
    void update_ShouldUpdatePedido() throws Exception {
        when(pedidosService.update(eq(testId), any(PostAndPutPedidoRequestDto.class)))
                .thenReturn(pedidoResponseDto);

        mockMvc.perform(put("/pedidos/{id}", testId.toHexString())
                        .with(user("testuser").roles("ADMIN","USUARIO"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testId.toHexString()));

        verify(pedidosService).update(eq(testId), any(PostAndPutPedidoRequestDto.class));
    }

    @Test
    @DisplayName("PUT /pedidos/{id} - Pedido no encontrado - Not Found (404)")
    void update_WhenNotFound_ShouldReturnNotFound() throws Exception {
        var idInexistente = new ObjectId();
        when(pedidosService.update(eq(idInexistente), any(PostAndPutPedidoRequestDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        mockMvc.perform(put("/pedidos/{id}", idInexistente.toHexString())
                        .with(user("testuser").roles("ADMIN","USUARIO"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequestDto)))
                .andExpect(status().isNotFound());

        verify(pedidosService).update(eq(idInexistente), any(PostAndPutPedidoRequestDto.class));
    }

    @Test
    @DisplayName("DELETE /pedidos/{id} - Eliminar pedido - OK")
    void delete_ShouldDeletePedido() throws Exception {
        var deleteResponse = new DeletePedidosResponseDto(pedidoResponseDto, "Pedido eliminado con éxito");
        when(pedidosService.delete(testId)).thenReturn(deleteResponse);

        mockMvc.perform(delete("/pedidos/{id}", testId.toHexString())
                        .with(user("testuser").roles("ADMIN","USUARIO"))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pedido eliminado con éxito"));

        verify(pedidosService).delete(testId);
    }
}