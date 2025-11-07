package dev.luisvives.trabajoprogramacionsegundo.pedidos.exceptions;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.LineaPedido;
/**
 * Clase que define las excepciones de dominio relativas a la clase Pedido
 * @see Pedido
 */
public sealed class PedidoException extends RuntimeException {
    public PedidoException(String message) { super(message); }

    /**
     * Subclase de excepciones de dominio que define la no existencia de un Pedido
     * @see Pedido
     */
    public static final class NotFoundException extends PedidoException {
        public NotFoundException(String message) { super(message); }
    }

    /**
     * Subclase de excepciones de dominio que define un error de lógica de negocio con el campo lineasPedido
     * @see Pedido
     */
    public static final class NoLinesException extends PedidoException {
        public NoLinesException(String message) { super(message); }
    }

    /**
     * Subclase de excepciones de dominio que define un conflicto de lógica de negocio con el campo precioProducto
     * @see LineaPedido
     */
    public static final class BadPriceException extends PedidoException {
        public BadPriceException(String message) { super(message); }
    }
}
