package dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para actualizar parcialmente un producto.
 * <p>
 * Todos los campos son opcionales (pueden enviarse vacíos), y se actualizan
 * únicamente aquellos que se incluyen en la petición.
 * </p>
 *
 * <b>Campos:</b>
 * <ul>
 *   <li><b>name</b> (<code>String</code>): Nombre del producto.
 *       <ul>
 *         <li>Opcional: puede estar vacío.</li>
 *         <li>Si se envía, no puede ser vacío ni contener solo espacios en blanco.</li>
 *         <li>Validado con <code>@Pattern(regexp = "^(?!\\s*$).+")</code>.</li>
 *       </ul>
 *   </li>
 *   <li><b>price</b> (<code>Double</code>): Precio del producto.
 *       <ul>
 *         <li>Opcional: puede estar vacío.</li>
 *         <li>Si se envía, debe ser mayor o igual a 0.0.</li>
 *         <li>Validado con <code>@Min(0)</code>.</li>
 *       </ul>
 *   </li>
 *   <li><b>category</b> (<code>String</code>): Nombre de la categoría. Opcional.</li>
 *   <li><b>description</b> (<code>String</code>): Descripción del producto. Opcional.</li>
 *   <li><b>image</b> (<code>String</code>): Nombre de la imagen asociada.
 *       <ul>
 *         <li>No tiene validaciones de Jakarta, ya que inicialmente está vacío.</li>
 *         <li>Se asigna un nombre posteriormente al guardar la imagen en la base de datos.</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <p><b>Notas:</b>
 * <ul>
 *   <li>Este DTO se usa en operaciones PATCH, por lo que todos los campos son opcionales.</li>
 *   <li>Si se envía el nombre, no se permite que sea vacío o contenga solo espacios.</li>
 * </ul>
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PATCHProductoRequestDTO {

    /**
     * Nombre del producto.
     * Validación: no puede estar vacío ni solo espacios si se envía.
     */
    @Pattern(regexp = "^(?!\\s*$).+", message = "El nombre no puede estar vacío si se envía")
    private String name;

    /**
     * Precio del producto.
     * Debe ser mayor o igual a 0 si se envía.
     */
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double price;

    /**
     * Cantidad de producto en stock
     */
    private Integer cantidad;

    /**
     * Nombre de la categoría a la que pertenece el producto.
     */
    private String category;

    /**
     * Descripción del producto.
     */
    private String description;

    /**
     * Nombre de la imagen asociada al producto.
     * Inicialmente vacío y se asigna después de guardar la imagen en la base de datos.
     */
    private String image;

}
