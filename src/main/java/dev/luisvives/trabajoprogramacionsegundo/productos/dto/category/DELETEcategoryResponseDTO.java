package dev.luisvives.trabajoprogramacionsegundo.productos.dto.category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para representar la respuesta de una operación de eliminación de categoría.
 * <p>
 * Contiene un mensaje informativo sobre el resultado de la operación y, opcionalmente,
 * la información de la categoría que fue eliminada.
 * </p>
 *
 * <b>Campos:</b>
 * <ul>
 *   <li><b>message</b> (<code>String</code>): Mensaje que describe el resultado de la eliminación.</li>
 *   <li><b>deletedCategory</b> (<code>GENERICcategoryResponseDTO</code>): Objeto con la información de la categoría eliminada.</li>
 * </ul>
 *
 * <p><b>Nota:</b> Se incluye el constructor sin argumentos mediante {@link NoArgsConstructor} porque es
 * necesario para que <b>Jackson</b> pueda deserializar correctamente el objeto desde JSON en pruebas o llamadas REST.</p>
 *
 * @see GENERICcategoryResponseDTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor // Necesario para que Jackson pueda deserializar desde JSON.
public class DELETEcategoryResponseDTO {

    /**
     * Mensaje descriptivo sobre el resultado de la eliminación.
     */
    private String message;

    /**
     * Objeto con la información de la categoría eliminada.
     */
    private GENERICcategoryResponseDTO deletedCategory;
}
