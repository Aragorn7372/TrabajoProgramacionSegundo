package dev.luisvives.trabajoprogramacionsegundo.productos.exceptions;

import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria;

import java.util.UUID;

/**
 * Clase que define las excepciones de dominio relativas a la clase Categoria
 * @see Categoria
 */
public class CategoryNotFoundException extends CategoryException {

    /**
     * Subclase de excepciones de dominio que define la no existencia de una categoria por un, id
     * @param id
     */
    public CategoryNotFoundException(UUID id) {
        super("Categoría con id " + id + " no encontrada");
    }

    /**
     * Subclase de excepciones de dominio que define la no existencia de una categoria por un nombre
     * @param name
     */
    public CategoryNotFoundException(String name) {
        super("Categoría con nombre " + name + " no encontrada");
    }
}