package dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class POSTandPUTRequestDTO {
    //No debe estar vacía y debe cumplir con la RegEx
    //OJO, luego nos tocará parsearlo de STRING a UUID

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
    private String description;

    private String image;
}
