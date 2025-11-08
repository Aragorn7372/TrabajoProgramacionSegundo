package dev.luisvives.trabajoprogramacionsegundo.productos.repository;

import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Clase que representa el repositorio de categorías, que realiza las operaciones CRUD.
 * @see Categoria
 */
public interface CategoriesRepository extends JpaRepository<Categoria, UUID> {
}
