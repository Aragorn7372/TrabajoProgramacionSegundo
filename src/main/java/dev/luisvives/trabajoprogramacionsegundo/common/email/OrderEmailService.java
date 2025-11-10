package dev.luisvives.trabajoprogramacionsegundo.common.email;

import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido;

/**
 * Servicio para el envío de emails relacionados con pedidos
 */
public interface OrderEmailService {

    /**
     * Envía email de confirmación de pedido en HTML simple
     * @param pedido El pedido para el cual enviar la confirmación
     */
    void enviarConfirmacionPedido(Pedido pedido);

    /**
     * Envía email de confirmación de pedido en formato HTML completo
     * @param pedido El pedido para el cual enviar la confirmación
     */
    void enviarConfirmacionPedidoHtml(Pedido pedido);
}