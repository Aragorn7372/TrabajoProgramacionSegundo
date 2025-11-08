package dev.luisvives.trabajoprogramacionsegundo.notificaciones.dto.productos;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import lombok.Data;

/**
 * Clase que describe el modelo de transferencia de datos de Producto
 * @see Producto
 */
@Data
public class ProductoNotificacionDto {
    private Long id;
    private String nombre;
    private Double precio;
    private Integer cantidad;
    private String imagen;
    private String Categoria;
    private String fechaCreacion;
    private String fechaActualizacion;
}
