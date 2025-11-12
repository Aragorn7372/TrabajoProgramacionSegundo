package dev.luisvives.trabajoprogramacionsegundo.productos.exceptions;

import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria;

/**
 * Clase que define las excepciones de dominio relativas a la clase Categoria
 * @see Categoria
 */
public class CategoryValidationException extends RuntimeException {
    public CategoryValidationException(String message) {
        super(message);
    }
}
