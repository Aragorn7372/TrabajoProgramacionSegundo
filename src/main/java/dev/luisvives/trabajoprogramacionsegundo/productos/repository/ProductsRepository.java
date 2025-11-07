package dev.luisvives.trabajoprogramacionsegundo.productos.repository;

import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Clase que representa el repositorio de productos, que realiza las operaciones CRUD.
 * @see Producto
 */
public interface ProductsRepository extends JpaRepository <Producto, Long>{
}
