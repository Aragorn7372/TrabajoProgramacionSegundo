package dev.luisvives.trabajoprogramacionsegundo.productos.repository;

import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Clase que representa el repositorio de categorías, que realiza las operaciones CRUD.
 * @see Categoria
 */
@Repository
public interface CategoriesRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByNameIgnoreCase(@NotBlank(message = "La categoría no puede estar vacía") String category);
}