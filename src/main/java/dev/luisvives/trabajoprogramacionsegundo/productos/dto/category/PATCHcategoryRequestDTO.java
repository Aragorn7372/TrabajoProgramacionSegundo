package dev.luisvives.trabajoprogramacionsegundo.productos.dto.category;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO utilizado para actualizar parcialmente una categoría.
 * <p>
 * Todos los campos son opcionales y se actualizan únicamente los que se envían
 * en la petición PATCH.
 * </p>
 *
 * <b>Campos:</b>
 * <ul>
 *   <li><b>name</b> (<code>String</code>): Nombre de la categoría.
 *       <ul>
 *         <li>Opcional: puede estar vacío.</li>
 *         <li>Si se envía, no puede ser vacío ni contener solo espacios en blanco.</li>
 *         <li>Validado con <code>@Pattern(regexp = "^(?!\\s*$).+")</code>.</li>
 *       </ul>
 *   </li>
 * </ul>
 */
@Data
public class PATCHcategoryRequestDTO {

    /**
     * Nombre de la categoría.
     * No puede estar vacío ni contener solo espacios si se envía.
     */
    @Pattern(regexp = "^(?!\\s*$).+", message = "El nombre no puede estar vacío si se envía")
    private String name;
}
