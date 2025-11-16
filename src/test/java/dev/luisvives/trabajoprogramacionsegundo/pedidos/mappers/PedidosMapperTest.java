package dev.luisvives.trabajoprogramacionsegundo.pedidos.mappers;

import dev.luisvives.trabajoprogramacionsegundo.common.dto.PageResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.request.PostAndPutPedidoRequestDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.GenericPedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Cliente;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Direccion;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.LineaPedido;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para {@link PedidosMapper}.
 * <p>
 * Verifica la correcta transformación entre modelos, DTOs de request/response
 * y paginación de pedidos.
 * </p>
 */
class PedidosMapperTest {

    private PedidosMapper mapper;
    private Cliente clienteTest;
    private Direccion direccionTest;
    private LineaPedido lineaPedidoTest;
    private ObjectId testId;

    @BeforeEach
    void setUp() {
        mapper = new PedidosMapper();
        testId = new ObjectId();

        direccionTest = new Direccion(
                "Calle Falsa",
                "123",
                "Springfield",
                "Provincia Test",
                "País Test",
                "28001"
        );

        clienteTest = new Cliente(
                "Homer Simpson",
                "homer@simpson.com",
                "600111222",
                direccionTest
        );

        lineaPedidoTest = new LineaPedido(
                2,
                1L,
                10.0,
                20.0
        );
    }

    // ====================================================================
    // TESTS PARA toResponse()
    // ====================================================================

    @Test
    @DisplayName("toResponse - Debe mapear Pedido a GenericPedidosResponseDto correctamente")
    void toResponse_ShouldMapPedidoToResponseDto() {
        // Arrange
        Pedido pedido = Pedido.builder()
                .id(testId)
                .idUsuario(1L)
                .cliente(clienteTest)
                .lineasPedido(List.of(lineaPedidoTest))
                .totalItems(2)
                .total(20.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        // Act
        GenericPedidosResponseDto result = mapper.toResponse(pedido);

        // Assert
        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals(1L, result.getIdUsuario());
        assertEquals(clienteTest, result.getCliente());
        assertEquals(1, result.getLineaPedido().size());
        assertEquals(lineaPedidoTest, result.getLineaPedido().get(0));
        assertEquals(2, result.getTotalItems());
        assertEquals(20.0, result.getTotal());
    }

    @Test
    @DisplayName("toResponse - Debe manejar pedido sin líneas de pedido")
    void toResponse_ShouldHandleEmptyLineaPedido() {
        // Arrange
        Pedido pedido = Pedido.builder()
                .id(testId)
                .idUsuario(1L)
                .cliente(clienteTest)
                .lineasPedido(List.of())
                .totalItems(0)
                .total(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        // Act
        GenericPedidosResponseDto result = mapper.toResponse(pedido);

        // Assert
        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertTrue(result.getLineaPedido().isEmpty());
        assertEquals(0, result.getTotalItems());
        assertEquals(0.0, result.getTotal());
    }

    @Test
    @DisplayName("toResponse - Debe mapear múltiples líneas de pedido")
    void toResponse_ShouldMapMultipleLineasPedido() {
        // Arrange
        LineaPedido linea1 = new LineaPedido(2, 1L, 10.0, 20.0);
        LineaPedido linea2 = new LineaPedido(3, 2L, 15.0, 45.0);
        LineaPedido linea3 = new LineaPedido(1, 3L, 5.0, 5.0);

        Pedido pedido = Pedido.builder()
                .id(testId)
                .idUsuario(1L)
                .cliente(clienteTest)
                .lineasPedido(List.of(linea1, linea2, linea3))
                .totalItems(6)
                .total(70.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        // Act
        GenericPedidosResponseDto result = mapper.toResponse(pedido);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getLineaPedido().size());
        assertEquals(6, result.getTotalItems());
        assertEquals(70.0, result.getTotal());
        assertTrue(result.getLineaPedido().contains(linea1));
        assertTrue(result.getLineaPedido().contains(linea2));
        assertTrue(result.getLineaPedido().contains(linea3));
    }

    // ====================================================================
    // TESTS PARA toModel()
    // ====================================================================

    @Test
    @DisplayName("toModel - Debe mapear PostAndPutPedidoRequestDto a Pedido correctamente")
    void toModel_ShouldMapRequestDtoToPedido() {
        // Arrange
        PostAndPutPedidoRequestDto requestDto = new PostAndPutPedidoRequestDto(
                1L,
                clienteTest,
                List.of(lineaPedidoTest)
        );

        // Act
        Pedido result = mapper.toModel(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdUsuario());
        assertEquals(clienteTest, result.getCliente());
        assertEquals(1, result.getLineasPedido().size());
        assertEquals(lineaPedidoTest, result.getLineasPedido().get(0));



    }

    @Test
    @DisplayName("toModel - Debe manejar request sin líneas de pedido")
    void toModel_ShouldHandleEmptyLineaPedido() {
        // Arrange
        PostAndPutPedidoRequestDto requestDto = new PostAndPutPedidoRequestDto(
                1L,
                clienteTest,
                List.of()
        );

        // Act
        Pedido result = mapper.toModel(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdUsuario());
        assertEquals(clienteTest, result.getCliente());
        assertTrue(result.getLineasPedido().isEmpty());
    }

    @Test
    @DisplayName("toModel - Debe mapear múltiples líneas de pedido")
    void toModel_ShouldMapMultipleLineasPedido() {
        // Arrange
        LineaPedido linea1 = new LineaPedido(2, 1L, 10.0, 20.0);
        LineaPedido linea2 = new LineaPedido(3, 2L, 15.0, 45.0);

        PostAndPutPedidoRequestDto requestDto = new PostAndPutPedidoRequestDto(
                1L,
                clienteTest,
                List.of(linea1, linea2)
        );

        // Act
        Pedido result = mapper.toModel(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getLineasPedido().size());
        assertTrue(result.getLineasPedido().contains(linea1));
        assertTrue(result.getLineasPedido().contains(linea2));
    }

    @Test
    @DisplayName("toModel - Debe preservar todos los datos del cliente")
    void toModel_ShouldPreserveClienteData() {
        // Arrange
        Cliente clienteCompleto = new Cliente(
                "Juan Pérez",
                "juan@test.com",
                "666777888",
                new Direccion("Gran Vía", "42", "Madrid", "Madrid", "España", "28013")
        );

        PostAndPutPedidoRequestDto requestDto = new PostAndPutPedidoRequestDto(
                5L,
                clienteCompleto,
                List.of(lineaPedidoTest)
        );

        // Act
        Pedido result = mapper.toModel(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getIdUsuario());
        assertEquals("Juan Pérez", result.getCliente().nombreCompleto());
        assertEquals("juan@test.com", result.getCliente().email());
        assertEquals("666777888", result.getCliente().telefono());
        assertEquals("Gran Vía", result.getCliente().direccion().calle());
        assertEquals("42", result.getCliente().direccion().numero());
        assertEquals("Madrid", result.getCliente().direccion().ciudad());
        assertEquals("Madrid", result.getCliente().direccion().provincia());
        assertEquals("España", result.getCliente().direccion().pais());
        assertEquals("28013", result.getCliente().direccion().codigoPostal());
    }

    // ====================================================================
    // TESTS PARA toPageDto()
    // ====================================================================

    @Test
    @DisplayName("toPageDto - Debe mapear Page a PageResponseDTO correctamente")
    void toPageDto_ShouldMapPageToPageResponseDto() {
        // Arrange
        GenericPedidosResponseDto pedido1 = new GenericPedidosResponseDto(
                testId,
                1L,
                clienteTest,
                List.of(lineaPedidoTest),
                2,
                20.0
        );

        GenericPedidosResponseDto pedido2 = new GenericPedidosResponseDto(
                new ObjectId(),
                2L,
                clienteTest,
                List.of(lineaPedidoTest),
                3,
                30.0
        );

        List<GenericPedidosResponseDto> content = List.of(pedido1, pedido2);
        Page<GenericPedidosResponseDto> page = new PageImpl<>(
                content,
                PageRequest.of(0, 10),
                2
        );

        // Act
        PageResponseDTO<GenericPedidosResponseDto> result = mapper.toPageDto(page, "id", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(1, result.getTotalPages());
        assertEquals(2L, result.getTotalElements());
        assertEquals(10, result.getPageSize());
        assertEquals(0, result.getPageNumber());
        assertFalse(result.isEmpty());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        assertEquals("id", result.getSortBy());
        assertEquals("asc", result.getDirection());
    }

    @Test
    @DisplayName("toPageDto - Debe manejar página vacía")
    void toPageDto_ShouldHandleEmptyPage() {
        // Arrange
        Page<GenericPedidosResponseDto> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );

        // Act
        PageResponseDTO<GenericPedidosResponseDto> result = mapper.toPageDto(emptyPage, "id", "asc");

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalPages());
        assertEquals(0L, result.getTotalElements());
        assertTrue(result.isEmpty());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
    }

    @Test
    @DisplayName("toPageDto - Debe manejar ordenación descendente")
    void toPageDto_ShouldHandleDescendingSort() {
        // Arrange
        GenericPedidosResponseDto pedido = new GenericPedidosResponseDto(
                testId,
                1L,
                clienteTest,
                List.of(lineaPedidoTest),
                2,
                20.0
        );

        Page<GenericPedidosResponseDto> page = new PageImpl<>(
                List.of(pedido),
                PageRequest.of(0, 10),
                1
        );

        // Act
        PageResponseDTO<GenericPedidosResponseDto> result = mapper.toPageDto(page, "total", "desc");

        // Assert
        assertNotNull(result);
        assertEquals("total", result.getSortBy());
        assertEquals("desc", result.getDirection());
    }

    @Test
    @DisplayName("toPageDto - Debe manejar múltiples páginas")
    void toPageDto_ShouldHandleMultiplePages() {
        // Arrange
        GenericPedidosResponseDto pedido = new GenericPedidosResponseDto(
                testId,
                1L,
                clienteTest,
                List.of(lineaPedidoTest),
                2,
                20.0
        );

        // Página 2 de 5 (índice 1)
        Page<GenericPedidosResponseDto> page = new PageImpl<>(
                List.of(pedido),
                PageRequest.of(1, 1),
                5
        );

        // Act
        PageResponseDTO<GenericPedidosResponseDto> result = mapper.toPageDto(page, "id", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getPageNumber()); // Segunda página (índice 1)
        assertEquals(5, result.getTotalPages());
        assertEquals(5L, result.getTotalElements());
        assertEquals(1, result.getPageSize());
        assertFalse(result.isFirst());
        assertFalse(result.isLast());
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("toPageDto - Debe preservar el contenido exacto de la página")
    void toPageDto_ShouldPreservePageContent() {
        // Arrange
        ObjectId id1 = new ObjectId();
        ObjectId id2 = new ObjectId();
        ObjectId id3 = new ObjectId();

        GenericPedidosResponseDto pedido1 = new GenericPedidosResponseDto(id1, 1L, clienteTest, List.of(), 0, 0.0);
        GenericPedidosResponseDto pedido2 = new GenericPedidosResponseDto(id2, 2L, clienteTest, List.of(), 0, 0.0);
        GenericPedidosResponseDto pedido3 = new GenericPedidosResponseDto(id3, 3L, clienteTest, List.of(), 0, 0.0);

        List<GenericPedidosResponseDto> content = List.of(pedido1, pedido2, pedido3);
        Page<GenericPedidosResponseDto> page = new PageImpl<>(content, PageRequest.of(0, 10), 3);

        // Act
        PageResponseDTO<GenericPedidosResponseDto> result = mapper.toPageDto(page, "id", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals(id1, result.getContent().get(0).getId());
        assertEquals(id2, result.getContent().get(1).getId());
        assertEquals(id3, result.getContent().get(2).getId());
    }

    // ====================================================================
    // TESTS DE INTEGRACIÓN (toModel -> toResponse)
    // ====================================================================

    @Test
    @DisplayName("Integración - Debe convertir RequestDto a Model y luego a ResponseDto")
    void integration_ShouldConvertRequestToModelToResponse() {
        // Arrange
        PostAndPutPedidoRequestDto requestDto = new PostAndPutPedidoRequestDto(
                1L,
                clienteTest,
                List.of(lineaPedidoTest)
        );

        // Act
        Pedido model = mapper.toModel(requestDto);
        model.setId(testId); // Simular que se guardó en BD
        model.setTotalItems(2);
        model.setTotal(20.0);

        GenericPedidosResponseDto responseDto = mapper.toResponse(model);

        // Assert
        assertNotNull(responseDto);
        assertEquals(testId, responseDto.getId());
        assertEquals(requestDto.getIdUsuario(), responseDto.getIdUsuario());
        assertEquals(requestDto.getCliente(), responseDto.getCliente());
        assertEquals(requestDto.getLineaPedido().size(), responseDto.getLineaPedido().size());
        assertEquals(2, responseDto.getTotalItems());
        assertEquals(20.0, responseDto.getTotal());
    }
}