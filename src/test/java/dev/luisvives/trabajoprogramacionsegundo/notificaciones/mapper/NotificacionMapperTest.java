package dev.luisvives.trabajoprogramacionsegundo.notificaciones.mapper;

import dev.luisvives.trabajoprogramacionsegundo.notificaciones.dto.pedidos.ClienteNotificacionDto;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.dto.pedidos.DireccionNotificacionDto;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.dto.pedidos.LineaPedidoNotificacionDto;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.dto.pedidos.PedidoNotificacionDto;
import dev.luisvives.trabajoprogramacionsegundo.notificaciones.dto.productos.ProductoNotificacionDto;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Cliente;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Direccion;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.LineaPedido;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificacionMapperTest {

    private Producto producto;
    private Categoria categoria;
    private Direccion direccion;
    private Cliente cliente;
    private LineaPedido linea1;
    private LineaPedido linea2;
    private Pedido pedido;

    private final LocalDateTime now = LocalDateTime.now();
    private final ObjectId pedidoId = new ObjectId();


    @BeforeEach
    void setUp() {
        categoria = new Categoria(1L, "TEST_CAT", now, now);

        producto = Producto.builder()
                .id(1L)
                .nombre("Test Producto")
                .precio(99.99)
                .cantidad(10)
                .imagen("test.jpg")
                .categoria(categoria)
                .fechaCreacion(now)
                .fechaModificacion(now)
                .build();

        direccion = Direccion.builder()
                .calle("Calle Falsa")
                .numero("123")
                .ciudad("Testville")
                .provincia("Testlandia")
                .pais("Testland")
                .codigoPostal("12345")
                .build();

        cliente = Cliente.builder()
                .nombreCompleto("Juan Cliente")
                .email("juan@test.com")
                .telefono("600111222")
                .direccion(direccion)
                .build();

        // El builder no usa los setters, por lo que el total
        // debe calcularse y asignarse manualmente en el test.
        linea1 = LineaPedido.builder()
                .idProducto(1L)
                .cantidad(2)
                .precioProducto(50.0)
                .total(100.0) // 2 * 50.0
                .build();

        linea2 = LineaPedido.builder()
                .idProducto(2L)
                .cantidad(1)
                .precioProducto(25.5)
                .total(25.5) // 1 * 25.5
                .build();

        // Construimos el pedido real
        pedido = Pedido.builder()
                .id(pedidoId)
                .idUsuario(1L)
                .cliente(cliente)
                .createdAt(now)
                .updatedAt(now)
                .isDeleted(false)
                .build();

        // Asignamos las líneas para disparar la lógica de cálculo
        // Total: 125.5, Items: 2
        pedido.setLineasPedido(List.of(linea1, linea2));
    }

    @Test
    @DisplayName("Mapea Producto a ProductoNotificacionDto")
    void toDto_with_Producto_MapeaCorrectamente() {
        // Act
        ProductoNotificacionDto dto = NotificacionMapper.toDto(producto);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(producto.getId());
        assertThat(dto.getNombre()).isEqualTo(producto.getNombre());
        assertThat(dto.getPrecio()).isEqualTo(producto.getPrecio());
        assertThat(dto.getCantidad()).isEqualTo(producto.getCantidad());
        assertThat(dto.getImagen()).isEqualTo(producto.getImagen());
        assertThat(dto.getFechaCreacion()).isEqualTo(producto.getFechaCreacion().toString());
        assertThat(dto.getFechaActualizacion()).isEqualTo(producto.getFechaModificacion().toString());

        // El mapper no mapea el campo Categoria
        assertThat(dto.getCategoria()).isNull();
    }

    @Nested
    @DisplayName("Pruebas de Mapeo de Pedido")
    class PedidoMappingTest {

        private PedidoNotificacionDto dto;

        @BeforeEach
        void mapPedido() {
            // Act
            dto = NotificacionMapper.toDto(pedido);
            assertThat(dto).isNotNull();
        }

        @Test
        @DisplayName("Mapea campos principales de Pedido")
        void toDto_Pedido_MapeaCamposPrincipales() {
            // Assert
            assertThat(dto.getId()).isEqualTo(pedidoId.toHexString());
            assertThat(dto.getTotalItems()).isEqualTo(2); // Calculado
            assertThat(dto.getTotal()).isEqualTo(125.5); // Calculado
            assertThat(dto.getIsDeleted()).isFalse();
            assertThat(dto.getCreatedAt()).isEqualTo(now.toString());
            assertThat(dto.getUpdatedAt()).isEqualTo(now.toString());
            // El IdUsuario no está siendo mapeado
            assertThat(dto.getIdUsuario()).isNull();
        }

        @Test
        @DisplayName("Mapea Cliente (prueba interna toDto(Cliente))")
        void toDto_Pedido_MapeaCliente() {
            // Assert
            ClienteNotificacionDto clienteDto = dto.getCliente();
            assertThat(clienteDto).isNotNull();
            assertThat(clienteDto.getNombreCompleto()).isEqualTo(cliente.nombreCompleto());
            assertThat(clienteDto.getEmail()).isEqualTo(cliente.email());
            assertThat(clienteDto.getTelefono()).isEqualTo(cliente.telefono());
        }

        @Test
        @DisplayName("Mapea Direccion (prueba interna toDto(Direccion))")
        void toDto_Pedido_MapeaDireccion() {
            // Assert
            DireccionNotificacionDto direccionDto = dto.getCliente().getDireccion();
            assertThat(direccionDto).isNotNull();
            assertThat(direccionDto.getCalle()).isEqualTo(direccion.calle());
            assertThat(direccionDto.getCiudad()).isEqualTo(direccion.ciudad());
            assertThat(direccionDto.getCodigoPostal()).isEqualTo(direccion.codigoPostal());
            assertThat(direccionDto.getPais()).isEqualTo(direccion.pais());
            assertThat(direccionDto.getProvincia()).isEqualTo(direccion.provincia());
            assertThat(direccionDto.getNumero()).isEqualTo(direccion.numero());
        }

        @Test
        @DisplayName("Mapea Lineas Pedido (prueba interna toDto(LineaPedido))")
        void toDto_Pedido_MapeaLineasPedido() {
            // Assert
            List<LineaPedidoNotificacionDto> lineasDto = dto.getLineasPedido();
            assertThat(lineasDto).isNotNull().hasSize(2);

            // Comprobar Linea 1
            LineaPedidoNotificacionDto lineaDto1 = lineasDto.get(0);
            assertThat(lineaDto1.getIdProducto()).isEqualTo(linea1.getIdProducto());
            assertThat(lineaDto1.getCantidad()).isEqualTo(linea1.getCantidad());
            assertThat(lineaDto1.getPrecio()).isEqualTo(linea1.getPrecioProducto());
            assertThat(lineaDto1.getTotal()).isEqualTo(linea1.getTotal());

            // Comprobar Linea 2
            LineaPedidoNotificacionDto lineaDto2 = lineasDto.get(1);
            assertThat(lineaDto2.getIdProducto()).isEqualTo(linea2.getIdProducto());
            assertThat(lineaDto2.getCantidad()).isEqualTo(linea2.getCantidad());
            assertThat(lineaDto2.getPrecio()).isEqualTo(linea2.getPrecioProducto());
            assertThat(lineaDto2.getTotal()).isEqualTo(linea2.getTotal());
        }
    }
}