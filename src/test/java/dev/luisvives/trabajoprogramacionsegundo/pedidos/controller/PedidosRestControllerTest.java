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
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para PedidosRestController.
 * Se utiliza @WebMvcTest para cargar solo la capa web y @MockBean para simular
 * las dependencias (servicio y mapper).
 */
@WebMvcTest(PedidosRestController.class)
class PedidosRestControllerTest {

    @Autowired
    private MockMvc mockMvc; // Cliente para simular peticiones HTTP

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos a/desde JSON

    @MockitoBean
    private PedidosService pedidosService; // Mock del servicio

    @MockitoBean
    private PedidosMapper pedidosMapper; // Mock del mapper

    // --- Datos de prueba ---
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
                1L, // idUsuario
                clienteTest,
                List.of(lineaPedidoTest),
                2,
                20.0
        );
    }

    // --- Tests para findAll (GET /pedidos) ---

    @Test
    @DisplayName("GET /pedidos - Obtener todos los pedidos paginados - OK")
    void findAll_ShouldReturnPagedPedidos() throws Exception {
        // Arrange
        var responseList = List.of(pedidoResponseDto);
        var page = new PageImpl<>(responseList);
        var pageDto = new PageResponseDTO<>(responseList, 1, 1, 1, 1, 1, false, true, false, "id", "asc");

        when(pedidosService.findAll(any(Pageable.class))).thenReturn(page);
        when(pedidosMapper.toPageDto(any(), eq("id"), eq("asc"))).thenReturn(pageDto);

        // Act & Assert
        mockMvc.perform(get("/pedidos")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("direction", "asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(pedidosService).findAll(any(Pageable.class));
        verify(pedidosMapper).toPageDto(any(), eq("id"), eq("asc"));
    }

    // --- Tests para findById (GET /pedidos/{id}) ---

    @Test
    @DisplayName("GET /pedidos/{id} - Obtener pedido por ID - OK")
    void findById_ShouldReturnPedido() throws Exception {
        // Arrange
        when(pedidosService.findById(testId)).thenReturn(pedidoResponseDto);

        // Act & Assert
        mockMvc.perform(get("/pedidos/{id}", testId.toHexString())
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
        // Arrange
        var idInexistente = new ObjectId();
        when(pedidosService.findById(idInexistente)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/pedidos/{id}", idInexistente.toHexString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(pedidosService).findById(idInexistente);
    }

    // --- Tests para save (POST /pedidos) ---

    @Test
    @DisplayName("POST /pedidos - Crear nuevo pedido - Created (201)")
    void save_ShouldCreatePedido() throws Exception {
        // Arrange
        when(pedidosService.save(any(PostAndPutPedidoRequestDto.class))).thenReturn(pedidoResponseDto);

        // Act & Assert
        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequestDto)))
                .andExpect(status().isCreated()) // HTTP 201
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testId.toHexString()))
                .andExpect(jsonPath("$.cliente.email").value(clienteTest.email()));

        verify(pedidosService).save(any(PostAndPutPedidoRequestDto.class));
    }

    @Test
    @DisplayName("POST /pedidos - Petición inválida (Cliente Nulo) - Bad Request (400)")
    void save_WhenInvalid_ShouldReturnBadRequest() throws Exception {
        // Arrange
        var requestInvalido = new PostAndPutPedidoRequestDto(1L, null, List.of()); // Cliente nulo y lista vacía

        // Act & Assert
        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest()); // HTTP 400

        verify(pedidosService, never()).save(any()); // El servicio no debe ser llamado
    }

    // --- Tests para update (PUT /pedidos/{id}) ---

    @Test
    @DisplayName("PUT /pedidos/{id} - Actualizar pedido - OK")
    void update_ShouldUpdatePedido() throws Exception {
        // Arrange
        when(pedidosService.update(eq(testId), any(PostAndPutPedidoRequestDto.class))).thenReturn(pedidoResponseDto);

        // Act & Assert
        mockMvc.perform(put("/pedidos/{id}", testId.toHexString())
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
        // Arrange
        var idInexistente = new ObjectId();
        when(pedidosService.update(eq(idInexistente), any(PostAndPutPedidoRequestDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        // Act & Assert
        mockMvc.perform(put("/pedidos/{id}", idInexistente.toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequestDto)))
                .andExpect(status().isNotFound());

        verify(pedidosService).update(eq(idInexistente), any(PostAndPutPedidoRequestDto.class));
    }

    // --- Tests para delete (DELETE /pedidos/{id}) ---

    @Test
    @DisplayName("DELETE /pedidos/{id} - Eliminar pedido - OK")
    void delete_ShouldDeletePedido() throws Exception {
        // Arrange
        var deleteResponse = new DeletePedidosResponseDto(pedidoResponseDto, "Pedido eliminado con éxito");
        when(pedidosService.delete(testId)).thenReturn(deleteResponse);

        // Act & Assert
        mockMvc.perform(delete("/pedidos/{id}", testId.toHexString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pedido eliminado con éxito"));

        verify(pedidosService).delete(testId);
    }
}