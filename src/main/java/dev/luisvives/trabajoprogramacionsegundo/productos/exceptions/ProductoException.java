package dev.luisvives.trabajoprogramacionsegundo.productos.exceptions;

import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;

/**
 * Clase que define las excepciones de dominio relativas a la clase Producto
 * @see Producto
 */
public sealed class ProductoException extends RuntimeException {
    public ProductoException(String message) { super(message); }

    /**
     * Subclase de excepciones de dominio que define la no existencia de un Producto
     * @see Producto
     */
    public static final class NotFoundException extends ProductoException {
        public NotFoundException(String message) { super(message); }
    }

    /**
     * Subclase de excepciones de dominio que define un conflicto de integridad referencial con el campo categoria
     * @see Producto
     */
    public static final class ValidationException extends ProductoException {
        public ValidationException(String message) { super(message); }
    }
}
