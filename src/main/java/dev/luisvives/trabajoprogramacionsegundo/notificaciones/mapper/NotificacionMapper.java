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
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import lombok.NoArgsConstructor;
import lombok.val;

/**
 * Mapper que convierte clases del dominio en un DTO para notificaciones
 */
@NoArgsConstructor
public class NotificacionMapper {
    public static ProductoNotificacionDto toDto(Producto producto) {
        val dto = new ProductoNotificacionDto();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setPrecio(producto.getPrecio());
        dto.setCantidad(producto.getCantidad());
        dto.setImagen(producto.getImagen());
        dto.setFechaCreacion(producto.getFechaCreacion().toString());
        dto.setFechaActualizacion(producto.getFechaModificacion().toString());
        return dto;
    }
    public static PedidoNotificacionDto toDto(Pedido pedido) {
        val dto = new PedidoNotificacionDto();
        dto.setId(pedido.getIdString());
        dto.setCliente(toDto(pedido.getCliente()));
        dto.setLineasPedido(pedido.getLineasPedido().stream().map(NotificacionMapper::toDto).toList());
        dto.setTotalItems(pedido.getTotalItems());
        dto.setTotal(pedido.getTotal());
        dto.setCreatedAt(pedido.getCreatedAt().toString());
        dto.setUpdatedAt(pedido.getUpdatedAt().toString());
        dto.setIsDeleted(pedido.getIsDeleted());
        return dto;
    }

    // Conversiones internas para facilitar el mapeo de las clases
    private static LineaPedidoNotificacionDto toDto(LineaPedido lineaPedido) {
        val dto = new LineaPedidoNotificacionDto();
        dto.setCantidad(lineaPedido.getCantidad());
        dto.setIdProducto(lineaPedido.getIdProducto());
        dto.setPrecio(lineaPedido.getPrecioProducto());
        dto.setTotal(lineaPedido.getTotal());
        return dto;
    }

    private static ClienteNotificacionDto toDto(Cliente cliente) {
        val dto = new ClienteNotificacionDto();
        dto.setNombreCompleto(cliente.nombreCompleto());
        dto.setEmail(cliente.email());
        dto.setTelefono(cliente.telefono());
        dto.setDireccion(toDto(cliente.direccion()));
        return dto;
    }

    private static DireccionNotificacionDto toDto(Direccion direccion) {
        val dto = new DireccionNotificacionDto();
        dto.setCalle(direccion.calle());
        dto.setNumero(direccion.numero());
        dto.setCiudad(direccion.ciudad());
        dto.setProvincia(direccion.provincia());
        dto.setPais(direccion.pais());
        dto.setCodigoPostal(direccion.codigoPostal());
        return dto;
    }
}
