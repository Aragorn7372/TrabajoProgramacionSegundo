package dev.luisvives.trabajoprogramacionsegundo.productos.dto.productos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO para las peticiones PATCH del usuario.
 * Solo incluimos los campos del modelo que vamos a dejar actualizar,
 * excluyendo id, createdAt y updatedAt, que serán manejados internamente
 * por seguridad.
 */
@Data
public class PATCHProductoRequestDTO {
    //Puede estar vacío
    //OJO, luego nos tocará parsearlo de STRING a UUID
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "El UUID no tiene un formato válido")
    private String uuid;
    //Puede estar vacío
    //En caso de que se envíe nombre, no nos vale como valor "" ni " "
    @Pattern(regexp = "^(?!\\s*$).+", message = "El nombre no puede estar vacío si se envía")
    private String name;
    //Puede estar vacío y su valor mínimo es 0.0
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double price;
    //Puede estar vacío
    private String category;
    //Puede estar vacía y debe coincidir con el formato de la expresión regular
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "La fecha de lanzamiento debe tener formato AAAA-MM-DD")
    private String releaseDate;

    private String image; // No tiene validaciones de jakarta porque es un campo que siempre
                        // esta vacio en su creacion por la necesidad de guardar primero la imagen
                        // en la base de datos para darle el nombre.
}
