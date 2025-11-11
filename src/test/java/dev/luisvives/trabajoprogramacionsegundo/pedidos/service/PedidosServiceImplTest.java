package dev.luisvives.trabajoprogramacionsegundo.pedidos.service;

import dev.luisvives.trabajoprogramacionsegundo.common.email.OrderEmailService;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.dto.response.GenericPedidosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.mappers.PedidosMapper;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Cliente;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Direccion;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.LineaPedido;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.repository.PedidosRepository;
import dev.luisvives.trabajoprogramacionsegundo.productos.repository.ProductsRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    @InjectMocks
    private PedidosServiceImpl pedidosServiceImpl;
    // === DATOS DE PRUEBA ===
    private ObjectId objectId = new  ObjectId();

    private Direccion direccion = new Direccion(
            "calle",
            "1",
            "ciudad",
            "provincia",
            "pais",
            "01000"
    );

    private Cliente cliente = new Cliente(
            "Pepe",
            "pepe@mail.com",
            "123456789",
            direccion
    );
    private LineaPedido lineaPedido = new LineaPedido(
            1,
            1L,
            10.0,
            10.0
    );

    private List<LineaPedido> lineasPedido = List.of(lineaPedido);
    private Pedido pedido = new Pedido(
            objectId,
            1L,
            cliente,
            lineasPedido,
            1,
            10.0,
            LocalDateTime.now(),
            LocalDateTime.now(),
            false
    );

    private GenericPedidosResponseDto pedidoResponse =  new GenericPedidosResponseDto(
            objectId,
            1L,
            cliente,
            lineasPedido,
            1,
            10.0
    );
    
}