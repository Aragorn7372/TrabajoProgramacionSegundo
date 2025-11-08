package dev.luisvives.trabajoprogramacionsegundo.productos.dto.productos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para las respuestas a las peticiones DELETE.
 * En el enunciado de la práctica se nos pide devolver un mensaje informativo
 * y el propio Funko eliminado cuando se ha completado con éxito. Mediante este
 * DTO, conseguirmos encapsular ambos para devolverlos.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor // NECESARIO para que Jackson pueda deserializar en el test, es decir, que pueda
//instanciarlo a partir del JSON.
public class DELETEProductoResponseDTO {
    private String message;
    private GENERICProductoResponseDTO deletedFunko;
}
