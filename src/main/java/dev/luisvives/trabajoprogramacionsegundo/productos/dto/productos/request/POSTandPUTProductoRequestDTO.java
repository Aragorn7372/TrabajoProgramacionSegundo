package dev.luisvives.trabajoprogramacionsegundo.productos.dto.productos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO para las peticiones POST (create) y PUT (update) del usuario.
 * Solo incluimos los campos del modelo que vamos a dejar a manos del usuario,
 * excluyendo id, createdAt y updatedAt, que serán manejados internamente
 * por seguridad.
 * A diferencia del DTO de las peticiones PATCH, en este todos los campos
 * son @NotNull o @NotBlank (para los String), para ser coherentes con la filosofía rest.
 * Así, si al hacer un POST o un PUT dejan alguno de los campos vacío o con un valor que no cumple
 * con las reglas de validación, saltará un 400 Bad Request.
 */
@Data
public class POSTandPUTProductoRequestDTO {
    //No debe estar vacía y debe cumplir con la RegEx
    //OJO, luego nos tocará parsearlo de STRING a UUID
    @NotBlank(message = "El UUID no puede estar vacío")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "El UUID no tiene un formato valido")
    private String uuid;
    //No debe ser nulo y debe contener al menos un carácter que no sea un espacio en blanco
    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;
    //No debe ser nulo y su valor mínimo es 0.0
    @NotNull(message = "El precio no puede estar vacío")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double price;
    //No debe estar vacía
    @NotBlank(message = "La categoría no puede estar vacía")
    private String category;
    //No debe estar vacía y debe coincidir con el formato de la expresión regular
    //OJO, luego nos tocará parsearlo de STRING a LOCALDATE
    @NotBlank(message = "La fecha de lanzamiento no puede estar vacía")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "La fecha de lanzamiento debe tener formato AAAA-MM-DD")
    private String releaseDate;

    private String image;
}
