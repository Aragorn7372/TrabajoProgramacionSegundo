package dev.luisvives.trabajoprogramacionsegundo.notificaciones.dto.pedidos;

import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Direccion;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido;
import lombok.Data;

@Data
/**
 * Clase que contiene la información de un cliente en un pedido.
 * @param nombreCompleto Nombre y apellidos del cliente.
 * @param email Correo electrónico del cliente.
 * @param telefono Número de teléfono del cliente.
 * @param direccion Objeto que define la dirección del cliente
 * @see PedidoNotificacionDto
 * @see DireccionNotificacionDto
 */
public class ClienteNotificacionDto {
    private String nombreCompleto;
    private String email;
    private String telefono;
    private DireccionNotificacionDto direccion;
}
