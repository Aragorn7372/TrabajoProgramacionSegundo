package dev.luisvives.trabajoprogramacionsegundo.productos.dto.category;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PATCHcategoryRequestDTO {
    //Puede estar vacío
    //En caso de que se envíe nombre, no nos vale como valor "" ni " "
    @Pattern(regexp = "^(?!\\s*$).+", message = "El nombre no puede estar vacío si se envía")
    private String name;
}