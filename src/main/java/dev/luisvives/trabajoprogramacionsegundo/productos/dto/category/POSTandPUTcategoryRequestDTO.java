package dev.luisvives.trabajoprogramacionsegundo.productos.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO utilizado para crear o actualizar una categoría mediante POST o PUT.
 * <p>
 * Todos los campos son obligatorios y deben cumplir con las validaciones especificadas
 * para asegurar la integridad de los datos.
 * </p>
 *
 * <b>Campos:</b>
 * <ul>
 *   <li><b>name</b> (<code>String</code>): Nombre de la categoría.
 *       <ul>
 *         <li>No puede estar vacío ni contener solo espacios en blanco.</li>
 *         <li>Validado con <code>@NotBlank</code>.</li>
 *       </ul>
 *   </li>
 * </ul>
 */
@Data
public class POSTandPUTcategoryRequestDTO {

    /**
     * Nombre de la categoría.
     * No puede estar vacío ni contener solo espacios en blanco.
     */
    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;
}
