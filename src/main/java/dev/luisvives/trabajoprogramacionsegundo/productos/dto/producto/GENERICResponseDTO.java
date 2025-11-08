package dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto;

import lombok.Data;

@Data
public class GENERICResponseDTO {
    private Long id;
    private String uuid;
    private String name;
    private Double price;
    private String category;
    private String releaseDate;
    private String image;
}
