package dev.luisvives.trabajoprogramacionsegundo.productos.repository;

import dev.luisvives.trabajoprogramacionsegundo.productos.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoriesRepository extends JpaRepository<Categoria, UUID> {
}
