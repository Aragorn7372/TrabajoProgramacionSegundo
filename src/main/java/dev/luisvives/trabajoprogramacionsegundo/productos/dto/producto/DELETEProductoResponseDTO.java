package dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para representar la respuesta de una operación de eliminación
 * de un producto u otra entidad similar.
 * <p>
 * Contiene un mensaje descriptivo sobre el resultado de la operación y, opcionalmente,
 * la información del producto que fue eliminado.
 * </p>
 *
 * <b>Campos:</b>
 * <ul>
 *   <li><b>message</b> (<code>String</code>): Mensaje informativo sobre el resultado de la eliminación.</li>
 *   <li><b>deletedProducto</b> (<code>GENERICResponseDTO</code>): Objeto con la información del producto eliminado.</li>
 * </ul>
 *
 * <p><b>Nota:</b> Se incluye el constructor sin argumentos mediante {@link NoArgsConstructor}
 * porque es necesario para que <b>Jackson</b> pueda deserializar el objeto correctamente
 * al convertir desde JSON durante las pruebas o llamadas REST.</p>
 *
 * @see GENERICProductosResponseDTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor // Necesario para que Jackson pueda deserializar desde JSON.
public class DELETEProductoResponseDTO {

    /**
     * Mensaje informativo sobre el resultado de la operación de eliminación.
     */
    private String message;

    /**
     * Objeto con la información del producto eliminado.
     */
    private GENERICProductosResponseDTO deletedProducto;
}
