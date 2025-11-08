package dev.luisvives.trabajoprogramacionsegundo.notificaciones.dto.pedidos;

import dev.luisvives.trabajoprogramacionsegundo.notificaciones.dto.productos.ProductoNotificacionDto;
import lombok.Data;

import java.util.List;

@Data
/**
 * Clase que contiene la información de los pedidos realizados en el sistema.
 * @see LineaPedidoNotificacionDto
 */
public class PedidoNotificacionDto {
    private String id; // ObjectId
    private Long idUsuario;
    private ClienteNotificacionDto cliente;
    private List<LineaPedidoNotificacionDto> lineasPedido;
    private Integer totalItems;
    private Double total;
    private String createdAt;
    private String updatedAt;
    private Boolean isDeleted;
}
