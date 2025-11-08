package dev.luisvives.trabajoprogramacionsegundo.productos.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class POSTandPUTcategoryRequestDTO {
    @NotBlank(message = "El nombre no puede estar vacío")
    private String name;
}
