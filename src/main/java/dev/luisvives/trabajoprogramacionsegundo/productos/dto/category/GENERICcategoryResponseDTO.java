package dev.luisvives.trabajoprogramacionsegundo.productos.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GENERICcategoryResponseDTO {
    private Long id;
    private String name;
}
