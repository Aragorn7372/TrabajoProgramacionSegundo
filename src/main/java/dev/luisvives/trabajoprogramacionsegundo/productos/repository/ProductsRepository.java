package dev.luisvives.trabajoprogramacionsegundo.productos.repository;

import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;




/**
 * Clase que representa el repositorio de productos, que realiza las operaciones CRUD.
 * @see Producto
 */
@Repository
public interface ProductsRepository extends JpaRepository <Producto, Long>{
    Page<Producto> findAll(Specification<Producto> criterio, Pageable pageable);
}
