package dev.luisvives.trabajoprogramacionsegundo.notificaciones.dto.pedidos;

import dev.luisvives.trabajoprogramacionsegundo.notificaciones.dto.productos.ProductoNotificacionDto;
import lombok.Data;

@Data
/**
 * Clase que contiene la información de un producto que ha comprado un cliente
 * @see ClienteNotificacionDto
 * @see ProductoNotificacionDto
 */
public class LineaPedidoNotificacionDto {
    private Integer cantidad;
    private Long idProducto;
    private Double precio;
    private Double total;
}
