package dev.luisvives.trabajoprogramacionsegundo.pedidos.service;

import dev.luisvives.trabajoprogramacionsegundo.common.email.OrderEmailService;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.config.WebSocketConfig;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.config.WebSocketHandler;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.request.PostAndPutPedidoRequestDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.DeletePedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.GenericPedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.exceptions.PedidoException;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.mappers.PedidosMapper;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Cliente;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Direccion;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.LineaPedido;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.repository.PedidosRepository;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import dev.luisvives.trabajoprogramacionsegundo.productos.repository.ProductsRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidosServiceImplTest {
    @Mock
    private PedidosRepository pedidoRepository;
    @Mock
    private ProductsRepository productsRepository;
    @Mock
    private OrderEmailService emailService;
    @Mock
    private PedidosMapper pedidosMapper;
    @Mock
    private WebSocketConfig webSocketConfig;
    @Mock
    private WebSocketHandler webSocketHandler;
    @Mock
    private Producto productoMock; // Mock para la validación de productos

    private PedidosServiceImpl pedidosServiceImpl;

    // === DATOS DE PRUEBA ===
    private final ObjectId objectId = new ObjectId();
    private final Long idProducto = 1L;

    private final Direccion direccion = new Direccion(
            "calle", "1", "ciudad", "provincia", "pais", "01000"
    );

    private final Cliente cliente = new Cliente(
            "Pepe", "pepe@mail.com", "123456789", direccion
    );

    private final LineaPedido lineaPedido = new LineaPedido(
            1, idProducto, 10.0, 10.0
    );

    private final List<LineaPedido> lineasPedido = List.of(lineaPedido);

    private final Pedido pedido = new Pedido(
            objectId, 1L, cliente, lineasPedido, 1, 10.0, LocalDateTime.now(), LocalDateTime.now(), false
    );

    private final GenericPedidosResponseDto pedidoResponse = new GenericPedidosResponseDto(
            objectId, 1L, cliente, lineasPedido, 1, 10.0
    );

    private final PostAndPutPedidoRequestDto postAndPutRequestDto = new PostAndPutPedidoRequestDto(
            1L, cliente, lineasPedido
    );

    @BeforeEach
    void setUp() {
        when(webSocketConfig.webSocketPedidosHandler()).thenReturn(webSocketHandler);

        pedidosServiceImpl = new PedidosServiceImpl(
                pedidoRepository,
                productsRepository,
                emailService,
                pedidosMapper,
                webSocketConfig
        );
    }

    @Test
    @DisplayName("FindAll - Devuelve página de pedidos")
    void findAllByOrderByIdAsc() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pedido> page = new PageImpl<>(List.of(pedido));
        when(pedidoRepository.findAll(pageable)).thenReturn(page);
        when(pedidosMapper.toResponse(pedido)).thenReturn(pedidoResponse);

        // Act
        Page<GenericPedidosResponseDto> result = pedidosServiceImpl.findAll(pageable);

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.getTotalElements()),
                () -> assertEquals(pedidoResponse.getId(), result.getContent().get(0).getId()),
                () -> verify(pedidoRepository).findAll(pageable),
                () -> verify(pedidosMapper).toResponse(pedido)
        );
    }

    @Test
    @DisplayName("FindById - Devuelve pedido")
    void findById_ShouldReturnPedido() {
        // Arrange
        when(pedidoRepository.findById(objectId)).thenReturn(Optional.of(pedido));
        when(pedidosMapper.toResponse(pedido)).thenReturn(pedidoResponse);

        // Act
        GenericPedidosResponseDto result = pedidosServiceImpl.findById(objectId);

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(pedidoResponse.getId(), result.getId()),
                () -> verify(pedidoRepository).findById(objectId),
                () -> verify(pedidosMapper).toResponse(pedido)
        );
    }

    @Test
    @DisplayName("FindById - Lanza NotFoundException")
    void findById_ShouldThrowNotFoundException() {
        // Arrange
        when(pedidoRepository.findById(objectId)).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(PedidoException.NotFoundException.class, () -> {
            pedidosServiceImpl.findById(objectId);
        });

        assertEquals("SERVICE: No se encontró el pedido con id: " + objectId, exception.getMessage());
        verify(pedidoRepository).findById(objectId);
        verify(pedidosMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Save - Guarda pedido y envía email/websocket")
    void save_ShouldSaveAndNotify() throws InterruptedException, IOException {
        // Arrange
        when(productsRepository.findById(idProducto)).thenReturn(Optional.of(productoMock)); // Validación OK
        when(pedidosMapper.toModel(postAndPutRequestDto)).thenReturn(pedido);
        when(pedidoRepository.save(pedido)).thenReturn(pedido);
        when(pedidosMapper.toResponse(pedido)).thenReturn(pedidoResponse);

        // Mocks para los hilos asíncronos (email y websocket)
        doNothing().when(emailService).enviarConfirmacionPedidoHtml(pedido);
        doNothing().when(webSocketHandler).sendMessage(anyString());

        // Act
        GenericPedidosResponseDto result = pedidosServiceImpl.save(postAndPutRequestDto);

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(pedidoResponse.getId(), result.getId()),
                () -> verify(productsRepository).findById(idProducto),
                // CORRECCIÓN 2: Verificar que se llama 2 veces
                () -> verify(pedidosMapper, times(2)).toModel(postAndPutRequestDto),
                () -> verify(pedidoRepository).save(pedido),
                () -> verify(pedidosMapper).toResponse(pedido)
        );

        // Verificar llamadas asíncronas con timeout
        verify(emailService, timeout(1000)).enviarConfirmacionPedidoHtml(pedido);
        verify(webSocketHandler, timeout(1000)).sendMessage(anyString());
    }

    @Test
    @DisplayName("Save - Lanza NotFoundException si producto no existe")
    void save_ShouldThrowNotFoundExceptionIfProductMissing() {
        // Arrange
        when(productsRepository.findById(idProducto)).thenReturn(Optional.empty()); // Validación KO

        // Act & Assert
        var exception = assertThrows(PedidoException.NotFoundException.class, () -> {
            pedidosServiceImpl.save(postAndPutRequestDto);
        });

        assertEquals("Producto no encontrado con id: " + idProducto, exception.getMessage());
        verify(productsRepository).findById(idProducto);
        verify(pedidoRepository, never()).save(any());
        verify(emailService, never()).enviarConfirmacionPedidoHtml(any());
    }

    @Test
    @DisplayName("Update - Actualiza pedido y envía websocket")
    void update_ShouldUpdateAndNotify() throws IOException {
        // Arrange
        when(pedidoRepository.findById(objectId)).thenReturn(Optional.of(pedido));
        when(productsRepository.findById(idProducto)).thenReturn(Optional.of(productoMock)); // Validación OK
        when(pedidosMapper.toModel(postAndPutRequestDto)).thenReturn(pedido);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido); // 'any' porque se actualiza internamente
        when(pedidosMapper.toResponse(pedido)).thenReturn(pedidoResponse);
        doNothing().when(webSocketHandler).sendMessage(anyString());

        // Act
        GenericPedidosResponseDto result = pedidosServiceImpl.update(objectId, postAndPutRequestDto);

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(pedidoResponse.getId(), result.getId()),
                () -> verify(pedidoRepository).findById(objectId),
                () -> verify(productsRepository).findById(idProducto),
                () -> verify(pedidoRepository).save(any(Pedido.class)),
                () -> verify(pedidosMapper).toResponse(pedido)
        );

        // Verificar llamada asíncrona con timeout
        verify(webSocketHandler, timeout(1000)).sendMessage(anyString());
    }

    @Test
    @DisplayName("Update - Lanza NotFoundException si pedido no existe")
    void update_ShouldThrowNotFoundExceptionIfPedidoMissing() {
        // Arrange
        when(productsRepository.findById(idProducto)).thenReturn(Optional.of(productoMock));
        when(pedidoRepository.findById(objectId)).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(PedidoException.NotFoundException.class, () -> {
            pedidosServiceImpl.update(objectId, postAndPutRequestDto);
        });

        // Ahora el mensaje de excepción esperado será el correcto
        assertEquals("Pedido no encontrado con id: " + objectId, exception.getMessage());
        verify(pedidoRepository).findById(objectId);
        verify(productsRepository).findById(idProducto); // Se verifica que la validación se intentó
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update - Lanza NotFoundException si producto no existe")
    void update_ShouldThrowNotFoundExceptionIfProductMissing() {
        // Arrange
        when(productsRepository.findById(idProducto)).thenReturn(Optional.empty()); // Validación KO

        // Act & Assert
        var exception = assertThrows(PedidoException.NotFoundException.class, () -> {
            pedidosServiceImpl.update(objectId, postAndPutRequestDto);
        });

        assertEquals("Producto no encontrado con id: " + idProducto, exception.getMessage());
        verify(productsRepository).findById(idProducto);
        verify(pedidoRepository, never()).save(any());
    }


    @Test
    @DisplayName("Delete - Elimina pedido y envía websocket")
    void delete_ShouldDeleteAndNotify() throws IOException {
        // Arrange
        when(pedidoRepository.findById(objectId)).thenReturn(Optional.of(pedido));
        doNothing().when(pedidoRepository).delete(pedido);
        when(pedidosMapper.toResponse(pedido)).thenReturn(pedidoResponse);
        doNothing().when(webSocketHandler).sendMessage(anyString());

        // Act
        DeletePedidosResponseDto result = pedidosServiceImpl.delete(objectId);

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(pedidoResponse, result.getGenericPedidosResponseDto()),
                () -> assertEquals("Pedido con id: " + objectId + " eliminado correctamente.", result.getMessage()),
                () -> verify(pedidoRepository).findById(objectId),
                () -> verify(pedidoRepository).delete(pedido),
                () -> verify(pedidosMapper).toResponse(pedido)
        );

        // Verificar llamada asíncrona con timeout
        verify(webSocketHandler, timeout(1000)).sendMessage(anyString());
    }

    @Test
    @DisplayName("Delete - Lanza NotFoundException si pedido no existe")
    void delete_ShouldThrowNotFoundException() {
        // Arrange
        when(pedidoRepository.findById(objectId)).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(PedidoException.NotFoundException.class, () -> {
            pedidosServiceImpl.delete(objectId);
        });

        assertEquals("Pedido no encontrado con id: " + objectId, exception.getMessage());
        verify(pedidoRepository).findById(objectId);
        verify(pedidoRepository, never()).delete(any());
        verify(pedidosMapper, never()).toResponse(any());
    }
}