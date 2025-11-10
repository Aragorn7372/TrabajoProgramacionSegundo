package dev.luisvives.trabajoprogramacionsegundo.productos.dto.category;

import lombok.Data;

import java.util.UUID;

/**
 * DTO genérico utilizado para representar la información de una categoría.
 * <p>
 * Contiene únicamente los datos esenciales de la categoría, como su identificador
 * y nombre.
 * </p>
 *
 * <b>Campos:</b>
 * <ul>
 *   <li><b>id</b> (<code>Long</code>): Identificador único de la categoría.</li>
 *   <li><b>name</b> (<code>String</code>): Nombre de la categoría.</li>
 * </ul>
 */
@Data
public class GENERICcategoryResponseDTO {

    /**
     * Identificador único de la categoría.
     */
    private UUID id;

    /**
     * Nombre de la categoría.
     */
    private String name;
}
