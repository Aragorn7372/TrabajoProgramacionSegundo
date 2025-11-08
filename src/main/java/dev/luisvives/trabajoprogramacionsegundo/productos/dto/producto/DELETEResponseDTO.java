package dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // NECESARIO para que Jackson pueda deserializar en el test, es decir, que pueda
//instanciarlo a partir del JSON.
public class DELETEResponseDTO {
    private String message;
    private GENERICResponseDTO deletedProducto;
}
