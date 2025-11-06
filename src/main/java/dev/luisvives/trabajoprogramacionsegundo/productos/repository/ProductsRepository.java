package dev.luisvives.trabajoprogramacionsegundo.productos.repository;

import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepository extends JpaRepository <Producto, Long>{


}
