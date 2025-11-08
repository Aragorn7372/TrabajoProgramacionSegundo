package dev.luisvives.trabajoprogramacionsegundo.productos.dto.category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // NECESARIO para que Jackson pueda deserializar en el test, es decir, que pueda
//instanciarlo a partir del JSON.
public class DELETEcategoryResponseDTO {
    private String message;
    private GENERICcategoryResponseDTO deletedCategory;
}